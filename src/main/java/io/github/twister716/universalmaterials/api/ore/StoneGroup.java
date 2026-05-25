package io.github.twister716.universalmaterials.api.ore;

import java.util.ArrayList;
import java.util.List;

/**
 * 鉱石が生成される「石の種類グループ」を表すクラス。
 *
 * 鉱石ブロックは「石の種類 × 素材」の組み合わせで登録される。
 * StoneGroup はその「石の種類の括り」を定義する。
 *
 * 例:
 *   STONE グループ     → 石・花崗岩・安山岩・閃緑岩・鍾乳石 が属する
 *   DEEPSLATE グループ → 深層岩・タフ が属する
 *
 * DimensionOreConfig の .stones(...) で指定することで、
 * そのグループに属する全石種で鉱石を生成できる。
 */
public class StoneGroup {

    private final String id;
    private final String tagId;
    private final List<Ore> ores = new ArrayList<>();
    private static final List<StoneGroup> ALL = new ArrayList<>();

    public StoneGroup(String id, String tagId) {
        this.id    = id;
        this.tagId = tagId;
        ALL.add(this);
    }

    /** OreのコンストラクタからStoneGroupへ自動登録するために使う（package-private） */
    void addOre(Ore ore) {
        ores.add(ore);
    }

    /**
     * このグループの代表Oreを返す（リストの先頭）。
     * シルクタッチドロップ時に「どの石種の鉱石に戻すか」の判断などに使う。
     */
    public Ore getDefaultOre() {
        return ores.isEmpty() ? null : ores.get(0);
    }

    public String getId()                        { return id; }
    public String getTagId()                     { return tagId; }
    public List<Ore> getOres()                   { return ores; }
    public static List<StoneGroup> getAllGroups() { return ALL; }
}