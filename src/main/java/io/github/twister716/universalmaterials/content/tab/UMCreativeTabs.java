package io.github.twister716.universalmaterials.content.tab;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefixes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * UniversalMaterialsのクリエイティブタブを定義するクラス。
 *
 * タブを追加するときは register() を1行追加するだけ。
 * アドオンから既存タブにPrefixを追加するときは:
 *   UMCreativeTabs.MATERIALS.addPrefix(MyPrefixes.GEAR);
 *
 * 表示順: 素材の登録順 × register()に渡したPrefixの順番
 *
 * タブ名の翻訳キー形式: "itemGroup.universalmaterials.<id>"
 * 例: "itemGroup.universalmaterials.materials" → "Universal Materials"
 */
public class UMCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UniversalMaterials.MOD_ID);

    // ==================== タブ定義 ====================

    /** アイテム系素材タブ（インゴット・ナゲット・粉・板・原石） */
    public static final UMTab MATERIALS = register(
            "materials",
            TagPrefixes.INGOT,
            TagPrefixes.NUGGET,
            TagPrefixes.DUST,
            TagPrefixes.PLATE,
            TagPrefixes.RAW_ORE
    );

    /** ブロック系素材タブ（保管ブロック） */
    public static final UMTab EQUIPMENT = register(
            "equipment",
            TagPrefixes.INGOT,
            TagPrefixes.STORAGE_BLOCK
    );

    // ==================== 内部処理 ====================

    /**
     * タブを登録するヘルパーメソッド。
     * タブ名は翻訳キー "itemGroup.universalmaterials.<id>" で解決される。
     */
    private static UMTab register(String id, TagPrefix... prefixes) {
        UMTab[] ref = new UMTab[1];

        DeferredHolder<CreativeModeTab, CreativeModeTab> holder = TABS.register(id,
                () -> CreativeModeTab.builder()
                        // 翻訳キーでタブ名を解決する
                        .title(Component.translatable(
                                "itemGroup." + UniversalMaterials.MOD_ID + "." + id))
                        .icon(() -> ref[0].getIconStack())
                        .displayItems((params, output) -> ref[0].fill(output))
                        .build());

        ref[0] = new UMTab(holder, prefixes);
        return ref[0];
    }

    /** IEventBusへの登録。UniversalMaterialsのコンストラクタから呼ぶ。 */
    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
