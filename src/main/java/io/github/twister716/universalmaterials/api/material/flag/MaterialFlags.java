package io.github.twister716.universalmaterials.api.material.flag;

import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefixes;

/**
 * UMが標準で定義するMaterialFlagの一覧。
 *
 * フラグは2種類に分かれる：
 * - 部品生成フラグ（GENERATE_xxx）: TagPrefixと紐付いており、appendFlags()で素材に付ける
 *   → そのTagPrefixのアイテム・ブロックが自動生成される
 * - 素材特性フラグ（IS_xxx）: TagPrefixを持たず、flags()で素材の性質を表す
 *   → アイテム生成はしないが、レシピ生成やツールチップなどの条件分岐に使う
 *
 * 素材定義ファイルでstaticインポートすると短く書ける：
 *   import static io.github.twister716.universalmaterials.api.material.flag.MaterialFlags.*;
 *
 *   .appendFlags(GENERATE_INGOT, GENERATE_NUGGET)
 *   .flags(IS_METAL)
 */
public class MaterialFlags {

    // ==================== 部品生成フラグ（appendFlags()で使う） ====================

    /** インゴットを自動生成する。例: Tin Ingot */
    public static final MaterialFlag GENERATE_INGOT =
            new MaterialFlag("generate_ingot", TagPrefixes.INGOT);

    /** ナゲットを自動生成する。例: Tin Nugget */
    public static final MaterialFlag GENERATE_NUGGET =
            new MaterialFlag("generate_nugget", TagPrefixes.NUGGET);

    /** 素材ブロックを自動生成する。例: Block of Tin */
    public static final MaterialFlag GENERATE_STORAGE_BLOCK =
            new MaterialFlag("generate_storage_block", TagPrefixes.STORAGE_BLOCK);

    /** 板材を自動生成する。例: Tin Plate */
    public static final MaterialFlag GENERATE_PLATE =
            new MaterialFlag("generate_plate", TagPrefixes.PLATE);

    /** 粉末を自動生成する。例: Tin Dust */
    public static final MaterialFlag GENERATE_DUST =
            new MaterialFlag("generate_dust", TagPrefixes.DUST);

    /**
     * 鉱石ブロックを自動生成する（世界生成と連動）。
     * 鉱石ブロック自体は石の種類ごとに別途生成されるため、TagPrefixはnull。
     */
    public static final MaterialFlag GENERATE_ORE =
            new MaterialFlag("generate_ore", null);

    /**
     * 原石アイテムを自動生成する。例: Raw Tin Ore
     * GENERATE_OREを付けると自動でimpliedFlagsに追加される。
     */
    public static final MaterialFlag GENERATE_RAW_ORE =
            new MaterialFlag("generate_raw_ore", TagPrefixes.RAW_ORE);

    // ==================== 素材特性フラグ（flags()で使う） ====================

    /**
     * バニラMinecraftに元から存在する素材を表すフラグ。
     * このフラグを持つ素材はimpliedFlagsのアイテムを生成しない。
     */
    public static final MaterialFlag IS_VANILLA =
            new MaterialFlag("is_vanilla", null);

    /**
     * 宝石系素材を表すフラグ。
     * このフラグを持つ素材はGENERATE_OREを付けても原石を生成しない。
     */
    public static final MaterialFlag IS_GEM =
            new MaterialFlag("is_gem", null);

    /**
     * 合金素材を表すフラグ。
     * このフラグを持つ素材はAlloyRecipeの定義が必要。
     */
    public static final MaterialFlag IS_ALLOY =
            new MaterialFlag("is_alloy", null);

    /**
     * この素材のブロックをビーコンの台座として使えるようにするフラグ。
     * GENERATE_STORAGE_BLOCKと組み合わせて使う。
     * appendFlags()で付けると、DatagenでBeacon台座タグ（beacon_base_blocks）が登録される。
     *
     * 例:
     *   .appendFlags(GENERATE_STORAGE_BLOCK, GENERATE_BEACON_MATERIAL)
     */
    public static final MaterialFlag GENERATE_BEACON_MATERIAL =
            new MaterialFlag("generate_beacon_material", null);

    private MaterialFlags() {}
}
