package io.github.twister716.universalmaterials.client;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefixes;
import io.github.twister716.universalmaterials.content.item.MaterialBlockItem;
import io.github.twister716.universalmaterials.content.item.MaterialItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * バニラアイテムを含む全アイテムにツールチップを付与するハンドラー。
 *
 * UMが生成したアイテム（MaterialItem/MaterialBlockItem）は
 * appendHoverText()で既にツールチップを持つため、ここでは処理しない。
 *
 * バニラアイテム（minecraft:iron_ingot等）は：
 *   1. アイテムのResourceLocationを取得する
 *   2. IS_VANILLAフラグを持つ全素材のTagPrefixで formatId() を呼んで逆引きする
 *   3. 一致すれば該当素材のツールチップを追加する
 *
 * これにより vanillaItem() のような明示的な紐付けなしに
 * バニラアイテムと素材を自動的に対応付けられる。
 */
@EventBusSubscriber(modid = UniversalMaterials.MOD_ID, value = Dist.CLIENT)
public class MaterialTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // UMのアイテムはappendHoverText()で処理済みなのでスキップする
        if (stack.getItem() instanceof MaterialItem) return;
        if (stack.getItem() instanceof MaterialBlockItem) return;

        // アイテムのResourceLocationを取得する
        ResourceLocation itemId = stack.getItemHolder()
                .unwrapKey()
                .map(key -> key.location())
                .orElse(null);
        if (itemId == null) return;

        // IS_VANILLAの素材のみを対象に逆引きする
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            if (!material.hasPropertyFlag(MaterialFlags.IS_VANILLA)) continue;

            TagPrefix matched = findMatchingPrefix(material, itemId);
            if (matched == null) continue;

            // MateralItemと同じShift/Ctrl判定でツールチップを追加する
            boolean shiftDown = Screen.hasShiftDown();
            boolean ctrlDown  = Screen.hasControlDown();
            MaterialItem.appendHoverTextForMaterial(
                    material, event.getToolTip(), shiftDown, ctrlDown);
            return;
        }
    }

    /**
     * 素材のTagPrefixとアイテムIDを照合して一致するTagPrefixを返す。
     *
     * IS_VANILLAの素材のアイテムは "minecraft" namespaceを持つ。
     * partFlagsのアイテム（例: NETHERITEのGENERATE_INGOT）は
     * "universalmaterials" namespaceなのでスキップする。
     */
    private static TagPrefix findMatchingPrefix(Material material, ResourceLocation itemId) {
        String materialName = material.getId().split(":")[1];

        for (TagPrefix prefix : TagPrefix.getRegistry().values()) {
            String expectedPath = prefix.formatId(materialName);
            if (!itemId.getPath().equals(expectedPath)) continue;

            // IS_VANILLAのimpliedFlagsアイテムはminecraft namespaceを期待する
            // partFlagsのアイテムはUM namespaceなのでスキップ
            boolean isPartFlag = material.getPartFlags().stream()
                    .anyMatch(f -> f.getTagPrefix().filter(p -> p == prefix).isPresent());
            if (isPartFlag) continue;

            if (!itemId.getNamespace().equals("minecraft")) continue;

            return prefix;
        }
        return null;
    }
}
