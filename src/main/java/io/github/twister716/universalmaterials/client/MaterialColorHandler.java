package io.github.twister716.universalmaterials.client;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.content.item.UMItems;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * 素材アイテム・ブロックのカラーハンドラーを登録するクラス。
 * グレースケールテクスチャに素材の色を乗算して着色する。
 *
 * layerインデックスと色の対応:
 *   layer0 → material.getColor()          （主色）
 *   layer1 → material.getSecondaryColor() （副色）
 *   layer2 → 0xFFFFFF                     （着色なし・オーバーレイ）
 *
 * クライアントサイドのみで動作する（サーバーには不要）。
 */
@EventBusSubscriber(modid = UniversalMaterials.MOD_ID,
        value = Dist.CLIENT)
public class MaterialColorHandler {

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // layerインデックスに応じて色を返すItemColor
        ItemColor materialItemColor = (stack, tintIndex) -> {
            // アイテムからMaterialItemかMaterialBlockItemを取得して素材の色を返す
            if (stack.getItem() instanceof io.github.twister716.universalmaterials.content.item.MaterialItem mi) {
                return getTint(mi.getMaterial(), tintIndex);
            }
            if (stack.getItem() instanceof io.github.twister716.universalmaterials.content.item.MaterialBlockItem mbi) {
                return getTint(mbi.getMaterial(), tintIndex);
            }
            return 0xFFFFFF;
        };

        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            registerItemColorsForMaterial(event, material, materialItemColor);
        }
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            String materialName = material.getId().split(":")[1];

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
        }
    }

    private static void registerItemColorsForMaterial(
            RegisterColorHandlersEvent.Item event,
            Material material,
            ItemColor color) {
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

    private static void registerItemColor(RegisterColorHandlersEvent.Item event,
                                          Material material, String materialName,
                                          TagPrefix prefix, ItemColor color) {
        var holder = UMItems.getItem(material.getId(), prefix.getId());
        if (holder == null) return;
        Item item = holder.get();
        event.register(color, item);
    }

    private static void registerBlockColor(RegisterColorHandlersEvent.Block event,
                                           Material material, String materialName,
                                           TagPrefix prefix) {
        var holder = UMItems.getBlock(material.getId(), prefix.getId());
        if (holder == null) return;
        Block block = holder.get();

        event.register((state, level, pos, tintIndex) ->
                        getTint(material, tintIndex),
                block);
    }

    /**
     * layerインデックスに応じた色を返す。
     *   0 → 主色（material.getColor()）
     *   1 → 副色（material.getSecondaryColor()）
     *   2 → 0xFFFFFF（着色なし・オーバーレイそのまま表示）
     */
    private static int getTint(Material material, int tintIndex) {
        return switch (tintIndex) {
            case 0 -> 0xFF000000 | material.getColor();
            case 1 -> 0xFF000000 | material.getSecondaryColor();
            default -> 0xFFFFFFFF;
        };
    }
}
