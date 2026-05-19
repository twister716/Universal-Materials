package io.github.twister716.universalmaterials.datagen.lang;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * en_us.json（英語翻訳ファイル）を自動生成するProvider。
 *
 * 全登録素材を走査し、partFlags・impliedFlagsに応じた
 * アイテム名・ブロック名の翻訳キーを書き出す。
 *
 * 翻訳キーの形式: item.universalmaterials.<アイテムID>
 * 例: "item.universalmaterials.tin_ingot" → "Tin Ingot"
 */
public class EnUsLanguageProvider extends LanguageProvider {

    public EnUsLanguageProvider(PackOutput output) {
        super(output, UniversalMaterials.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            // 素材名の最初の文字だけ取り出してModIDを除くため":"の後ろをとる
            // 例: "universalmaterials:tin" → "tin"
            String materialName = material.getId().split(":")[1];

            // partFlagsのアイテム・ブロック名を登録する
            for (MaterialFlag flag : material.getPartFlags()) {
                flag.getTagPrefix().ifPresent(prefix ->
                        addTranslation(material, materialName, prefix));
            }

            // impliedFlagsは IS_VANILLA でなければ登録する
            if (!material.hasPropertyFlag(MaterialFlags.IS_VANILLA)) {
                for (MaterialFlag flag : material.getImpliedFlags()) {
                    flag.getTagPrefix().ifPresent(prefix ->
                            addTranslation(material, materialName, prefix));
                }
            }
        }
    }

    /**
     * 1つのTagPrefixに対して翻訳キーを登録する。
     * ブロックとアイテムで翻訳キーのプレフィックスが違う（block. vs item.）。
     */
    private void addTranslation(Material material, String materialName, TagPrefix prefix) {
        // アイテムID（例: "tin_ingot"）
        String itemId = prefix.formatId(materialName);
        // 英語表示名（例: "Tin Ingot"）
        String enName = prefix.formatEnName(material.getEnglishName());

        if (prefix.isBlock()) {
            // ブロックの翻訳キーは "block.modid.blockid"
            add("block." + UniversalMaterials.MOD_ID + "." + itemId, enName);
        } else {
            // アイテムの翻訳キーは "item.modid.itemid"
            add("item." + UniversalMaterials.MOD_ID + "." + itemId, enName);
        }
    }
}
