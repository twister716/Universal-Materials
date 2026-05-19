package io.github.twister716.universalmaterials.api.material.tagprefix;

/**
 * UMが標準で定義するTagPrefixの一覧。
 * 外部ModはTagPrefixBuilderを使って独自のTagPrefixを追加できる。
 */
public class TagPrefixes {

    // インゴット。例: Tin Ingot
    public static final TagPrefix INGOT = new TagPrefixBuilder("ingot")
            .idFormat("%s_ingot")
            .enFormat("%s Ingot")
            .itemTagFormat("c:ingots/%s")
            .parentTag("c:ingots")
            .build();

    // 素材ブロック。例: Block of Tin
    public static final TagPrefix STORAGE_BLOCK = new TagPrefixBuilder("storage_block")
            .idFormat("%s_block")
            .enFormat("Block of %s")
            .itemTagFormat("c:storage_block/%s")
            .parentTag("c:storage_block")
            .asBlock()
            .build();

    // ナゲット（塊）。例: Tin Nugget
    public static final TagPrefix NUGGET = new TagPrefixBuilder("nugget")
            .idFormat("%s_nugget")
            .enFormat("%s Nugget")
            .itemTagFormat("c:nuggets/%s")
            .parentTag("c:nuggets")
            .build();

    // 板。例: Tin Plate
    public static final TagPrefix PLATE = new TagPrefixBuilder("plate")
            .idFormat("%s_plate")
            .enFormat("%s Plate")
            .itemTagFormat("c:plates/%s")
            .parentTag("c:plates")
            .build();

    // 粉。例: Tin Dust
    public static final TagPrefix DUST = new TagPrefixBuilder("dust")
            .idFormat("%s_dust")
            .enFormat("%s Dust")
            .itemTagFormat("c:dusts/%s")
            .parentTag("c:dusts")
            .build();

    // 鉱石ブロック
    public static final TagPrefix ORE = new TagPrefixBuilder("ore")
            .idFormat("%s_ore")
            .enFormat("%s Ore")
            .itemTagFormat("c:ores/%s")
            .parentTag("c:ores")
            .asBlock()
            .build();

    // 原石
    public static final TagPrefix RAW_ORE = new TagPrefixBuilder("raw_ore")
            .idFormat("raw_%s_ore")
            .enFormat("Raw %s Ore")
            .itemTagFormat("c:raw_ores/%s")
            .parentTag("c:raw_ores")
            .build();

    // インスタンス化を防ぐ
    private TagPrefixes() {}
}