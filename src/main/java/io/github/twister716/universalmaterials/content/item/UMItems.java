package io.github.twister716.universalmaterials.content.item;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * 素材システムのフラグに基づいてアイテム・ブロックを自動登録するクラス。
 * UMMaterialRegistryに登録された全素材を走査し、
 * partFlagsとimpliedFlagsに応じたアイテム・ブロックを生成する。
 *
 * 登録ルール：
 * - IS_VANILLAフラグを持つ素材はimpliedFlagsのアイテムを生成しない
 *   （バニラのアイテムと重複しないようにするため）
 * - TagPrefixのisBlock()がtrueのフラグはBlockとBlockItemの両方を登録する
 * - TagPrefixのisBlock()がfalseのフラグはItemのみを登録する
 */
public class UMItems {

    // 登録済みアイテムを "素材フルID:prefixId" で管理するマップ
    // 例: "universalmaterials:tin:ingot" → DeferredHolder<Item>
    private static final Map<String, DeferredHolder<Item, ? extends Item>> ITEM_MAP = new HashMap<>();

    // 登録済みブロックを "素材フルID:prefixId" で管理するマップ
    // 例: "universalmaterials:tin:storage_block" → DeferredHolder<Block>
    private static final Map<String, DeferredHolder<Block, ? extends Block>> BLOCK_MAP = new HashMap<>();

    /**
     * 全素材を走査してフラグに応じたアイテム・ブロックを登録する。
     * UniversalMaterialsのコンストラクタ内で呼び出す。
     */
    public static void registerAll() {
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            // 素材IDから "modid:" を除いた部分を取得する
            // 例: "universalmaterials:tin" → "tin"
            String materialName = material.getId().split(":")[1];

            // partFlagsを全走査してTagPrefixに紐付いたアイテム・ブロックを登録する
            for (MaterialFlag flag : material.getPartFlags()) {
                flag.getTagPrefix().ifPresent(prefix ->
                        registerByPrefix(material, materialName, prefix)
                );
            }

            // impliedFlagsを全走査する
            // ただしIS_VANILLAフラグを持つ素材はimpliedFlagsのアイテムを生成しない
            // （バニラのインゴットや鉱石と重複しないようにするため）
            if (!material.hasPropertyFlag(MaterialFlags.IS_VANILLA)) {
                for (MaterialFlag flag : material.getImpliedFlags()) {
                    flag.getTagPrefix().ifPresent(prefix ->
                            registerByPrefix(material, materialName, prefix)
                    );
                }
            }
        }
    }

    /**
     * TagPrefixのisBlock()に応じてアイテムまたはブロックを登録する。
     *
     * @param material     対象の素材
     * @param materialName 素材名（ModIDなし）例: "tin"
     * @param prefix       対象のTagPrefix
     */
    private static void registerByPrefix(Material material, String materialName, TagPrefix prefix) {
        if (prefix.isBlock()) {
            registerBlock(material, materialName, prefix);
        } else {
            registerItem(material, materialName, prefix);
        }
    }

    /**
     * アイテムを登録する。isBlock()がfalseのTagPrefixに使う。
     *
     * @param material     対象の素材
     * @param materialName 素材名（ModIDなし）例: "tin"
     * @param prefix       対象のTagPrefix
     */
    private static void registerItem(Material material, String materialName, TagPrefix prefix) {
        String itemId = prefix.formatId(materialName);
        DeferredHolder<Item, Item> holder = UniversalMaterials.ITEMS.register(
                itemId, () -> new Item(new Item.Properties())
        );
        ITEM_MAP.put(material.getId() + ":" + prefix.getId(), holder);
    }

    /**
     * ブロックとそのBlockItemを登録する。isBlock()がtrueのTagPrefixに使う。
     * BlockはBLOCKSに、BlockItemはITEMSにそれぞれ登録する。
     *
     * @param material     対象の素材
     * @param materialName 素材名（ModIDなし）例: "tin"
     * @param prefix       対象のTagPrefix
     */
    private static void registerBlock(Material material, String materialName, TagPrefix prefix) {
        String blockId = prefix.formatId(materialName);

        // ブロックをBLOCKSに登録する
        DeferredHolder<Block, Block> blockHolder = UniversalMaterials.BLOCKS.register(
                blockId, () -> new Block(BlockBehaviour.Properties.of()
                        .strength(3.0f, 6.0f)
                        .requiresCorrectToolForDrops())
        );
        BLOCK_MAP.put(material.getId() + ":" + prefix.getId(), blockHolder);

        // ブロックに対応するBlockItemをITEMSに登録する
        UniversalMaterials.ITEMS.register(
                blockId, () -> new BlockItem(blockHolder.get(), new Item.Properties())
        );
    }

    /**
     * 素材とTagPrefixのIDからアイテムを取得する。
     *
     * @param materialId 素材のフルID 例: "universalmaterials:tin"
     * @param prefixId   TagPrefixのID 例: "ingot"
     * @return 対応するDeferredHolder。存在しない場合はnull
     */
    public static DeferredHolder<Item, ? extends Item> getItem(String materialId, String prefixId) {
        return ITEM_MAP.get(materialId + ":" + prefixId);
    }

    /**
     * 素材とTagPrefixのIDからブロックを取得する。
     *
     * @param materialId 素材のフルID 例: "universalmaterials:tin"
     * @param prefixId   TagPrefixのID 例: "storage_block"
     * @return 対応するDeferredHolder。存在しない場合はnull
     */
    public static DeferredHolder<Block, ? extends Block> getBlock(String materialId, String prefixId) {
        return BLOCK_MAP.get(materialId + ":" + prefixId);
    }
}