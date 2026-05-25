package io.github.twister716.universalmaterials.datagen.lang;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.api.ore.Ore;
import io.github.twister716.universalmaterials.api.ore.OreRegistry;
import io.github.twister716.universalmaterials.api.ore.StoneGroups;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * en_us.json（英語翻訳ファイル）を自動生成するProvider。
 *
 * 生成する翻訳キー:
 *   tagprefix.universalmaterials.<id>     → TagPrefixの名前フォーマット
 *   ore.universalmaterials.<oreId>        → 鉱石名フォーマット（例: "Deepslate %s Ore"）
 *   ore.description.<素材ID>             → 鉱石・原石の説明文（OreSettings.oreDesc()で設定）
 *   material.universalmaterials.<id>     → 素材のアイテム名用
 *   material.universalmaterials.<id>.local → Local MatName用
 *   material.description.<id>            → 素材の説明文
 *   itemGroup.universalmaterials.<id>    → クリエイティブタブ名
 *   tooltip.universalmaterials.*         → ツールチップ用キー
 *   tooltip.universalmaterials.stone_group.* → 石グループ名
 */
public class EnUsLanguageProvider extends LanguageProvider {

    public EnUsLanguageProvider(PackOutput output) {
        super(output, UniversalMaterials.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        // ===== クリエイティブタブ名 =====
        add("itemGroup." + UniversalMaterials.MOD_ID + ".materials", "Universal Materials: Materials");
        add("itemGroup." + UniversalMaterials.MOD_ID + ".ores", "Universal Materials: Ores");

        // ===== 素材部品ツールチップ =====
        add("tooltip." + UniversalMaterials.MOD_ID + ".hold_shift",       "Hold [%s] for material info.");
        add("tooltip." + UniversalMaterials.MOD_ID + ".hold_ctrl",        "Hold [%s] for details.");
        add("tooltip." + UniversalMaterials.MOD_ID + ".english_mat_name", "English MatName: §l%s");
        add("tooltip." + UniversalMaterials.MOD_ID + ".local_mat_name",   "Local MatName: §l%s");
        add("tooltip." + UniversalMaterials.MOD_ID + ".atomic_number",    "Atomic number: §l%s");
        add("tooltip." + UniversalMaterials.MOD_ID + ".element_symbol",   "Element symbol: §l%s");
        add("tooltip." + UniversalMaterials.MOD_ID + ".material_info",    "Material info: §l%s");
        add("tooltip." + UniversalMaterials.MOD_ID + ".compat_material",  "%s Compat Material");

        // ===== 鉱石・原石ツールチップ =====
        add("tooltip." + UniversalMaterials.MOD_ID + ".ore_stone_type",        "Ore stone type: §l%s");
        add("tooltip." + UniversalMaterials.MOD_ID + ".ore_info",              "Ore info: §l%s");
        add("tooltip." + UniversalMaterials.MOD_ID + ".ore_production",        "Ore production: %s");
        add("tooltip." + UniversalMaterials.MOD_ID + ".ore_production.high",   "High");
        add("tooltip." + UniversalMaterials.MOD_ID + ".ore_production.normal", "Normal");
        add("tooltip." + UniversalMaterials.MOD_ID + ".ore_production.low",    "Low");
        add("tooltip." + UniversalMaterials.MOD_ID + ".ore_jei_hint",
                "Refer to JEI for ore formation altitude.");

        // ===== 石グループ名 =====
        add("tooltip." + UniversalMaterials.MOD_ID + ".stone_group." + StoneGroups.STONE.getId(),
                "Stone");
        add("tooltip." + UniversalMaterials.MOD_ID + ".stone_group." + StoneGroups.DEEPSLATE.getId(),
                "Deepslate");
        add("tooltip." + UniversalMaterials.MOD_ID + ".stone_group." + StoneGroups.NETHER.getId(),
                "Nether");
        add("tooltip." + UniversalMaterials.MOD_ID + ".stone_group." + StoneGroups.END.getId(),
                "End");

        // ===== TagPrefixのフォーマット文字列 =====
        for (TagPrefix prefix : TagPrefix.getRegistry().values()) {
            add("tagprefix." + UniversalMaterials.MOD_ID + "." + prefix.getId(),
                    prefix.getEnFormat());
        }

        // ===== Oreの名前フォーマット =====
        for (Ore ore : Ore.getAllOres()) {
            add("ore." + UniversalMaterials.MOD_ID + "." + ore.getId(), ore.getEnFormat());
        }

        // ===== 素材名・説明文 + 鉱石説明文 =====
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            String materialName = material.getId().split(":")[1];

            add("material." + UniversalMaterials.MOD_ID + "." + materialName,
                    material.getEnglishName());

            add("material." + UniversalMaterials.MOD_ID + "." + materialName + ".local",
                    material.getFullName());

            if (material.getDescription() != null) {
                add("material.description." + materialName, material.getDescription());
            }

            // 鉱石・原石説明文: OreSettings.oreDesc() が設定されている場合のみ登録する
            // 翻訳キー "ore.description.<素材ID>" に説明文を登録する
            if (OreRegistry.hasSettings(material)) {
                var oreSettings = OreRegistry.getSettings(material);
                if (oreSettings.getOreDescription() != null) {
                    add("ore.description." + materialName, oreSettings.getOreDescription());
                }
            }
        }
    }
}