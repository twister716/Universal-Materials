package io.github.twister716.universalmaterials.content.tab;

import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.content.item.UMItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * クリエイティブタブのラッパークラス。
 * DeferredHolderを直接使う代わりにこのクラスで包むことで、
 * アドオンから addPrefix() でPrefixを後から追加できる。
 *
 * 使い方（アドオン側）:
 *   UMCreativeTabs.MATERIALS.addPrefix(MyPrefixes.GEAR);
 */
public class UMTab {

    // NeoForgeのタブ登録ホルダー。getHolder()で取得できる
    private final DeferredHolder<CreativeModeTab, CreativeModeTab> holder;

    // このタブに含めるTagPrefixのリスト（登録順 = 表示順）
    private final List<TagPrefix> prefixes;

    UMTab(DeferredHolder<CreativeModeTab, CreativeModeTab> holder, TagPrefix... initialPrefixes) {
        this.holder   = holder;
        this.prefixes = new ArrayList<>(Arrays.asList(initialPrefixes));
    }

    /**
     * このタブにTagPrefixを追加する。
     * 追加したPrefixのアイテムは既存のPrefixの後ろに表示される。
     * アドオンから既存タブに新しいPrefixを追加したいときに使う。
     *
     * @param prefix 追加するTagPrefix
     */
    public void addPrefix(TagPrefix prefix) {
        prefixes.add(prefix);
    }

    /**
     * タブのアイテムを埋める。CreativeModeTab.builderのdisplayItemsから呼ばれる。
     * 素材の登録順 × prefixesの順番 で表示順が決まる。
     */
    void fill(CreativeModeTab.Output output) {
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            for (TagPrefix prefix : prefixes) {
                if (!hasPrefixItem(material, prefix)) continue;
                var holder = UMItems.getItem(material.getId(), prefix.getId());
                if (holder == null) continue;
                Item item = holder.get();
                output.accept(new ItemStack(item));
            }
        }
    }

    /**
     * タブアイコン用: このタブの最初のPrefixを持つ最初の素材のItemStackを返す。
     */
    ItemStack getIconStack() {
        if (prefixes.isEmpty()) return ItemStack.EMPTY;
        TagPrefix first = prefixes.get(0);
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            if (!hasPrefixItem(material, first)) continue;
            var h = UMItems.getItem(material.getId(), first.getId());
            if (h != null) return new ItemStack(h.get());
        }
        return ItemStack.EMPTY;
    }

    /**
     * NeoForgeのDeferredHolderを返す。
     * NeoForgeのAPIが必要な箇所（タブへの登録確認など）で使う。
     */
    public DeferredHolder<CreativeModeTab, CreativeModeTab> getHolder() {
        return holder;
    }

    private static boolean hasPrefixItem(Material material, TagPrefix prefix) {
        for (MaterialFlag flag : material.getPartFlags()) {
            if (flag.getTagPrefix().filter(p -> p == prefix).isPresent()) return true;
        }
        if (!material.hasPropertyFlag(MaterialFlags.IS_VANILLA)) {
            for (MaterialFlag flag : material.getImpliedFlags()) {
                if (flag.getTagPrefix().filter(p -> p == prefix).isPresent()) return true;
            }
        }
        return false;
    }
}
