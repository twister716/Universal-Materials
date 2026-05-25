package io.github.twister716.universalmaterials.api.material.tagprefix;

/**
 * UMが標準で定義するTagPrefixの一覧。
 * iconType() でテクスチャファイル名（material_sets/<iconSet>/<iconType>.png）を指定する。
 */
public class TagPrefixes {

    public static final TagPrefix INGOT = new TagPrefixBuilder("ingot")
            .idFormat("%s_ingot")
            .enFormat("%s Ingot")
            .itemTagFormat("c:ingots/%s")
            .parentTag("c:ingots")
            .iconType("ingot")
            .build();

    public static final TagPrefix GEM = new TagPrefixBuilder("gem")
            .idFormat("%s")
            .enFormat("%s")
            .itemTagFormat("c:gems/%s")
            .parentTag("c:gems")
            .iconType("gem")
            .build();

    public static final TagPrefix NUGGET = new TagPrefixBuilder("nugget")
            .idFormat("%s_nugget")
            .enFormat("%s Nugget")
            .itemTagFormat("c:nuggets/%s")
            .parentTag("c:nuggets")
            .iconType("nugget")
            .build();

    public static final TagPrefix STORAGE_BLOCK = new TagPrefixBuilder("storage_block")
            .idFormat("%s_block")
            .enFormat("Block of %s")
            .itemTagFormat("c:storage_blocks/%s")
            .parentTag("c:storage_blocks")
            .iconType("storage_block")
            .asBlock()
            .build();

    public static final TagPrefix PLATE = new TagPrefixBuilder("plate")
            .idFormat("%s_plate")
            .enFormat("%s Plate")
            .itemTagFormat("c:plates/%s")
            .parentTag("c:plates")
            .iconType("plate")
            .build();

    public static final TagPrefix DUST = new TagPrefixBuilder("dust")
            .idFormat("%s_dust")
            .enFormat("%s Dust")
            .itemTagFormat("c:dusts/%s")
            .parentTag("c:dusts")
            .iconType("dust")
            .build();

    public static final TagPrefix ORE = new TagPrefixBuilder("ore")
            .idFormat("%s_ore")
            .enFormat("%s Ore")
            .itemTagFormat("c:ores/%s")
            .parentTag("c:ores")
            .iconType("ore")
            .asOreBlock()
            .build();

    public static final TagPrefix RAW_ORE = new TagPrefixBuilder("raw_ore")
            .idFormat("raw_%s")
            .enFormat("Raw %s")
            .itemTagFormat("c:raw_materials/%s")
            .parentTag("c:raw_materials")
            .iconType("raw_ore")
            .build();

    private TagPrefixes() {}
}
