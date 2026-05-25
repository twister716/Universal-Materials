package io.github.twister716.universalmaterials.content.item;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefixes;
import io.github.twister716.universalmaterials.client.OreTooltipHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;

import java.util.List;

/**
 * 素材システムが自動生成するアイテムのクラス。
 * アイテム名は翻訳キーの組み合わせで動的に生成する。
 *
 * ツールチップの振り分け:
 *   prefix == RAW_ORE → OreTooltipHandler.appendRawOreTooltip()（原石専用）
 *   それ以外           → appendHoverTextForMaterial()（素材汎用）
 */
public class MaterialItem extends Item {

    private final Material material;
    private final TagPrefix prefix;

    public MaterialItem(Material material, TagPrefix prefix, Properties properties) {
        super(properties);
        this.material = material;
        this.prefix   = prefix;
    }

    @Override
    public Component getName(ItemStack stack) {
        String prefixKey    = "tagprefix." + UniversalMaterials.MOD_ID + "." + prefix.getId();
        String materialName = material.getId().split(":")[1];
        String materialKey  = "material." + UniversalMaterials.MOD_ID + "." + materialName;
        return Component.translatable(prefixKey, Component.translatable(materialKey));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        // RAW_OREは鉱石専用ツールチップ（石種なし）を使う
        if (prefix == TagPrefixes.RAW_ORE) {
            OreTooltipHandler.appendRawOreTooltip(material, tooltip);
            return;
        }
        appendHoverTextForMaterial(material, tooltip,
                Screen.hasShiftDown(), Screen.hasControlDown());
    }

    /**
     * Shift/Ctrl判定込みの素材ツールチップ追加処理。
     * MaterialItem・MaterialBlockItem・MaterialTooltipHandler（バニラ用）から呼ばれる。
     */
    public static void appendHoverTextForMaterial(Material material, List<Component> tooltip,
                                                  boolean shiftDown, boolean ctrlDown) {
        if (!shiftDown && !ctrlDown) {
            tooltip.add(Component.translatable(
                            "tooltip.universalmaterials.hold_shift",
                            Component.literal("Shift").withStyle(ChatFormatting.YELLOW))
                    .withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            return;
        }

        if (shiftDown) {
            appendMaterialTooltip(material, tooltip);
        }
    }

    public static void appendMaterialTooltip(Material material, List<Component> tooltip) {
        tooltip.add(Component.translatable(
                        "tooltip.universalmaterials.english_mat_name",
                        Component.literal(material.getFullName())
                                .withStyle(ChatFormatting.BOLD))
                .withStyle(ChatFormatting.DARK_AQUA));

        var minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            String langCode = minecraft.getLanguageManager().getSelected();
            if (!langCode.startsWith("en_")) {
                tooltip.add(Component.translatable(
                                "tooltip.universalmaterials.local_mat_name",
                                Component.translatable(material.getLocalNameKey())
                                        .withStyle(ChatFormatting.BOLD))
                        .withStyle(ChatFormatting.AQUA));
            }
        }

        if (!material.isAlloyOrCompound()) {
            tooltip.add(Component.translatable(
                            "tooltip.universalmaterials.atomic_number",
                            Component.literal(String.valueOf(material.getAtomicNumber()))
                                    .withStyle(ChatFormatting.BOLD))
                    .withStyle(ChatFormatting.DARK_PURPLE));

            if (material.getElementSymbol() != null) {
                tooltip.add(Component.translatable(
                                "tooltip.universalmaterials.element_symbol",
                                Component.literal(material.getElementSymbol())
                                        .withStyle(ChatFormatting.BOLD))
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }

        String descKey = material.getDescriptionKey();
        if (descKey != null) {
            tooltip.add(Component.translatable(
                            "tooltip.universalmaterials.material_info",
                            Component.translatable(descKey)
                                    .withStyle(ChatFormatting.BOLD))
                    .withStyle(ChatFormatting.GREEN));
        }

        if (material.getCompatName() != null
                && material.getCompatModId() != null
                && ModList.get().isLoaded(material.getCompatModId())) {
            tooltip.add(Component.translatable(
                            "tooltip.universalmaterials.compat_material",
                            Component.literal(material.getCompatName()).withStyle(ChatFormatting.GOLD))
                    .withStyle(ChatFormatting.YELLOW).withStyle(ChatFormatting.BOLD));
        }
    }

    public Material getMaterial() { return material; }
    public TagPrefix getPrefix()  { return prefix; }
}