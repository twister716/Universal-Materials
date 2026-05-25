package io.github.twister716.universalmaterials.api.ore;

/**
 * UMが標準で定義するStoneGroupの定数置き場。
 *
 * Oreの定数（STONE, DEEPSLATE...）はOreTypes.javaで定義する。
 * これは静的初期化の順序を管理するための分離で、
 * OreTypes が StoneGroups を参照するため、StoneGroups が先に初期化される必要がある。
 *
 * アドオンModはこのクラスに倣って独自のStoneGroupを定義できる。
 */
public class StoneGroups {

    /** オーバーワールドの通常石グループ（石・花崗岩・安山岩・閃緑岩・鍾乳石） */
    public static final StoneGroup STONE     = new StoneGroup("stone",      "stone");

    /** オーバーワールドの深層岩グループ（深層岩・凝灰岩） */
    public static final StoneGroup DEEPSLATE = new StoneGroup("deepslate",  "deepslate");

    /** ネザーの石グループ（ネザーラック・玄武岩・ブラックストーン・マグマブロック） */
    public static final StoneGroup NETHER    = new StoneGroup("netherrack", "netherrack");

    /** エンドの石グループ（エンドストーン） */
    public static final StoneGroup END       = new StoneGroup("end_stone",  "end_stone");

    private StoneGroups() {}

    /** 静的初期化を強制するためのダミーメソッド。OreTypes.init()より前に呼ぶ。 */
    public static void init() {}
}