package io.github.twister716.universalmaterials.api.ore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 1素材分の鉱石生成設定全体を表すクラス。
 *
 * 説明文（Ore info）の設定:
 *   Builder.oreDesc("説明文") で設定する。
 *   翻訳キーは素材IDを使って "ore.description.<素材ID>" の形式で外部から組み立てる。
 *   例: TIN素材 → "ore.description.tin": "A soft metal ore."
 *   このキーは鉱石ブロック・原石の両方のツールチップで使われる。
 *   未設定（null）の場合は Ore info 行を表示しない。
 */
public class OreSettings {

    private final List<DimensionOreConfig> overworld;
    private final List<DimensionOreConfig> nether;
    private final List<DimensionOreConfig> end;

    private final float dropMin;
    private final float dropMax;

    // oreDesc() で設定した説明文。null なら Ore info 行を表示しない。
    // 翻訳キーは "ore.description.<素材ID>" として外部で組み立てる。
    private final String oreDescription;

    // バニラがすでに生成している石種のSet。
    // configured_feature の targets からこの石種を除外することで、
    // バニラの既存鉱石ブロックを上書きせずに残り石種だけUMが担当する。
    // 例: IRONのSTONE（vanilla stone_iron_ore）をexcludeすることで
    //     granite_iron_ore, andesite_iron_ore... だけをUMが追加生成する。
    private final Set<Ore> vanillaBaseOres;

    private final int orePrimaryColor;
    private final int oreSecondaryColor;
    private final boolean colorizeOverlay;

    private OreSettings(Builder b) {
        this.overworld         = List.copyOf(b.overworld);
        this.nether            = List.copyOf(b.nether);
        this.end               = List.copyOf(b.end);
        this.dropMin           = b.dropMin;
        this.dropMax           = b.dropMax;
        this.oreDescription    = b.oreDescription;
        this.vanillaBaseOres   = Collections.unmodifiableSet(b.vanillaBaseOres);
        this.orePrimaryColor   = b.orePrimaryColor;
        this.oreSecondaryColor = b.oreSecondaryColor;
        this.colorizeOverlay   = b.colorizeOverlay;
    }

    public List<DimensionOreConfig> getOverworld()  { return overworld; }
    public List<DimensionOreConfig> getNether()     { return nether; }
    public List<DimensionOreConfig> getEnd()        { return end; }
    public float getDropMin()                       { return dropMin; }
    public float getDropMax()                       { return dropMax; }

    /**
     * oreDesc() で設定した説明文を返す。
     * null の場合は Ore info 行を表示しない。
     * 翻訳キーは "ore.description.<素材ID>" として呼び出し側で組み立てること。
     */
    public String getOreDescription()              { return oreDescription; }

    /**
     * バニラがすでに生成している石種のSetを返す。
     * MaterialWorldGenProvider はこの石種を targets から除外することで、
     * バニラの既存鉱石と重複しないよう生成する。
     */
    public Set<Ore> getVanillaBaseOres()           { return vanillaBaseOres; }

    public int getOrePrimaryColor()                { return orePrimaryColor; }
    public int getOreSecondaryColor()              { return oreSecondaryColor; }
    public boolean isColorizeOverlay()             { return colorizeOverlay; }

    // ==================== Builder ====================

    public static class Builder {

        private final List<DimensionOreConfig> overworld = new ArrayList<>();
        private final List<DimensionOreConfig> nether    = new ArrayList<>();
        private final List<DimensionOreConfig> end       = new ArrayList<>();

        private float   dropMin          = 1.0f;
        private float   dropMax          = 1.0f;
        private String  oreDescription   = null;
        private int     orePrimaryColor   = -1;
        private int     oreSecondaryColor = -1;
        private boolean colorizeOverlay   = true;
        private final Set<Ore> vanillaBaseOres = new HashSet<>();

        public Builder overworld(DimensionOreConfig... configs) {
            for (DimensionOreConfig c : configs) overworld.add(c);
            return this;
        }

        public Builder nether(DimensionOreConfig... configs) {
            for (DimensionOreConfig c : configs) nether.add(c);
            return this;
        }

        public Builder end(DimensionOreConfig... configs) {
            for (DimensionOreConfig c : configs) end.add(c);
            return this;
        }

        public Builder drop(float min, float max) {
            this.dropMin = min;
            this.dropMax = max;
            return this;
        }

        /**
         * 鉱石ブロック・原石共通の説明文を設定する。
         * 翻訳キーは素材IDから "ore.description.<素材ID>" の形式で自動生成される。
         * ここで渡すのは翻訳キーではなく説明文そのもの。
         *
         * 使い方:
         *   .oreDesc("A soft, malleable metal ore.")
         *   → TIN素材なら "ore.description.tin": "A soft, malleable metal ore." として登録
         *
         * @param description en_us.json に登録する説明文
         */
        public Builder oreDesc(String description) {
            this.oreDescription = description;
            return this;
        }

        /**
         * バニラがすでに生成している石種を指定する。
         *
         * 指定した石種は configured_feature の targets から除外され、
         * UMはその石種以外の同グループの石種にのみ鉱石を追加生成する。
         * これにより既存のバニラ鉱石と重複せず、生成量を増やさない。
         *
         * 例:
         *   バニラは stone_iron_ore と deepslate_iron_ore を生成している。
         *   UMはSTONEグループの残り（granite, andesite...）とDEEPSLATEグループの残り（tuff）を担当する。
         *
         *   .vanillaStoneOres(OreTypes.STONE, OreTypes.DEEPSLATE)
         *
         * @param ores バニラが担当する石種（targetsから除外するOre）
         */
        public Builder vanillaStoneOres(Ore... ores) {
            vanillaBaseOres.addAll(Arrays.asList(ores));
            return this;
        }

        public Builder oreColor(int primary, int secondary) {
            this.orePrimaryColor   = primary;
            this.oreSecondaryColor = secondary;
            return this;
        }

        public Builder noColorize() {
            this.colorizeOverlay = false;
            return this;
        }

        public OreSettings build() {
            return new OreSettings(this);
        }
    }
}