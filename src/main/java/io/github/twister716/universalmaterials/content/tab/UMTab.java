package io.github.twister716.universalmaterials.content.tab;

import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.BlockModelType;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefixes;
import io.github.twister716.universalmaterials.api.ore.DimensionOreConfig;
import io.github.twister716.universalmaterials.api.ore.Ore;
import io.github.twister716.universalmaterials.api.ore.OreRegistry;
import io.github.twister716.universalmaterials.api.ore.StoneGroup;
import io.github.twister716.universalmaterials.content.item.UMItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * クリエイティブタブのラッパークラス。
 * DeferredHolderを直接使う代わりにこのクラスで包むことで、
 * アドオンから addPrefix() でPrefixを後から追加できる。
 *
 * 鉱石ブロック（BlockModelType.ORE）は石種ごとに複数ブロックが存在するため、
 * 通常のprefixとは別の専用処理（fillOreBlocks）で追加する。
 *
 * 使い方（アドオン側）:
 *   UMCreativeTabs.MATERIALS.addPrefix(MyPrefixes.GEAR);
 */
public class UMTab {

    private final DeferredHolder<CreativeModeTab, CreativeModeTab> holder;
    private final List<TagPrefix> prefixes;

    UMTab(DeferredHolder<CreativeModeTab, CreativeModeTab> holder, TagPrefix... initialPrefixes) {
        this.holder   = holder;
        this.prefixes = new ArrayList<>(Arrays.asList(initialPrefixes));
    }

    /**
     * このタブにTagPrefixを追加する。
     * 追加したPrefixのアイテムは既存のPrefixの後ろに表示される。
     */
    public void addPrefix(TagPrefix prefix) {
        prefixes.add(prefix);
    }

    /**
     * タブのアイテムを埋める。CreativeModeTab.builderのdisplayItemsから呼ばれる。
     * 素材の登録順 × prefixesの順番 で表示順が決まる。
     *
     * prefixがBlockModelType.ORE（鉱石ブロック）の場合は、
     * 石種ごとに複数ブロックがあるためfillOreBlocks()に委譲する。
     */
    void fill(CreativeModeTab.Output output) {
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            for (TagPrefix prefix : prefixes) {

                // 鉱石ブロックPrefixは専用処理で全石種分追加する
                if (prefix.getBlockModelType() == BlockModelType.ORE) {
                    fillOreBlocks(material, output);
                    continue;
                }

                if (!hasPrefixItem(material, prefix)) continue;
                var holder = UMItems.getItem(material.getId(), prefix.getId());
                if (holder == null) continue;
                output.accept(new ItemStack(holder.get()));
            }
        }
    }

    /**
     * 鉱石ブロックを全石種分クリエイティブタブに追加する。
     *
     * 鉱石ブロックは「素材 × 石種」の組み合わせで複数存在する。
     * 例: TIN → stone_tin_ore, granite_tin_ore, deepslate_tin_ore, ...
     *
     * OreRegistryから設定を取得し、各ディメンションの全石種を重複なく追加する。
     * GENERATE_OREフラグがない、またはOreRegistryに未登録の素材はスキップする。
     */
    private void fillOreBlocks(Material material, CreativeModeTab.Output output) {
        if (!material.hasPartFlag(MaterialFlags.GENERATE_ORE)) return;
        if (!OreRegistry.hasSettings(material)) return;

        var settings = OreRegistry.getSettings(material);

        // 全ディメンション設定からOreを重複なく収集する（登録順を保つためLinkedHashSetは使わない）
        Set<Ore> seen    = new HashSet<>();
        List<Ore> ores   = new ArrayList<>();

        for (DimensionOreConfig config : settings.getOverworld()) collectOres(config, seen, ores);
        for (DimensionOreConfig config : settings.getNether())    collectOres(config, seen, ores);
        for (DimensionOreConfig config : settings.getEnd())       collectOres(config, seen, ores);

        String materialName = material.getId().split(":")[1];
        for (Ore ore : ores) {
            var blockHolder = UMItems.getItem(
                    material.getId(), "ore_" + ore.getId());
            if (blockHolder == null) continue;
            output.accept(new ItemStack(blockHolder.get()));
        }
    }

    /** DimensionOreConfigから除外されていないOreをリストに追加するヘルパー */
    private void collectOres(DimensionOreConfig config, Set<Ore> seen, List<Ore> result) {
        for (StoneGroup group : config.getStones()) {
            for (Ore ore : group.getOres()) {
                if (!config.isExcluded(ore) && seen.add(ore)) {
                    result.add(ore);
                }
            }
        }
    }

    /**
     * タブアイコン用: このタブの最初のPrefixを持つ最初の素材のItemStackを返す。
     */
    ItemStack getIconStack() {
        if (prefixes.isEmpty()) return ItemStack.EMPTY;
        TagPrefix first = prefixes.get(0);

        // 鉱石ブロックPrefixがアイコンの場合: 最初の素材の最初の石種を使う
        if (first.getBlockModelType() == BlockModelType.ORE) {
            for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
                if (!material.hasPartFlag(MaterialFlags.GENERATE_ORE)) continue;
                if (!OreRegistry.hasSettings(material)) continue;
                var settings = OreRegistry.getSettings(material);
                if (settings.getOverworld().isEmpty()) continue;
                var config = settings.getOverworld().get(0);
                for (StoneGroup group : config.getStones()) {
                    for (Ore ore : group.getOres()) {
                        var h = UMItems.getItem(material.getId(), "ore_" + ore.getId());
                        if (h != null) return new ItemStack(h.get());
                    }
                }
            }
            return ItemStack.EMPTY;
        }

        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            if (!hasPrefixItem(material, first)) continue;
            var h = UMItems.getItem(material.getId(), first.getId());
            if (h != null) return new ItemStack(h.get());
        }
        return ItemStack.EMPTY;
    }

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
