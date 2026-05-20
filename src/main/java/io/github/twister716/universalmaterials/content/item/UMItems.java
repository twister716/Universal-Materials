package io.github.twister716.universalmaterials.content.item;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * 素材システムのフラグに基づいてアイテム・ブロックを自動登録するクラス。
 * UMMaterialRegistryに登録された全素材を走査し、
 * partFlagsとimpliedFlagsに応じたアイテム・ブロックを生成する。
 */
public class UMItems {

    // "素材フルID:prefixId" でアイテム（BlockItemを含む）を引ける Map
    private static final Map<String, DeferredHolder<Item, ? extends Item>> ITEM_MAP = new HashMap<>();

    // "素材フルID:prefixId" でブロックを引ける Map
    private static final Map<String, DeferredHolder<Block, ? extends Block>> BLOCK_MAP = new HashMap<>();

    public static void registerAll() {
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            String materialName = material.getId().split(":")[1];

            for (MaterialFlag flag : material.getPartFlags()) {
                flag.getTagPrefix().ifPresent(prefix ->
                        registerByPrefix(material, materialName, prefix));
            }

            if (!material.hasPropertyFlag(MaterialFlags.IS_VANILLA)) {
                for (MaterialFlag flag : material.getImpliedFlags()) {
                    flag.getTagPrefix().ifPresent(prefix ->
                            registerByPrefix(material, materialName, prefix));
                }
            }
        }
    }

    private static void registerByPrefix(Material material, String materialName, TagPrefix prefix) {
        if (prefix.isBlock()) {
            registerBlock(material, materialName, prefix);
        } else {
            registerItem(material, materialName, prefix);
        }
    }

    private static void registerItem(Material material, String materialName, TagPrefix prefix) {
        String itemId = prefix.formatId(materialName);
        DeferredHolder<Item, MaterialItem> holder = UniversalMaterials.ITEMS.register(
                itemId, () -> new MaterialItem(material, prefix, new Item.Properties())
        );
        ITEM_MAP.put(material.getId() + ":" + prefix.getId(), holder);
    }

    private static void registerBlock(Material material, String materialName, TagPrefix prefix) {
        String blockId = prefix.formatId(materialName);

        DeferredHolder<Block, Block> blockHolder = UniversalMaterials.BLOCKS.register(
                blockId, () -> new Block(BlockBehaviour.Properties.of()
                        .strength(material.getHardness(), material.getExplosionResistance())
                        .requiresCorrectToolForDrops())
        );
        BLOCK_MAP.put(material.getId() + ":" + prefix.getId(), blockHolder);

        // BlockItemもITEM_MAPに登録する
        // getItem()はアイテムもブロックのBlockItemも同じメソッドで取得するため、
        // ここに入れないとクリエイティブタブやその他の処理でnullが返ってしまう
        DeferredHolder<Item, MaterialBlockItem> itemHolder = UniversalMaterials.ITEMS.register(
                blockId, () -> new MaterialBlockItem(
                        blockHolder.get(), material, prefix, new Item.Properties())
        );
        ITEM_MAP.put(material.getId() + ":" + prefix.getId(), itemHolder);
    }

    public static DeferredHolder<Item, ? extends Item> getItem(String materialId, String prefixId) {
        return ITEM_MAP.get(materialId + ":" + prefixId);
    }

    public static DeferredHolder<Block, ? extends Block> getBlock(String materialId, String prefixId) {
        return BLOCK_MAP.get(materialId + ":" + prefixId);
    }
}

