package io.github.twister716.universalmaterials.api.ore;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/**
 * Oreを構築するビルダークラス。
 *
 * 説明文（Ore info）は素材側の OreSettings.Builder.oreDesc() で設定する。
 * 鉱石ブロックと原石の両方で同じ説明文が表示される。
 */
public class OreBuilder {

    private final String id;
    private String    idFormat;
    private String    enFormat        = "%s Ore";
    private Block     baseBlock;
    private String    texturePath;
    private SoundType soundType       = SoundType.STONE;
    private float     hardness        = 0.0f;
    private float     resistance      = 0.0f;
    private StoneGroup stoneGroup;

    // 着色色（-1なら素材のprimaryColor/secondaryColorを使う）
    private int     orePrimaryColor   = -1;
    private int     oreSecondaryColor = -1;
    private boolean colorizeOverlay   = true;

    public OreBuilder(String id) { this.id = id; }

    /** ブロックIDフォーマット。例: "deepslate_%s_ore" → "deepslate_tin_ore" */
    public OreBuilder idFormat(String fmt)     { this.idFormat       = fmt;   return this; }

    /** 英語名フォーマット。例: "Deepslate %s Ore" → "Deepslate Tin Ore" */
    public OreBuilder enFormat(String fmt)     { this.enFormat       = fmt;   return this; }

    /** ベースとなるバニラブロック。例: Blocks.DEEPSLATE */
    public OreBuilder baseBlock(Block block)   { this.baseBlock      = block; return this; }

    /** ベーステクスチャのリソースパス。省略すると "block/<id>" になる。 */
    public OreBuilder texturePath(String path) { this.texturePath    = path;  return this; }

    /** 鉱石ブロックのサウンド。省略するとSoundType.STONE。 */
    public OreBuilder blockSound(SoundType st) { this.soundType      = st;    return this; }

    /** ベースブロックとの硬度差分。 */
    public OreBuilder hardness(float h)        { this.hardness       = h;     return this; }

    /** ベースブロックとの爆発耐性差分。 */
    public OreBuilder resistance(float r)      { this.resistance     = r;     return this; }

    /** このOreが属するStoneGroup。 */
    public OreBuilder stoneGroup(StoneGroup g) { this.stoneGroup     = g;     return this; }

    /**
     * 鉱石オーバーレイの着色色を素材の色と独立して指定する。
     * 省略すると素材の primaryColor / secondaryColor が使われる。
     */
    public OreBuilder oreColor(int primary, int secondary) {
        this.orePrimaryColor   = primary;
        this.oreSecondaryColor = secondary;
        return this;
    }

    /**
     * 鉱石オーバーレイの着色を無効にする。
     * バニラ鉄・銅のように専用テクスチャを持つ素材に使う。
     */
    public OreBuilder noColorize() { this.colorizeOverlay = false; return this; }

    public Ore buildAndRegister() {
        if (idFormat    == null) idFormat    = "%s_ore";
        if (texturePath == null) texturePath = "block/" + id;
        return new Ore(id, idFormat, enFormat,
                baseBlock, texturePath, soundType,
                hardness, resistance, stoneGroup,
                orePrimaryColor, oreSecondaryColor, colorizeOverlay);
    }
}