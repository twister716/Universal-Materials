package io.github.twister716.universalmaterials.api.material.tagprefix;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 素材から作られるアイテム・ブロックの種類を定義するクラス。
 *
 * iconType はテクスチャファイル名に対応する。
 * 例: INGOT の iconType = "ingot"
 *   → textures/item/material_sets/metallic/ingot.png を参照する
 */
public class TagPrefix {

    private static final Map<String, TagPrefix> REGISTRY = new HashMap<>();
    private static final Map<String, TagPrefix> REGISTRY_VIEW = Collections.unmodifiableMap(REGISTRY);
    private static boolean locked = false;
    private static final String KUBEJS_MODID = "kubejs";

    private final String id;
    private final String idFormat;
    private final String enFormat;
    private final String itemTagFormat;
    private final String parentTag;
    private final boolean isBlock;
    private final BlockModelType blockModelType;

    /**
     * テクスチャファイル名。MaterialIconSetのフォルダ内のPNG名に対応する。
     * 例: "ingot" → material_sets/metallic/ingot.png
     * nullの場合はモデルJSON生成・カラーハンドラーの対象外になる。
     */
    @Nullable
    private final String iconType;

    TagPrefix(String id, String idFormat, String enFormat,
              String itemTagFormat, String parentTag, boolean isBlock,
              BlockModelType blockModelType, @Nullable String iconType,
              @Nullable String callerModId) {
        if (locked) {
            throw new IllegalStateException(
                    "The TagPrefix registry is locked, so you cannot add new entries!");
        }
        if (REGISTRY.containsKey(id)) {
            if (KUBEJS_MODID.equals(callerModId)) {
                REGISTRY.put(id, this);
            } else {
                throw new IllegalArgumentException(
                        "TagPrefix \"" + id + "\" is already registered!");
            }
        } else {
            REGISTRY.put(id, this);
        }

        this.id             = id;
        this.idFormat       = idFormat;
        this.enFormat       = enFormat;
        this.itemTagFormat  = itemTagFormat;
        this.parentTag      = parentTag;
        this.isBlock        = isBlock;
        this.blockModelType = blockModelType;
        this.iconType       = iconType;
    }

    public static void lockRegistry()                  { locked = true; }
    public static boolean isLocked()                   { return locked; }
    public static Map<String, TagPrefix> getRegistry() { return REGISTRY_VIEW; }

    @Nullable
    public static TagPrefix getByName(String name)     { return REGISTRY.get(name); }

    public String formatId(String materialId)       { return String.format(idFormat, materialId); }
    public String formatEnName(String materialName) { return String.format(enFormat, materialName); }
    public String formatItemTag(String materialId)  { return String.format(itemTagFormat, materialId); }

    public String getId()                     { return id; }
    public String getEnFormat()               { return enFormat; }
    public String getParentTag()              { return parentTag; }
    public String getItemTagFormat()          { return itemTagFormat; }
    public boolean isBlock()                  { return isBlock; }
    public BlockModelType getBlockModelType() { return blockModelType; }

    /** テクスチャファイル名を返す。nullの場合はモデル・カラーハンドラーの対象外。 */
    @Nullable
    public String getIconType()               { return iconType; }

    @Override
    public String toString() { return "TagPrefix{" + id + "}"; }
}
