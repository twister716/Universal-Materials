package io.github.twister716.universalmaterials.content.item;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * 素材システムが自動生成するブロックのアイテム形式クラス。
 * BlockItemを継承しており、MaterialItemと同じく名前を動的生成する。
 *
 * 例: "Block of %s" + "Tin" → "Block of Tin"
 */
public class MaterialBlockItem extends BlockItem {

    private final Material material;
    private final TagPrefix prefix;

    public MaterialBlockItem(Block block, Material material, TagPrefix prefix, Properties properties) {
        super(block, properties);
        this.material = material;
        this.prefix   = prefix;
    }

    @Override
    public Component getName(ItemStack stack) {
        String prefixKey  = "tagprefix." + UniversalMaterials.MOD_ID + "." + prefix.getId();
        String materialName = material.getId().split(":")[1];
        String materialKey  = "material." + UniversalMaterials.MOD_ID + "." + materialName;

        return Component.translatable(prefixKey,
                Component.translatable(materialKey));
    }

    public Material getMaterial() { return material; }
    public TagPrefix getPrefix()  { return prefix; }
}
