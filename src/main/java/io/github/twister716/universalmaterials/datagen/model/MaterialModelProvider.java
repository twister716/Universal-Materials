package io.github.twister716.universalmaterials.datagen.model;

import com.google.gson.JsonObject;
import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.iconset.MaterialIconSet;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * アイテム・ブロックのモデルJSONとblockstatesJSONを自動生成するProvider。
 *
 * テクスチャパスはルートIconSetを基準にする。
 * 例: DULL（親=METALLIC）のインゴット → "material_sets/metallic/ingot"
 *
 * DULLのテクスチャをMETALLICから差し替えたい場合は
 * dull/ingot.png を置けばMinecraftのリソースパック機構が自動的に優先してくれる。
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
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private void generateModels(CachedOutput cache, List<CompletableFuture<?>> futures,
                                Material material, String materialName, TagPrefix prefix) {
        if (prefix.getIconType() == null) return;

        String itemId   = prefix.formatId(materialName);
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
     * アイテムモデルJSONを生成する。
     *
     * テクスチャパスはルートIconSetを基準にする。
     * 例: DULL（親=METALLIC）→ "material_sets/metallic/ingot"
     *
     * DULLのテクスチャを差し替えたい場合は dull/ingot.png を置けば
     * Minecraftのリソースパック機構が自動的に優先する。
     */
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

    /**
     * ブロックモデルJSONを生成する。
     * テクスチャパスはルートIconSetを基準にする。
     */
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

    /**
     * IconSetの親チェーンを辿ってルート（parentがnull）を返す。
     * 例: DULL → METALLIC（ルート）
     */
    private static MaterialIconSet getRoot(MaterialIconSet iconSet) {
        MaterialIconSet current = iconSet;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current;
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