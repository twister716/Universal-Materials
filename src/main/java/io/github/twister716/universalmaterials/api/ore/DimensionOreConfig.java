package io.github.twister716.universalmaterials.api.ore;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 1つのディメンション・1つの高度範囲に対する鉱石生成設定を表すクラス。
 *
 * OreSettings の overworld()/nether()/end() に渡して使う。
 * 1素材に対してディメンションごとに複数設定することも可能。
 *
 * 例:
 *   new DimensionOreConfig()
 *       .height(-64, 72)
 *       .size(9)
 *       .count(8)
 *       .heightType(HeightType.TRAPEZOID)
 *       .stones(StoneGroups.STONE, StoneGroups.DEEPSLATE)
 */
public class DimensionOreConfig {

    /**
     * 高度分布タイプ。
     * バニラの各鉱石がどのような分布で生成されているかに対応する。
     */
    public enum HeightType {
        /** 範囲内で均等に生成（石炭・錫など） */
        UNIFORM("minecraft:uniform"),
        /** 中心付近で最も多く、端に向かって減少（鉄・銅など） */
        TRAPEZOID("minecraft:trapezoid");

        private final String id;
        HeightType(String id) { this.id = id; }
        public String getId() { return id; }
    }

    private int minHeight  = -64;
    private int maxHeight  = 16;
    private int size       = 9;    // 1塊あたりの最大鉱石数
    private int count      = 8;    // チャンクあたりの生成試行回数
    private HeightType heightType  = HeightType.UNIFORM;
    private final List<StoneGroup> stones       = new ArrayList<>();
    private final Set<Ore> excludedOres          = new HashSet<>();
    private final List<ResourceKey<Biome>> biomes = new ArrayList<>();

    /** 生成する高度範囲を設定する。例: .height(-64, 72) */
    public DimensionOreConfig height(int min, int max) {
        this.minHeight = min;
        this.maxHeight = max;
        return this;
    }

    /**
     * 1塊あたりの最大鉱石数を設定する。
     * バニラの鉄鉱石は9、銅は10。
     */
    public DimensionOreConfig size(int size) {
        this.size = size;
        return this;
    }

    /**
     * チャンクあたりの生成試行回数を設定する。
     * 数値が大きいほど鉱石が多く生成される。
     */
    public DimensionOreConfig count(int count) {
        this.count = count;
        return this;
    }

    /** 高度分布タイプを設定する。 */
    public DimensionOreConfig heightType(HeightType type) {
        this.heightType = type;
        return this;
    }

    /**
     * 鉱石を生成する石グループを指定する。
     * 例: .stones(StoneGroups.STONE, StoneGroups.DEEPSLATE)
     * → 石グループ・深層岩グループの全石種に鉱石が生成される。
     */
    public DimensionOreConfig stones(StoneGroup... groups) {
        for (StoneGroup g : groups) stones.add(g);
        return this;
    }

    /**
     * 特定のOreを生成対象から除外する。
     * 例: バニラの石（Blocks.STONE）には生成したくないが、
     *     花崗岩・安山岩には生成したい、という場合に使う。
     */
    public DimensionOreConfig excludeOres(Ore... ores) {
        for (Ore ore : ores) excludedOres.add(ore);
        return this;
    }

    /**
     * 生成を特定のバイオームに限定する。
     * 指定しない場合はディメンション全体に生成される。
     */
    @SafeVarargs
    public final DimensionOreConfig biomes(ResourceKey<Biome>... biomes) {
        for (ResourceKey<Biome> b : biomes) this.biomes.add(b);
        return this;
    }

    public boolean isExcluded(Ore ore)              { return excludedOres.contains(ore); }
    public int getMinHeight()                        { return minHeight; }
    public int getMaxHeight()                        { return maxHeight; }
    public int getSize()                             { return size; }
    public int getCount()                            { return count; }
    public HeightType getHeightType()                { return heightType; }
    public List<StoneGroup> getStones()              { return stones; }
    public Set<Ore> getExcludedOres()                { return excludedOres; }
    public List<ResourceKey<Biome>> getBiomes()      { return biomes; }
}