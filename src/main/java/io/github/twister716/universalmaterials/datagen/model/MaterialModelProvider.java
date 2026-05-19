package io.github.twister716.universalmaterials.datagen.model;

import com.google.gson.JsonObject;
import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.BlockModelType;
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
 * アイテム: minecraft:item/generated を親とするシンプルなモデル
 * キューブブロック: minecraft:block/cube_all を親とするモデル
 * 鉱石ブロック: cube_all（テクスチャはランタイムでVirtualPackResourcesが合成）
 *
 * テクスチャパスは "universalmaterials:item/generated/<id>" の形式になる。
 * 実際のテクスチャはVirtualPackResources（ランタイム着色）が供給する。
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
        String itemId = prefix.formatId(materialName);
        if (prefix.isBlock()) {
            generateBlockModel(cache, futures, itemId, prefix.getBlockModelType());
            generateBlockState(cache, futures, itemId);
            generateBlockItemModel(cache, futures, itemId);
        } else {
            generateItemModel(cache, futures, itemId);
        }
    }

    // アイテムモデルJSON: minecraft:item/generated を親にして
    // テクスチャを "universalmaterials:item/generated/<id>" に指定する
    private void generateItemModel(CachedOutput cache, List<CompletableFuture<?>> futures,
                                   String itemId) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", UniversalMaterials.MOD_ID + ":item/generated/" + itemId);
        model.add("textures", textures);
        futures.add(DataProvider.saveStable(cache, model, getItemModelPath(itemId)));
    }

    // ブロックモデルJSON: minecraft:block/cube_all を親にして全面同じテクスチャを指定する
    private void generateBlockModel(CachedOutput cache, List<CompletableFuture<?>> futures,
                                    String blockId, BlockModelType type) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:block/cube_all");
        JsonObject textures = new JsonObject();
        // ORE型もCUBE型も同じcube_allを使う
        // 鉱石テクスチャの合成はVirtualPackResourcesが行うのでモデルJSONは同じでよい
        textures.addProperty("all", UniversalMaterials.MOD_ID + ":block/generated/" + blockId);
        model.add("textures", textures);
        futures.add(DataProvider.saveStable(cache, model, getBlockModelPath(blockId)));
    }

    // blockstatesJSON: バリアントなし（""）で単一モデルを指定する
    private void generateBlockState(CachedOutput cache, List<CompletableFuture<?>> futures,
                                    String blockId) {
        JsonObject blockState = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject variant = new JsonObject();
        variant.addProperty("model", UniversalMaterials.MOD_ID + ":block/" + blockId);
        variants.add("", variant);
        blockState.add("variants", variants);
        futures.add(DataProvider.saveStable(cache, blockState, getBlockStatePath(blockId)));
    }

    // ブロックのアイテムモデル: ブロックモデルを親として継承するだけ
    private void generateBlockItemModel(CachedOutput cache, List<CompletableFuture<?>> futures,
                                        String blockId) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", UniversalMaterials.MOD_ID + ":block/" + blockId);
        futures.add(DataProvider.saveStable(cache, model, getItemModelPath(blockId)));
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
    public String getName() {
        return "Universal Materials Model Provider";
    }
}
