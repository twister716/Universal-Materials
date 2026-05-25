package io.github.twister716.universalmaterials.content.item;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.ore.Ore;
import io.github.twister716.universalmaterials.client.OreTooltipHandler;
import io.github.twister716.universalmaterials.content.block.MaterialOreBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * 鉱石ブロック専用のBlockItem。
 *
 * 通常のBlockItem（インベントリに入るアイテム形態）を継承し、
 * 鉱石専用のツールチップをOreTooltipHandlerで追加する。
 *
 * アイテム名は "Deepslate Tin Ore" のように石種 + 素材名 + "Ore" の形式で
 * 翻訳キーの組み合わせで動的に生成する。
 */
public class MaterialOreBlockItem extends BlockItem {

    private final Material material;
    private final Ore ore;

    public MaterialOreBlockItem(Block block, Material material, Ore ore, Properties properties) {
        super(block, properties);
        this.material = material;
        this.ore      = ore;
    }

    /**
     * アイテム名を翻訳キーの組み合わせで動的に生成する。
     *
     * 翻訳キー構造:
     *   ore.universalmaterials.<oreId>  → "Deepslate %s Ore"  （例: "Deepslate {0} Ore"）
     *   material.universalmaterials.<materialId> → "Tin"
     *
     * 結果: "Deepslate Tin Ore"
     */
    @Override
    public Component getName(ItemStack stack) {
        String oreKey      = "ore." + UniversalMaterials.MOD_ID + "." + ore.getId();
        String materialId  = material.getId().split(":")[1];
        String materialKey = "material." + UniversalMaterials.MOD_ID + "." + materialId;
        return Component.translatable(oreKey, Component.translatable(materialKey));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        OreTooltipHandler.appendOreTooltip(material, ore, tooltip);
    }

    public Material getMaterial() { return material; }
    public Ore getOre()           { return ore; }
}