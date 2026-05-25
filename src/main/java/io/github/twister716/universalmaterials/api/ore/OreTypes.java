package io.github.twister716.universalmaterials.api.ore;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

/**
 * UMが標準で定義するOre定数の置き場。
 *
 * ここに定義されたOreが鉱石ブロック生成の「石の種類」になる。
 * StoneGroupsより後に初期化されるため、StoneGroups.* を参照してOK。
 *
 * アドオンModはこのクラスに倣って独自のOreをOreBuilderで定義できる。
 */
public class OreTypes {

    // ===== 通常石グループ（STONEグループ） =====

    public static final Ore STONE = new OreBuilder("stone")
            .baseBlock(Blocks.STONE)
            .stoneGroup(StoneGroups.STONE)
            .buildAndRegister();

    public static final Ore GRANITE = new OreBuilder("granite")
            .idFormat("granite_%s_ore")
            .enFormat("Granite %s Ore")
            .baseBlock(Blocks.GRANITE)
            .stoneGroup(StoneGroups.STONE)
            .buildAndRegister();

    public static final Ore ANDESITE = new OreBuilder("andesite")
            .idFormat("andesite_%s_ore")
            .enFormat("Andesite %s Ore")
            .baseBlock(Blocks.ANDESITE)
            .stoneGroup(StoneGroups.STONE)
            .buildAndRegister();

    public static final Ore DIORITE = new OreBuilder("diorite")
            .idFormat("diorite_%s_ore")
            .enFormat("Diorite %s Ore")
            .baseBlock(Blocks.DIORITE)
            .stoneGroup(StoneGroups.STONE)
            .buildAndRegister();

    public static final Ore DRIPSTONE = new OreBuilder("dripstone_block")
            .idFormat("dripstone_%s_ore")
            .enFormat("Dripstone %s Ore")
            .baseBlock(Blocks.DRIPSTONE_BLOCK)
            .hardness(-0.5f)
            .blockSound(SoundType.DRIPSTONE_BLOCK)
            .stoneGroup(StoneGroups.STONE)
            .buildAndRegister();

    // ===== 深層岩グループ（DEEPSLATEグループ） =====

    public static final Ore DEEPSLATE = new OreBuilder("deepslate")
            .idFormat("deepslate_%s_ore")
            .enFormat("Deepslate %s Ore")
            .baseBlock(Blocks.DEEPSLATE)
            .blockSound(SoundType.DEEPSLATE)
            .hardness(1.5f)
            .stoneGroup(StoneGroups.DEEPSLATE)
            .buildAndRegister();

    public static final Ore TUFF = new OreBuilder("tuff")
            .idFormat("tuff_%s_ore")
            .enFormat("Tuff %s Ore")
            .baseBlock(Blocks.TUFF)
            .blockSound(SoundType.TUFF)
            .hardness(0.5f)
            .stoneGroup(StoneGroups.DEEPSLATE)
            .buildAndRegister();

    // ===== ネザーグループ（NETHERグループ） =====

    public static final Ore NETHERRACK = new OreBuilder("netherrack")
            .idFormat("nether_%s_ore")
            .enFormat("Nether %s Ore")
            .baseBlock(Blocks.NETHERRACK)
            .blockSound(SoundType.NETHERRACK)
            .hardness(-0.5f)
            .resistance(-0.5f)
            .stoneGroup(StoneGroups.NETHER)
            .buildAndRegister();

    public static final Ore BASALT = new OreBuilder("basalt")
            .idFormat("basalt_%s_ore")
            .enFormat("Basalt %s Ore")
            .baseBlock(Blocks.BASALT)
            .blockSound(SoundType.BASALT)
            .texturePath("block/basalt_side")
            .hardness(-0.5f)
            .resistance(-0.5f)
            .stoneGroup(StoneGroups.NETHER)
            .buildAndRegister();

    public static final Ore BLACKSTONE = new OreBuilder("blackstone")
            .idFormat("blackstone_%s_ore")
            .enFormat("Blackstone %s Ore")
            .baseBlock(Blocks.BLACKSTONE)
            .hardness(-0.5f)
            .resistance(-0.5f)
            .stoneGroup(StoneGroups.NETHER)
            .buildAndRegister();

    public static final Ore MAGMA_BLOCK = new OreBuilder("magma_block")
            .idFormat("magma_%s_ore")
            .enFormat("Magma %s Ore")
            .baseBlock(Blocks.MAGMA_BLOCK)
            .texturePath("block/magma")
            .hardness(-1.0f)
            .resistance(-0.5f)
            .stoneGroup(StoneGroups.NETHER)
            .buildAndRegister();

    // ===== エンドグループ（ENDグループ） =====

    public static final Ore END_STONE = new OreBuilder("end_stone")
            .idFormat("end_%s_ore")
            .enFormat("End %s Ore")
            .baseBlock(Blocks.END_STONE)
            .hardness(0.5f)
            .resistance(1.0f)
            .stoneGroup(StoneGroups.END)
            .buildAndRegister();

    private OreTypes() {}

    /** 静的初期化を強制するためのダミーメソッド。StoneGroups.init()より後に呼ぶ。 */
    public static void init() {}
}