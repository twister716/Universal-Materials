package io.github.twister716.universalmaterials.client;

import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.ore.Ore;
import io.github.twister716.universalmaterials.api.ore.OreRegistry;
import io.github.twister716.universalmaterials.api.ore.OreSettings;
import io.github.twister716.universalmaterials.api.ore.StoneGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 鉱石ブロック・原石アイテム専用のツールチップ処理クラス。
 *
 * Ore info の説明文:
 *   OreSettings.getOreDescription() が非null の場合のみ表示する。
 *   翻訳キーは素材IDを使って "ore.description.<素材ID>" の形式で組み立てる。
 *   鉱石ブロック・原石の両方で同じキーを参照する。
 *   未設定の場合は Ore info 行を表示しない（フォールバックなし）。
 */
public class OreTooltipHandler {

    /**
     * 鉱石ブロックのツールチップを追加する。
     * MaterialOreBlockItemのappendHoverText()から呼ばれる。
     */
    public static void appendOreTooltip(Material material, Ore ore, List<Component> tooltip) {
        boolean shiftDown = Screen.hasShiftDown();
        boolean ctrlDown  = Screen.hasControlDown();

        if (!shiftDown && !ctrlDown) {
            tooltip.add(Component.translatable(
                            "tooltip.universalmaterials.hold_shift",
                            Component.literal("Shift").withStyle(ChatFormatting.YELLOW))
                    .withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            return;
        }

        if (shiftDown) appendOreInfo(material, ore, tooltip);
        if (ctrlDown)  appendOreDetails(material, ore, tooltip);
    }

    /**
     * 原石アイテムのツールチップを追加する。
     * MaterialItemのappendHoverText()からRAW_OREのとき呼ばれる。
     */
    public static void appendRawOreTooltip(Material material, List<Component> tooltip) {
        boolean shiftDown = Screen.hasShiftDown();

        if (!shiftDown) {
            tooltip.add(Component.translatable(
                            "tooltip.universalmaterials.hold_shift",
                            Component.literal("Shift").withStyle(ChatFormatting.YELLOW))
                    .withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            return;
        }

        // 原石は石種情報なし・Ore info + Ore production + JEIヒントのみ
        appendOreInfoCommon(material, tooltip);
    }

    // ===== 鉱石ブロック =====

    private static void appendOreInfo(Material material, Ore ore, List<Component> tooltip) {
        // Ore stone type: 石の種類グループ名（例: Stone / Deepslate）
        StoneGroup group = ore.getStoneGroup();
        if (group != null) {
            tooltip.add(Component.translatable(
                            "tooltip.universalmaterials.ore_stone_type",
                            Component.translatable(
                                    "tooltip.universalmaterials.stone_group." + group.getId()))
                    .withStyle(ChatFormatting.GRAY));
        }

        appendOreInfoCommon(material, tooltip);
    }

    private static void appendOreDetails(Material material, Ore ore, List<Component> tooltip) {
        // 将来拡張用
    }

    // ===== 鉱石・原石共通 =====

    /**
     * Ore info・Ore production・JEIヒントをツールチップに追加する。
     *
     * Ore info の翻訳キーは素材IDから組み立てる。
     * 例: material.getId() が "universalmaterials:tin" → "ore.description.tin"
     */
    private static void appendOreInfoCommon(Material material, List<Component> tooltip) {
        OreSettings settings = OreRegistry.getSettings(material);

        // Ore info: OreSettings.getOreDescription() が設定されている場合のみ表示する
        if (settings != null && settings.getOreDescription() != null) {
            // 翻訳キーを素材IDから組み立てる
            String materialName = material.getId().split(":")[1];
            String oreDescKey   = "ore.description." + materialName;

            tooltip.add(Component.translatable(
                            "tooltip.universalmaterials.ore_info",
                            Component.translatable(oreDescKey))
                    .withStyle(ChatFormatting.GREEN));
        }

        // Ore production: count値から High / Normal / Low を自動判定する
        if (settings != null && !settings.getOverworld().isEmpty()) {
            int count = settings.getOverworld().get(0).getCount();
            tooltip.add(Component.translatable(
                            "tooltip.universalmaterials.ore_production",
                            Component.translatable(getProductionKey(count))
                                    .withStyle(ChatFormatting.BOLD))
                    .withStyle(ChatFormatting.YELLOW));
        }

        // JEI参照のヒント
        tooltip.add(Component.translatable("tooltip.universalmaterials.ore_jei_hint")
                .withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
    }

    private static String getProductionKey(int count) {
        if (count >= 10) return "tooltip.universalmaterials.ore_production.high";
        if (count >= 6)  return "tooltip.universalmaterials.ore_production.normal";
        return "tooltip.universalmaterials.ore_production.low";
    }
}