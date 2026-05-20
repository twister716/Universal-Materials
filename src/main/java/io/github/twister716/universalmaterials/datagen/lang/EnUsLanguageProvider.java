package io.github.twister716.universalmaterials.datagen.lang;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.content.tab.UMCreativeTabs;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * en_us.json（英語翻訳ファイル）を自動生成するProvider。
 *
 * 生成する翻訳キー:
 *   tagprefix.universalmaterials.<id>  → TagPrefixの名前フォーマット（例: "%s Ingot"）
 *   material.universalmaterials.<id>   → 素材の英語名（例: "Tin"）
 *   itemGroup.universalmaterials.<id>  → クリエイティブタブ名（例: "Universal Materials"）
 */
public class EnUsLanguageProvider extends LanguageProvider {

    public EnUsLanguageProvider(PackOutput output) {
        super(output, UniversalMaterials.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        // ===== クリエイティブタブ名 =====
        add("itemGroup." + UniversalMaterials.MOD_ID + ".materials",  "Universal Materials: Materials");
        add("itemGroup." + UniversalMaterials.MOD_ID + ".equipment",  "Universal Materials: Blocks");

        // ===== TagPrefixのフォーマット文字列 =====
        // キー例: "tagprefix.universalmaterials.ingot" → "%s Ingot"
        for (TagPrefix prefix : TagPrefix.getRegistry().values()) {
            add("tagprefix." + UniversalMaterials.MOD_ID + "." + prefix.getId(),
                    prefix.getEnFormat());
        }

        // ===== 素材名 =====
        // キー例: "material.universalmaterials.tin" → "Tin"
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            String materialName = material.getId().split(":")[1];
            add("material." + UniversalMaterials.MOD_ID + "." + materialName,
                    material.getEnglishName());
        }
    }
}
