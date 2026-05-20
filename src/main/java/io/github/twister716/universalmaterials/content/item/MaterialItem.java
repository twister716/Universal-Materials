package io.github.twister716.universalmaterials.content.item;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 素材システムが自動生成するアイテムのクラス。
 * 通常の Item と違い、アイテム名を翻訳キーの組み合わせで動的に生成する。
 *
 * 名前の生成ルール:
 *   "tagprefix.universalmaterials.ingot" = "%s Ingot"
 *   "material.universalmaterials.tin"   = "Tin"
 *   → Component.translatable("%s Ingot", "Tin") → "Tin Ingot"
 *
 * これにより素材を追加するだけでアイテム名が自動的に決まる。
 * このクラス自体は変更不要。
 */
public class MaterialItem extends Item {

    // このアイテムが属する素材（例: TIN）
    private final Material material;

    // このアイテムの形状（例: TagPrefixes.INGOT）
    private final TagPrefix prefix;

    public MaterialItem(Material material, TagPrefix prefix, Properties properties) {
        super(properties);
        this.material = material;
        this.prefix   = prefix;
    }

    /**
     * アイテム名を動的に生成して返す。
     *
     * 手順:
     * 1. TagPrefixの翻訳キーからフォーマット文字列を取得する（例: "%s Ingot"）
     * 2. 素材の翻訳キーをComponent.translatableで包む（例: Component("Tin")）
     * 3. フォーマットに素材コンポーネントを埋め込んで完成（例: "Tin Ingot"）
     */
    @Override
    public Component getName(ItemStack stack) {
        // TagPrefixの翻訳キー: "tagprefix.universalmaterials.ingot"
        String prefixKey = "tagprefix." + UniversalMaterials.MOD_ID + "." + prefix.getId();

        // 素材名の翻訳キー: "material.universalmaterials.tin"
        String materialName = material.getId().split(":")[1];
        String materialKey  = "material." + UniversalMaterials.MOD_ID + "." + materialName;

        // "%s Ingot" の %s に Component.translatable("...tin") を埋め込む
        // Component.translatableWithFallback は翻訳がなければfallback文字列を使う
        return Component.translatable(prefixKey,
                Component.translatable(materialKey));
    }

    public Material getMaterial() { return material; }
    public TagPrefix getPrefix()  { return prefix; }
}
