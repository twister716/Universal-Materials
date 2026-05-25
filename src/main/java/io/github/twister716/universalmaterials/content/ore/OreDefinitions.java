package io.github.twister716.universalmaterials.content.ore;

import io.github.twister716.universalmaterials.api.ore.*;
import io.github.twister716.universalmaterials.content.material.UMMetalMaterials;
import io.github.twister716.universalmaterials.content.material.UMVanillaMaterials;

import static io.github.twister716.universalmaterials.api.ore.DimensionOreConfig.HeightType.*;

/**
 * 素材と鉱石生成設定（OreSettings）を結びつける定数置き場。
 *
 * Material自体にはOreSettingsを持たせず、このファイルで分離管理する。
 * これにより Material.Builder の肥大化を防ぐ。
 *
 * 各素材の生成設定を定義し、OreRegistry.register() で登録する。
 * このクラスの init() は UniversalMaterials のコンストラクタから呼ぶ。
 */
public class OreDefinitions {

    public static void init() {

        OreRegistry.register(UMVanillaMaterials.IRON, new OreSettings.Builder()
                .overworld(new DimensionOreConfig()
                        .height(-64, 72).size(9).count(12)
                        .heightType(TRAPEZOID)
                        .stones(StoneGroups.STONE, StoneGroups.DEEPSLATE))
                .vanillaStoneOres(OreTypes.STONE, OreTypes.DEEPSLATE)
                .noColorize()
                .build());

        OreRegistry.register(UMVanillaMaterials.GOLD, new OreSettings.Builder()
                .overworld(new DimensionOreConfig()
                        .height(-64, 72).size(9).count(12)
                        .heightType(TRAPEZOID)
                        .stones(StoneGroups.STONE, StoneGroups.DEEPSLATE))
                .vanillaStoneOres(OreTypes.STONE, OreTypes.DEEPSLATE)
                .noColorize()
                .build());

        OreRegistry.register(UMVanillaMaterials.COPPER, new OreSettings.Builder()
                .overworld(new DimensionOreConfig()
                        .height(-64, 72).size(9).count(12)
                        .heightType(TRAPEZOID)
                        .stones(StoneGroups.STONE, StoneGroups.DEEPSLATE))
                .vanillaStoneOres(OreTypes.STONE, OreTypes.DEEPSLATE)
                .noColorize()
                .build());

        // ===== 錫（Tin） =====
        // y=-64〜72、STONE+DEEPSLATEグループ、原石を1〜2個ドロップ（幸運で増加）
        OreRegistry.register(UMMetalMaterials.TIN, new OreSettings.Builder()

                .overworld(new DimensionOreConfig()
                        .height(-64, 72)
                        .size(9)
                        .count(8)
                        .heightType(TRAPEZOID)
                        .stones(StoneGroups.STONE, StoneGroups.DEEPSLATE))

                .drop(1, 2)
                .oreColor(0xcbeaed, 0x909d9e)
                .oreDesc("A soft, malleable metal ore found in stone.")
                .build());

        // ===== 鉛（Lead） =====
        // y=-64〜16、深めに生成、原石を1〜2個ドロップ
        OreRegistry.register(UMMetalMaterials.LEAD, new OreSettings.Builder()
                .overworld(new DimensionOreConfig()
                        .height(-64, 16)
                        .size(8)
                        .count(6)
                        .heightType(TRAPEZOID)
                        .stones(StoneGroups.STONE, StoneGroups.DEEPSLATE))
                .drop(1, 2)
                .build());

        // ===== 銀（Silver） =====
        // y=-64〜0、かなり深い場所にのみ生成、原石を1〜2個ドロップ
        OreRegistry.register(UMMetalMaterials.SILVER, new OreSettings.Builder()
                .overworld(new DimensionOreConfig()
                        .height(-64, 0)
                        .size(7)
                        .count(4)
                        .heightType(UNIFORM)
                        .stones(StoneGroups.STONE, StoneGroups.DEEPSLATE))
                .drop(1, 2)
                .build());
    }

    private OreDefinitions() {}
}