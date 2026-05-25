package io.github.twister716.universalmaterials.content.item;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.api.ore.DimensionOreConfig;
import io.github.twister716.universalmaterials.api.ore.Ore;
import io.github.twister716.universalmaterials.api.ore.OreRegistry;
import io.github.twister716.universalmaterials.api.ore.OreSettings;
import io.github.twister716.universalmaterials.api.ore.StoneGroup;
import io.github.twister716.universalmaterials.content.block.MaterialOreBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 素材システムのフラグに基づいてアイテム・ブロックを自動登録するクラス。
 * UMMaterialRegistryに登録された全素材を走査し、
 * partFlagsとimpliedFlagsに応じたアイテム・ブロックを生成する。
 *
 * GENERATE_OREフラグを持つ素材については、OreRegistryから設定を取得し、
 * 全石種分（例: stone_tin_ore, deepslate_tin_ore...）の鉱石ブロックも登録する。
 */
public class UMItems {

    // "素材フルID:prefixId" でアイテム（BlockItemを含む）を引ける Map
    private static final Map<String, DeferredHolder<Item, ? extends Item>> ITEM_MAP = new HashMap<>();

    // "素材フルID:prefixId" でブロックを引ける Map
    private static final Map<String, DeferredHolder<Block, ? extends Block>> BLOCK_MAP = new HashMap<>();

    // 鉱石ブロック専用: "素材フルID:oreId" でブロックを引ける Map
    // キー例: "universalmaterials:tin:deepslate"
    private static final Map<String, DeferredHolder<Block, ? extends Block>> ORE_BLOCK_MAP = new HashMap<>();

    public static void registerAll() {
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            if (!material.isEnabled()) continue;
            String materialName = material.getId().split(":")[1];

            // partFlags のアイテム・ブロック登録
            for (MaterialFlag flag : material.getPartFlags()) {
                flag.getTagPrefix().ifPresent(prefix ->
                        registerByPrefix(material, materialName, prefix));
            }

            // IS_VANILLA でなければ impliedFlags も登録
            if (!material.hasPropertyFlag(MaterialFlags.IS_VANILLA)) {
                for (MaterialFlag flag : material.getImpliedFlags()) {
                    flag.getTagPrefix().ifPresent(prefix ->
                            registerByPrefix(material, materialName, prefix));
                }
            }

            // GENERATE_OREフラグを持ち、OreRegistryに設定がある素材は鉱石ブロックも登録する
            if (material.hasPartFlag(MaterialFlags.GENERATE_ORE)
                    && OreRegistry.hasSettings(material)) {
                registerOreBlocks(material, materialName);
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
                        .sound(material.getSoundType())
                        .requiresCorrectToolForDrops())
        );
        BLOCK_MAP.put(material.getId() + ":" + prefix.getId(), blockHolder);

        DeferredHolder<Item, MaterialBlockItem> itemHolder = UniversalMaterials.ITEMS.register(
                blockId, () -> new MaterialBlockItem(
                        blockHolder.get(), material, prefix, new Item.Properties())
        );
        ITEM_MAP.put(material.getId() + ":" + prefix.getId(), itemHolder);
    }

    /**
     * GENERATE_OREフラグを持つ素材の鉱石ブロックを、全石種分登録する。
     * OreRegistryから設定を取得し、設定されているStoneGroupの全Oreに対してブロックを生成する。
     *
     * 例: 素材TIN + StoneGroups.STONE → stone_tin_ore, granite_tin_ore, ...
     *     素材TIN + StoneGroups.DEEPSLATE → deepslate_tin_ore, tuff_tin_ore, ...
     *
     * BlockItemには通常のBlockItemではなくMaterialOreBlockItemを使う。
     * これにより鉱石専用のツールチップと動的なアイテム名が有効になる。
     */
    private static void registerOreBlocks(Material material, String materialName) {
        OreSettings settings = OreRegistry.getSettings(material);

        // 全ディメンション設定から使用するOreを重複なく収集する
        Set<Ore> oresToRegister = new HashSet<>();
        for (DimensionOreConfig config : settings.getOverworld()) collectOres(config, oresToRegister, settings);
        for (DimensionOreConfig config : settings.getNether())    collectOres(config, oresToRegister, settings);
        for (DimensionOreConfig config : settings.getEnd())       collectOres(config, oresToRegister, settings);

        for (Ore ore : oresToRegister) {
            String blockId = ore.formatBlockId(materialName);

            DeferredHolder<Block, MaterialOreBlock> blockHolder =
                    UniversalMaterials.BLOCKS.register(
                            blockId, () -> new MaterialOreBlock(material, ore));

            ORE_BLOCK_MAP.put(material.getId() + ":" + ore.getId(), blockHolder);

            // MaterialOreBlockItemで登録することで、
            // 鉱石専用ツールチップと動的アイテム名（"Deepslate Tin Ore"）が有効になる
            DeferredHolder<Item, MaterialOreBlockItem> itemHolder = UniversalMaterials.ITEMS.register(
                    blockId, () -> new MaterialOreBlockItem(
                            blockHolder.get(), material, ore, new Item.Properties()));

            ITEM_MAP.put(material.getId() + ":ore_" + ore.getId(), itemHolder);
        }
    }

    /** DimensionOreConfigから除外されていないOreをSetに追加するヘルパー */
    private static void collectOres(DimensionOreConfig config, Set<Ore> result,
                                    OreSettings settings) {
        for (StoneGroup group : config.getStones()) {
            for (Ore ore : group.getOres()) {
                if (config.isExcluded(ore)) continue;
                // vanillaStoneOres()で指定した石種はUMがブロックを登録しない
                if (settings.getVanillaBaseOres().contains(ore)) continue;
                result.add(ore);
            }
        }
    }

    public static DeferredHolder<Item, ? extends Item> getItem(String materialId, String prefixId) {
        return ITEM_MAP.get(materialId + ":" + prefixId);
    }

    public static DeferredHolder<Block, ? extends Block> getBlock(String materialId, String prefixId) {
        return BLOCK_MAP.get(materialId + ":" + prefixId);
    }

    /**
     * 鉱石ブロックを取得する。
     *
     * @param materialId 素材のフルID（例: "universalmaterials:tin"）
     * @param oreId      OreのID（例: "deepslate"）
     */
    public static DeferredHolder<Block, ? extends Block> getOreBlock(String materialId, String oreId) {
        return ORE_BLOCK_MAP.get(materialId + ":" + oreId);
    }
}