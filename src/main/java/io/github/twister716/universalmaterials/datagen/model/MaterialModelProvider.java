package io.github.twister716.universalmaterials.datagen.model;

import com.google.gson.JsonObject;
import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.iconset.MaterialIconSet;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.api.ore.DimensionOreConfig;
import io.github.twister716.universalmaterials.api.ore.Ore;
import io.github.twister716.universalmaterials.api.ore.OreRegistry;
import io.github.twister716.universalmaterials.api.ore.StoneGroup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * アイテム・ブロックのモデルJSONとblockstatesJSONを自動生成するProvider。
 *
 * 通常ブロック（保管ブロック等）:
 *   parent: universalmaterials:block/tinted_cube（1レイヤー着色）
 *
 * 鉱石ブロック（動的テクスチャ方式）:
 *   parent: minecraft:block/cube_all
 *   textures.all: universalmaterials:block/generated/<blockId>
 *
 *   テクスチャ実体は TextureGenerator が実行時に生成する。
 *   generated/ パスは VirtualPackResources から提供される。
 *   手動テクスチャを置く場合は textures/block/custom/ore/<blockId>.png を配置する
 *   （TextureGeneratorが自動的にスキップし、手動テクスチャが優先される）。
 */
public class MaterialModelProvider implements DataProvider {

    private final PackOutput output;

    public MaterialModelProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            String materialName = material.getId().split(":")[1];

            // 通常アイテム・ブロックのモデル生成
            for (MaterialFlag flag : material.getPartFlags()) {
                flag.getTagPrefix().ifPresent(prefix ->
                        generateModels(cache, futures, material, materialName, prefix));
            }

            if (!material.hasPropertyFlag(MaterialFlags.IS_VANILLA)) {
                for (MaterialFlag flag : material.getImpliedFlags()) {
                    flag.getTagPrefix().ifPresent(prefix ->
                            generateModels(cache, futures, material, materialName, prefix));
                }
            }

            // 鉱石ブロックのモデル生成
            if (material.hasPartFlag(MaterialFlags.GENERATE_ORE)
                    && OreRegistry.hasSettings(material)) {
                generateOreModels(cache, futures, material, materialName);
            }
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private void generateModels(CachedOutput cache, List<CompletableFuture<?>> futures,
                                Material material, String materialName, TagPrefix prefix) {
        if (prefix.getIconType() == null) return;
        String itemId = prefix.formatId(materialName);
        MaterialIconSet iconSet = material.getIconSet();
        if (iconSet == null) return;

        if (prefix.isBlock()) {
            generateBlockModel(cache, futures, itemId, iconSet, prefix.getIconType());
            generateBlockState(cache, futures, itemId);
            generateBlockItemModel(cache, futures, itemId);
        } else {
            generateItemModel(cache, futures, itemId, iconSet, prefix.getIconType());
        }
    }

    /**
     * 鉱石ブロックのモデル・blockstates・アイテムモデルを全石種分生成する。
     *
     * テクスチャパスは "universalmaterials:block/generated/<blockId>" を参照する。
     * このパスは VirtualPackResources → TextureGenerator が実行時に提供する。
     */
    private void generateOreModels(CachedOutput cache, List<CompletableFuture<?>> futures,
                                   Material material, String materialName) {
        var settings = OreRegistry.getSettings(material);
        Set<Ore> registered = new HashSet<>();

        for (DimensionOreConfig config : settings.getOverworld()) collectOres(config, registered);
        for (DimensionOreConfig config : settings.getNether())    collectOres(config, registered);
        for (DimensionOreConfig config : settings.getEnd())       collectOres(config, registered);

        for (Ore ore : registered) {
            String blockId = ore.formatBlockId(materialName);
            generateOreBlockModel(cache, futures, blockId);
            generateBlockState(cache, futures, blockId);
            generateBlockItemModel(cache, futures, blockId);
        }
    }

