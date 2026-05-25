package io.github.twister716.universalmaterials.client;

import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.api.ore.DimensionOreConfig;
import io.github.twister716.universalmaterials.api.ore.Ore;
import io.github.twister716.universalmaterials.api.ore.OreRegistry;
import io.github.twister716.universalmaterials.api.ore.StoneGroup;
import io.github.twister716.universalmaterials.content.item.MaterialBlockItem;
import io.github.twister716.universalmaterials.content.item.MaterialItem;
import io.github.twister716.universalmaterials.content.item.MaterialOreBlockItem;
import io.github.twister716.universalmaterials.content.item.UMItems;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * 素材アイテム・ブロックのカラーハンドラーを登録するクラス。
 * グレースケールテクスチャに素材の色を乗算して着色する。
 *
 * layerインデックスと色の対応:
 *   通常アイテム・ブロック:
 *     layer0 → material.getColor()          （主色）
 *     layer1 → material.getSecondaryColor() （副色）
 *
 *   鉱石ブロック（2レイヤー構成）:
 *     layer0 → 石テクスチャ（着色なし）
 *     layer1 → 鉱石オーバーレイ → oreColor（設定あり）またはmaterial.getColor()で着色
 *
 * 鉱石の着色色は Ore.getOrePrimaryColor() が -1 でなければそれを使い、
 * -1 なら素材の primaryColor / secondaryColor にフォールバックする。
 */
public class MaterialColorHandler {

    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // 通常アイテム・ブロックアイテムの着色
        ItemColor materialItemColor = (stack, tintIndex) -> {
            if (stack.getItem() instanceof MaterialItem mi) {
                return getTint(mi.getMaterial(), tintIndex);
            }
            if (stack.getItem() instanceof MaterialBlockItem mbi) {
                return getTint(mbi.getMaterial(), tintIndex);
            }
            return 0xFFFFFFFF;
        };

        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            registerItemColorsForMaterial(event, material, materialItemColor);

