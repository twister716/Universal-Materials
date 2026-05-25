package io.github.twister716.universalmaterials.datagen.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.ore.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 鉱石生成に必要な3種のJSONファイルを自動生成するProvider。
 *
 * 1つの素材×ディメンション設定ごとに3ファイルが生成される：
 *
 *   configured_feature/<id>.json
 *     → 鉱石ブロックの種類とサイズを定義する
 *
 *   placed_feature/<id>.json
 *     → 高度・頻度・分布タイプを定義する
 *
 *   neoforge/biome_modifier/<id>.json
 *     → どのバイオーム（ディメンション）に生成するかを定義する
 *
 * OreRegistry に登録された全素材を走査して自動生成する。
 */
public class MaterialWorldGenProvider implements DataProvider {

    private final PackOutput output;

    public MaterialWorldGenProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Map.Entry<Material, OreSettings> entry : OreRegistry.getAll().entrySet()) {
            Material material    = entry.getKey();
            OreSettings settings = entry.getValue();
            String materialId    = material.getId().split(":")[1];

            // オーバーワールドの設定を順番に処理する
            // 複数の高度範囲を設定した場合は overworld_0.json, overworld_1.json... と連番になる
            List<DimensionOreConfig> overworldConfigs = settings.getOverworld();
            for (int i = 0; i < overworldConfigs.size(); i++) {
                generateOreJson(cache, futures, material, materialId, overworldConfigs.get(i), settings,
                        "overworld_" + i, "#minecraft:is_overworld");
            }

            List<DimensionOreConfig> netherConfigs = settings.getNether();
            for (int i = 0; i < netherConfigs.size(); i++) {
                generateOreJson(cache, futures, material, materialId, netherConfigs.get(i), settings,
                        "nether_" + i, "#minecraft:is_nether");
            }

            List<DimensionOreConfig> endConfigs = settings.getEnd();
            for (int i = 0; i < endConfigs.size(); i++) {
                generateOreJson(cache, futures, material, materialId, endConfigs.get(i), settings,
                        "end_" + i, "#minecraft:is_end");
            }
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * 1つのDimensionOreConfigに対して3つのJSONファイルを生成する。
     *
     * @param material        対象素材
     * @param materialId      素材のID（名前空間なし。例: "tin"）
     * @param config          1ディメンション・1高度範囲の設定
     * @param dimensionSuffix JSONファイル名のサフィックス（例: "overworld_0"）
     * @param defaultBiomeTag バイオーム指定なしのときに使うタグ（例: "#minecraft:is_overworld"）
     */
    private void generateOreJson(CachedOutput cache, List<CompletableFuture<?>> futures,
                                 Material material, String materialId,
                                 DimensionOreConfig config,
                                 OreSettings settings,
                                 String dimensionSuffix, String defaultBiomeTag) {
        // JSONファイルのベースID（例: "tin_ore_overworld_0"）
        String id = materialId + "_ore_" + dimensionSuffix;

        futures.add(DataProvider.saveStable(cache,
                buildConfiguredFeature(material, materialId, config, settings),
                getConfiguredFeaturePath(id)));

        futures.add(DataProvider.saveStable(cache,
                buildPlacedFeature(id, config),
                getPlacedFeaturePath(id)));

        futures.add(DataProvider.saveStable(cache,
                buildBiomeModifier(id, config.getBiomes(), defaultBiomeTag),
                getBiomeModifierPath(id)));
    }

    /**
     * configured_feature のJSONを構築する。
     * どの鉱石ブロックをどの石ブロックに置き換えるか（targets）と塊のサイズを定義する。
     */
    private JsonObject buildConfiguredFeature(Material material, String materialId,
                                              DimensionOreConfig config,
                                              OreSettings settings) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:ore");

        JsonObject featureConfig = new JsonObject();
        featureConfig.addProperty("size", config.getSize());
        featureConfig.addProperty("discard_chance_on_air_exposure", 0.0f);

        JsonArray targets = new JsonArray();
        Set<Ore> registered = new HashSet<>();

        for (StoneGroup group : config.getStones()) {
            for (Ore ore : group.getOres()) {
                if (!registered.add(ore)) continue;
                if (config.isExcluded(ore)) continue;
                // vanillaStoneOres()で指定した石種はバニラが担当するため除外する
                if (settings.getVanillaBaseOres().contains(ore)) continue;

                // 置き換え先の鉱石ブロックID（例: "universalmaterials:deepslate_tin_ore"）
                String oreBlockId = UniversalMaterials.MOD_ID + ":"
                        + ore.formatBlockId(materialId);

                // 置き換え元のバニラブロックID（例: "minecraft:deepslate"）
                String baseBlockId = BuiltInRegistries.BLOCK
                        .getKey(ore.getBaseBlock()).toString();

                JsonObject target = new JsonObject();

                // target: どのブロックを検索するか
                JsonObject predicate = new JsonObject();
                predicate.addProperty("predicate_type", "minecraft:block_match");
                predicate.addProperty("block", baseBlockId);
                target.add("target", predicate);

                // state: 置き換え後のブロック
                JsonObject state = new JsonObject();
                state.addProperty("Name", oreBlockId);
                target.add("state", state);

                targets.add(target);
            }
        }

        featureConfig.add("targets", targets);
        root.add("config", featureConfig);
        return root;
    }

    /**
     * placed_feature のJSONを構築する。
     * 高度範囲・生成頻度・分布タイプを定義する。
     */
    private JsonObject buildPlacedFeature(String id, DimensionOreConfig config) {
        JsonObject root = new JsonObject();
        root.addProperty("feature", UniversalMaterials.MOD_ID + ":" + id);

        JsonArray placement = new JsonArray();

        // チャンクあたりの生成試行回数
        JsonObject count = new JsonObject();
        count.addProperty("type", "minecraft:count");
        count.addProperty("count", config.getCount());
        placement.add(count);

        // チャンク内でランダムなX・Z座標にばらつかせる
        JsonObject inSquare = new JsonObject();
        inSquare.addProperty("type", "minecraft:in_square");
        placement.add(inSquare);

        // 高度範囲と分布タイプ
        placement.add(buildHeightRange(config));

        // バイオームフィルター（biomes指定がある場合にのみ有効）
        JsonObject biome = new JsonObject();
        biome.addProperty("type", "minecraft:biome");
        placement.add(biome);

        root.add("placement", placement);
        return root;
    }

    /** height_range modifier のJSONを構築するヘルパー */
    private JsonObject buildHeightRange(DimensionOreConfig config) {
        JsonObject heightRange = new JsonObject();
        heightRange.addProperty("type", "minecraft:height_range");

        JsonObject height = new JsonObject();
        height.addProperty("type", config.getHeightType().getId());

        JsonObject minInclusive = new JsonObject();
        minInclusive.addProperty("absolute", config.getMinHeight());
        height.add("min_inclusive", minInclusive);

        JsonObject maxInclusive = new JsonObject();
        maxInclusive.addProperty("absolute", config.getMaxHeight());
        height.add("max_inclusive", maxInclusive);

        heightRange.add("height", height);
        return heightRange;
    }

    /**
     * biome_modifier のJSONを構築する。
     * バイオーム指定がある場合はそれを使い、ない場合はデフォルトのディメンションタグを使う。
     */
    private JsonObject buildBiomeModifier(String id, List<ResourceKey<Biome>> biomes,
                                          String defaultBiomeTag) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "neoforge:add_features");

        if (biomes.isEmpty()) {
            // バイオーム指定なし → ディメンション全体に生成（タグ指定）
            root.addProperty("biomes", defaultBiomeTag);
        } else if (biomes.size() == 1) {
            // バイオーム1つ → 文字列で指定
            root.addProperty("biomes", biomes.get(0).location().toString());
        } else {
            // バイオーム複数 → 配列で指定
            JsonArray biomeArray = new JsonArray();
            for (ResourceKey<Biome> biome : biomes) {
                biomeArray.add(biome.location().toString());
            }
            root.add("biomes", biomeArray);
        }

        root.addProperty("features", UniversalMaterials.MOD_ID + ":" + id);
        root.addProperty("step", "underground_ores");
        return root;
    }

    // ===== JSONファイルの出力パスを返すヘルパーメソッド =====

    private Path getConfiguredFeaturePath(String id) {
        return output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(UniversalMaterials.MOD_ID
                        + "/worldgen/configured_feature/" + id + ".json");
    }

    private Path getPlacedFeaturePath(String id) {
        return output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(UniversalMaterials.MOD_ID
                        + "/worldgen/placed_feature/" + id + ".json");
    }

    private Path getBiomeModifierPath(String id) {
        return output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(UniversalMaterials.MOD_ID
                        + "/neoforge/biome_modifier/" + id + ".json");
    }

    @Override
    public String getName() {
        return "Universal Materials World Gen Provider";
    }
}