package io.github.twister716.universalmaterials.api.material.iconset;

import io.github.twister716.universalmaterials.UniversalMaterials;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 素材のテクスチャセットを定義するクラス。
 *
 * 親子継承によりテクスチャをフォールバックできる。
 * 例: DULLの親をMETALLICにすると、dull/フォルダにないテクスチャは
 *     自動的にmetallic/フォルダのものが使われる。
 *
 * テクスチャファイルの配置場所:
 *   assets/universalmaterials/textures/item/material_sets/<iconSet>/<iconType>.png
 *   assets/universalmaterials/textures/block/material_sets/<iconSet>/<iconType>.png
 *
 * モデルJSONのテクスチャパス:
 *   universalmaterials:item/material_sets/<iconSet>/<iconType>
 */
public class MaterialIconSet {

    private static final Map<String, MaterialIconSet> REGISTRY = new HashMap<>();
    private static final Map<String, MaterialIconSet> REGISTRY_VIEW =
            Collections.unmodifiableMap(REGISTRY);
    private static boolean locked = false;
    private static final String KUBEJS_MODID = "kubejs";

    // ==================== デフォルトIconSet ====================

    /** 金属光沢テクスチャセット。ルート（フォールバック先なし） */
    public static final MaterialIconSet METALLIC = new MaterialIconSet("metallic", null, null);

    /** 艶消し金属テクスチャセット。METALLICを親とする */
    public static final MaterialIconSet DULL = new MaterialIconSet("dull", METALLIC, null);

    /** バニラ鉄テクスチャセット。METALLICを親とする */
    public static final MaterialIconSet IRON = new MaterialIconSet("iron", METALLIC, null);

    /** バニラ鉄テクスチャセット。METALLICを親とする */
    public static final MaterialIconSet GOLD = new MaterialIconSet("gold", METALLIC, null);

    /** バニラ鉄テクスチャセット。METALLICを親とする */
    public static final MaterialIconSet COPPER = new MaterialIconSet("copper", METALLIC, null);

    // ==================== フィールド ====================

    private final String name;

    @Nullable
    private final MaterialIconSet parent;

    // ==================== コンストラクタ ====================

    public MaterialIconSet(String name, @Nullable MaterialIconSet parent,
                           @Nullable String callerModId) {
        if (locked) {
            throw new IllegalStateException(
                    "The IconSet registry is locked, so you cannot add new entries!");
        }
        if (REGISTRY.containsKey(name)) {
            if (KUBEJS_MODID.equals(callerModId)) {
                REGISTRY.put(name, this);
            } else {
                throw new IllegalArgumentException(
                        "IconSet \"" + name + "\" is already registered!");
            }
        } else {
            REGISTRY.put(name, this);
        }
        this.name   = name;
        this.parent = parent;
    }

    // ==================== テクスチャパス解決 ====================

    /**
     * このIconSetの iconType に対応するテクスチャのResourceLocationを返す。
     * 親を辿り、ルートに達したパスを返す。
     *
     * 「自分のフォルダにテクスチャがあればそれを使い、なければ親のフォルダを使う」
     * という挙動は、モデルJSONが参照するテクスチャパスをIconSetごとに分けることで
     * Minecraftのリソースパック機構が自動的に処理する。
     *
     * Datagenでは「このIconSetが実際に持つべきテクスチャパス」を返す。
     * テクスチャPNGが存在しない場合は親のIconSetのパスにフォールバックする。
     *
     * @param type     "item" または "block"
     * @param iconType テクスチャ名。例: "ingot", "nugget", "storage_block"
     */
    public ResourceLocation getTexturePath(String type, String iconType) {
        // 自分から親チェーンを辿り、最初に「テクスチャを定義すべきIconSet」を探す
        // 現状はIconSet自身のパスを返す（テクスチャはIconSetフォルダに手動配置）
        // 将来的にはランタイムでPNG存在チェックを挟む拡張が可能
        MaterialIconSet resolved = this;
        while (resolved.parent != null && !resolved.hasOwnTexture(type, iconType)) {
            resolved = resolved.parent;
        }
        return ResourceLocation.fromNamespaceAndPath(
                UniversalMaterials.MOD_ID,
                type + "/material_sets/" + resolved.name + "/" + iconType);
    }

    /**
     * このIconSetが指定したtypeとiconTypeのテクスチャを自分のフォルダに持つか返す。
     * ランタイムでMinecraftのリソースマネージャーを使って確認する。
     * Datagenフェーズでは呼べないため、Datagenでは常にfalseを返す。
     */
    private boolean hasOwnTexture(String type, String iconType) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc == null) return false;
        var path = ResourceLocation.fromNamespaceAndPath(
                UniversalMaterials.MOD_ID,
                "textures/" + type + "/material_sets/" + name + "/" + iconType + ".png");
        return mc.getResourceManager().getResource(path).isPresent();
    }

    // ==================== レジストリ ====================

    public static void lockRegistry()                        { locked = true; }
    public static boolean isLocked()                         { return locked; }
    public static Map<String, MaterialIconSet> getRegistry() { return REGISTRY_VIEW; }

    @Nullable
    public static MaterialIconSet getByName(String name)     { return REGISTRY.get(name); }

    public String getName()                                   { return name; }

    @Nullable
    public MaterialIconSet getParent()                       { return parent; }

    @Override
    public String toString() {
        return "MaterialIconSet{" + name
                + ", parent=" + (parent != null ? parent.getName() : "null") + "}";
    }
}