            // 鉱石ブロックアイテム（MaterialOreBlockItem）の着色登録
            if (material.hasPartFlag(MaterialFlags.GENERATE_ORE)
                    && OreRegistry.hasSettings(material)) {
                registerOreItemColors(event, material);
            }
        }
    }

    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            String materialName = material.getId().split(":")[1];

            // 通常ブロックの着色
            for (MaterialFlag flag : material.getPartFlags()) {
                flag.getTagPrefix().ifPresent(prefix -> {
                    if (prefix.isBlock()) registerBlockColor(event, material, materialName, prefix);
                });
            }

            if (!material.hasPropertyFlag(MaterialFlags.IS_VANILLA)) {
                for (MaterialFlag flag : material.getImpliedFlags()) {
                    flag.getTagPrefix().ifPresent(prefix -> {
                        if (prefix.isBlock()) registerBlockColor(event, material, materialName, prefix);
                    });
                }
            }

            // 鉱石ブロックの着色登録
            if (material.hasPartFlag(MaterialFlags.GENERATE_ORE)
                    && OreRegistry.hasSettings(material)) {
                registerOreBlockColors(event, material, materialName);
            }
        }
    }

    private static void registerItemColorsForMaterial(RegisterColorHandlersEvent.Item event,
                                                      Material material, ItemColor color) {
        String materialName = material.getId().split(":")[1];

        for (MaterialFlag flag : material.getPartFlags()) {
            flag.getTagPrefix().ifPresent(prefix ->
                    registerItemColor(event, material, materialName, prefix, color));
        }

        if (!material.hasPropertyFlag(MaterialFlags.IS_VANILLA)) {
            for (MaterialFlag flag : material.getImpliedFlags()) {
                flag.getTagPrefix().ifPresent(prefix ->
                        registerItemColor(event, material, materialName, prefix, color));
            }
        }
    }

    /**
     * 鉱石ブロックアイテム（MaterialOreBlockItem）の着色を登録する。
     *
     * 鉱石は2レイヤー構成:
     *   layer0 = 石テクスチャ（0xFFFFFF = 着色なし）
     *   layer1 = 鉱石オーバーレイ（ore.getOrePrimaryColor() または material.getColor()）
     */
    private static void registerOreItemColors(RegisterColorHandlersEvent.Item event,
                                              Material material) {
        var settings = OreRegistry.getSettings(material);
        Set<Ore> registered = new HashSet<>();

        for (DimensionOreConfig config : settings.getOverworld()) collectOres(config, registered);
        for (DimensionOreConfig config : settings.getNether())    collectOres(config, registered);
        for (DimensionOreConfig config : settings.getEnd())       collectOres(config, registered);

        for (Ore ore : registered) {
            var holder = UMItems.getItem(material.getId(), "ore_" + ore.getId());
            if (holder == null) continue;
            Item item = holder.get();

            event.register((stack, tintIndex) -> getOreTint(material, ore, tintIndex), item);
        }
    }

    /**
     * 鉱石ブロック自体の着色を登録する。
     */
    private static void registerOreBlockColors(RegisterColorHandlersEvent.Block event,
                                               Material material, String materialName) {
        var settings = OreRegistry.getSettings(material);
        Set<Ore> registered = new HashSet<>();

        for (DimensionOreConfig config : settings.getOverworld()) collectOres(config, registered);
        for (DimensionOreConfig config : settings.getNether())    collectOres(config, registered);
        for (DimensionOreConfig config : settings.getEnd())       collectOres(config, registered);

        for (Ore ore : registered) {
            var holder = UMItems.getOreBlock(material.getId(), ore.getId());
            if (holder == null) continue;
            Block block = holder.get();

            event.register(
                    (state, level, pos, tintIndex) -> getOreTint(material, ore, tintIndex),
                    block);
        }
    }

    private static void registerItemColor(RegisterColorHandlersEvent.Item event,
                                          Material material, String materialName,
                                          TagPrefix prefix, ItemColor color) {
        var holder = UMItems.getItem(material.getId(), prefix.getId());
        if (holder == null) return;
        event.register(color, holder.get());
    }

    private static void registerBlockColor(RegisterColorHandlersEvent.Block event,
                                           Material material, String materialName,
                                           TagPrefix prefix) {
        var holder = UMItems.getBlock(material.getId(), prefix.getId());
        if (holder == null) return;
        Block block = holder.get();
        event.register((state, level, pos, tintIndex) -> getTint(material, tintIndex), block);
    }

    /**
     * 通常アイテム・ブロックのtintを返す。
     */
    private static int getTint(Material material, int tintIndex) {
        return switch (tintIndex) {
            case 0 -> 0xFF000000 | material.getColor();
            case 1 -> 0xFF000000 | material.getSecondaryColor();
            default -> 0xFFFFFFFF;
        };
    }

    /**
     * 鉱石ブロックのtintを返す。
     *
     * layer0: 石テクスチャ → 着色なし（0xFFFFFF）
     * layer1: 鉱石オーバーレイ → Ore設定の色 or 素材の色
     *
     * Ore.getOrePrimaryColor() が -1 の場合は素材の primaryColor を使う。
     */
    private static int getOreTint(Material material, Ore ore, int tintIndex) {
        return switch (tintIndex) {
            // layer0: 石テクスチャは着色しない
            case 0 -> 0xFFFFFFFF;
            // layer1: 鉱石オーバーレイ
            case 1 -> {
                int primary = ore.getOrePrimaryColor();
                yield 0xFF000000 | (primary == -1 ? material.getColor() : primary);
            }
            default -> 0xFFFFFFFF;
        };
    }

    private static void collectOres(DimensionOreConfig config, Set<Ore> result) {
        for (StoneGroup group : config.getStones()) {
            for (Ore ore : group.getOres()) {
                if (!config.isExcluded(ore)) result.add(ore);
            }
        }
    }
}