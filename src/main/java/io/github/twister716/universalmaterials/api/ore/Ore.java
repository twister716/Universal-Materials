package io.github.twister716.universalmaterials.api.ore;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

import java.util.ArrayList;
import java.util.List;

/**
 * 鉱石ブロックの「石の種類1つ分」を表すクラス。
 *
 * 鉱石ブロックは「石の種類 × 素材」の組み合わせで生成される。
 * このクラスは「石の種類」側の情報（名前フォーマット・テクスチャ・物性・着色色）を持つ。
 *
 * 説明文（Ore info）は素材に紐づく OreSettings.getOreDescKey() で管理する。
 * 鉱石ブロック・原石の両方が同じキーを参照する。
 */
public class Ore {

    private final String id;
    private final String idFormat;
    private final String enFormat;
    private final Block  baseBlock;
    private final String texturePath;
    private final SoundType soundType;
    private final float hardness;
    private final float resistance;
    private final StoneGroup stoneGroup;

    // 鉱石オーバーレイの着色色（-1なら素材のprimaryColor/secondaryColorを使う）
    private final int orePrimaryColor;
    private final int oreSecondaryColor;
    // falseにするとオーバーレイを着色しない
    private final boolean colorizeOverlay;

    private static final List<Ore> ALL = new ArrayList<>();

    // OreBuilderからのみ呼ばれる（package-private）
    Ore(String id, String idFormat, String enFormat,
        Block baseBlock, String texturePath, SoundType soundType,
        float hardness, float resistance, StoneGroup stoneGroup,
        int orePrimaryColor, int oreSecondaryColor, boolean colorizeOverlay) {
        this.id                = id;
        this.idFormat          = idFormat;
        this.enFormat          = enFormat;
        this.baseBlock         = baseBlock;
        this.texturePath       = texturePath;
        this.soundType         = soundType;
        this.hardness          = hardness;
        this.resistance        = resistance;
        this.stoneGroup        = stoneGroup;
        this.orePrimaryColor   = orePrimaryColor;
        this.oreSecondaryColor = oreSecondaryColor;
        this.colorizeOverlay   = colorizeOverlay;
        if (stoneGroup != null) stoneGroup.addOre(this);
        ALL.add(this);
    }

    /** 素材IDからブロックIDを生成する。例: formatBlockId("tin") → "deepslate_tin_ore" */
    public String formatBlockId(String materialId) {
        return String.format(idFormat, materialId);
    }

    /** 素材英語名からブロック英語名を生成する。例: formatEnName("Tin") → "Deepslate Tin Ore" */
    public String formatEnName(String materialEnName) {
        return String.format(enFormat, materialEnName);
    }

    public String getId()              { return id; }
    public String getIdFormat()        { return idFormat; }
    public String getEnFormat()        { return enFormat; }
    public Block  getBaseBlock()       { return baseBlock; }
    public String getTexturePath()     { return texturePath; }
    public SoundType getSoundType()    { return soundType; }
    public float getHardness()         { return hardness; }
    public float getResistance()       { return resistance; }
    public StoneGroup getStoneGroup()  { return stoneGroup; }
    public int getOrePrimaryColor()    { return orePrimaryColor; }
    public int getOreSecondaryColor()  { return oreSecondaryColor; }
    public boolean isColorizeOverlay() { return colorizeOverlay; }

    public static List<Ore> getAllOres() { return ALL; }
}