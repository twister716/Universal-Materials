package io.github.twister716.universalmaterials.datagen.lang;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * en_us.json（英語翻訳ファイル）を自動生成するProvider。
 *
 * 生成する翻訳キー:
 *   tagprefix.universalmaterials.<id>        → TagPrefixの名前フォーマット
 *   material.universalmaterials.<id>         → 素材のアイテム名用（englishName）
 *   material.universalmaterials.<id>.local   → Local MatName用（初期値はfullName）
 *   material.description.<id>               → 素材の説明文
 *   itemGroup.universalmaterials.<id>        → クリエイティブタブ名
 *   tooltip.universalmaterials.*             → ツールチップ用キー
 *
 * English MatNameはツールチップでハードコード表示するため翻訳キーを追加しない。
 * Local MatNameの初期値はfullName（未設定ならenglishName）と同じ値にする。
 * 翻訳者はlocal.jsonでこのキーを上書きして各言語の素材名を設定する。
 */
public class EnUsLanguageProvider extends LanguageProvider {

    public EnUsLanguageProvider(PackOutput output) {
        super(output, UniversalMaterials.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        // ===== クリエイティブタブ名 =====
        add("itemGroup." + UniversalMaterials.MOD_ID + ".materials", "Universal Materials: Materials");
        add("itemGroup." + UniversalMaterials.MOD_ID + ".equipment", "Universal Materials: Ores");

        // ===== ツールチップ =====
        add("tooltip." + UniversalMaterials.MOD_ID + ".hold_shift",       "Hold [%s] for material info.");
        add("tooltip." + UniversalMaterials.MOD_ID + ".hold_ctrl",        "Hold [%s] for details.");
        add("tooltip." + UniversalMaterials.MOD_ID + ".english_mat_name", "English MatName: §l%s");
        add("tooltip." + UniversalMaterials.MOD_ID + ".local_mat_name",   "Local MatName: §l%s");
        add("tooltip." + UniversalMaterials.MOD_ID + ".atomic_number",    "Atomic number: §l%s");
        add("tooltip." + UniversalMaterials.MOD_ID + ".element_symbol",   "Element symbol: §l%s");
        add("tooltip." + UniversalMaterials.MOD_ID + ".material_info",    "Material info: §l%s");

        add("tooltip." + UniversalMaterials.MOD_ID + ".compat_material",  "%s Compat Material");

        // ===== TagPrefixのフォーマット文字列 =====
        for (TagPrefix prefix : TagPrefix.getRegistry().values()) {
            add("tagprefix." + UniversalMaterials.MOD_ID + "." + prefix.getId(),
                    prefix.getEnFormat());
        }

        // ===== 素材名・Local MatName・説明文 =====
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            String materialName = material.getId().split(":")[1];

            // アイテム名用（englishName）
            add("material." + UniversalMaterials.MOD_ID + "." + materialName,
                    material.getEnglishName());

            // Local MatName用（初期値はfullName。翻訳者がja_jp.json等で上書きする）
            add("material." + UniversalMaterials.MOD_ID + "." + materialName + ".local",
                    material.getFullName());

            // 素材の説明文（description()が設定されている場合のみ）
            if (material.getDescription() != null) {
                add("material.description." + materialName, material.getDescription());
            }
        }
    }
}