    /**
     * 鉱石ブロックモデルJSONを生成する。
     *
     * parent: minecraft:block/cube_all（全面同一テクスチャのシンプルなキューブ）
     * textures.all: VirtualPackResourcesが提供する動的生成テクスチャを参照する
     */
    private void generateOreBlockModel(CachedOutput cache, List<CompletableFuture<?>> futures,
                                       String blockId) {
        JsonObject model    = new JsonObject();
        JsonObject textures = new JsonObject();
        model.addProperty("parent", "minecraft:block/cube_all");
        // "block/generated/<blockId>" → VirtualPackResources が提供するパス
        textures.addProperty("all",
                UniversalMaterials.MOD_ID + ":block/generated/" + blockId);
        model.add("textures", textures);

        futures.add(DataProvider.saveStable(cache, model, getBlockModelPath(blockId)));
    }

    private void generateItemModel(CachedOutput cache, List<CompletableFuture<?>> futures,
                                   String itemId, MaterialIconSet iconSet, String iconType) {
        String setName = getRoot(iconSet).getName();
        String base    = UniversalMaterials.MOD_ID + ":item/material_sets/" + setName + "/";

        JsonObject model    = new JsonObject();
        JsonObject textures = new JsonObject();
        model.addProperty("parent", "minecraft:item/generated");
        textures.addProperty("layer0", base + iconType);
        model.add("textures", textures);

        futures.add(DataProvider.saveStable(cache, model, getItemModelPath(itemId)));
    }

    private void generateBlockModel(CachedOutput cache, List<CompletableFuture<?>> futures,
                                    String blockId, MaterialIconSet iconSet, String iconType) {
        String setName = getRoot(iconSet).getName();
        String base    = UniversalMaterials.MOD_ID + ":block/material_sets/" + setName + "/";

        JsonObject model    = new JsonObject();
        JsonObject textures = new JsonObject();
        model.addProperty("parent", UniversalMaterials.MOD_ID + ":block/tinted_cube");
        textures.addProperty("all", base + iconType);
        model.add("textures", textures);

        futures.add(DataProvider.saveStable(cache, model, getBlockModelPath(blockId)));
    }

    private void generateBlockState(CachedOutput cache, List<CompletableFuture<?>> futures,
                                    String blockId) {
        JsonObject blockState = new JsonObject();
        JsonObject variants   = new JsonObject();
        JsonObject variant    = new JsonObject();
        variant.addProperty("model", UniversalMaterials.MOD_ID + ":block/" + blockId);
        variants.add("", variant);
        blockState.add("variants", variants);
        futures.add(DataProvider.saveStable(cache, blockState, getBlockStatePath(blockId)));
    }

    private void generateBlockItemModel(CachedOutput cache, List<CompletableFuture<?>> futures,
                                        String blockId) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", UniversalMaterials.MOD_ID + ":block/" + blockId);
        futures.add(DataProvider.saveStable(cache, model, getItemModelPath(blockId)));
    }

    private static MaterialIconSet getRoot(MaterialIconSet iconSet) {
        MaterialIconSet current = iconSet;
        while (current.getParent() != null) current = current.getParent();
        return current;
    }

    private void collectOres(DimensionOreConfig config, Set<Ore> result) {
        for (StoneGroup group : config.getStones()) {
            for (Ore ore : group.getOres()) {
                if (!config.isExcluded(ore)) result.add(ore);
            }
        }
    }

    private Path getItemModelPath(String id) {
        return output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(UniversalMaterials.MOD_ID + "/models/item/" + id + ".json");
    }

    private Path getBlockModelPath(String id) {
        return output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(UniversalMaterials.MOD_ID + "/models/block/" + id + ".json");
    }

    private Path getBlockStatePath(String id) {
        return output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(UniversalMaterials.MOD_ID + "/blockstates/" + id + ".json");
    }

    @Override
    public String getName() { return "Universal Materials Model Provider"; }
}