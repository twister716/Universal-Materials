package io.github.twister716.universalmaterials.api.material.tagprefix;

import javax.annotation.Nullable;

/**
 * TagPrefixを組み立てるBuilderクラス。
 *
 * iconType() でテクスチャファイル名を指定する。
 * 例: .iconType("ingot") → material_sets/metallic/ingot.png を参照する
 */
public class TagPrefixBuilder {

    final String id;
    String idFormat      = "%s";
    String enFormat      = "%s";
    String itemTagFormat = "";
    String parentTag     = "";
    boolean isBlock      = false;
    BlockModelType blockModelType = BlockModelType.CUBE;

    @Nullable
    String iconType    = null;

    @Nullable
    String callerModId = null;

    public TagPrefixBuilder(String id) { this.id = id; }

    public TagPrefixBuilder idFormat(String idFormat)   { this.idFormat = idFormat;   return this; }
    public TagPrefixBuilder enFormat(String enFormat)   { this.enFormat = enFormat;   return this; }
    public TagPrefixBuilder itemTagFormat(String fmt)   { this.itemTagFormat = fmt;   return this; }
    public TagPrefixBuilder parentTag(String parentTag) { this.parentTag = parentTag; return this; }
    public TagPrefixBuilder callerModId(String modId)   { this.callerModId = modId;   return this; }

    /**
     * テクスチャファイル名を設定する。
     * MaterialIconSetのフォルダ内のPNG名に対応する。
     * 例: .iconType("ingot") → material_sets/metallic/ingot.png
     */
    public TagPrefixBuilder iconType(String iconType) {
        this.iconType = iconType;
        return this;
    }

    public TagPrefixBuilder asBlock() {
        this.isBlock = true;
        this.blockModelType = BlockModelType.CUBE;
        return this;
    }

    public TagPrefixBuilder asOreBlock() {
        this.isBlock = true;
        this.blockModelType = BlockModelType.ORE;
        return this;
    }

    public TagPrefix build() {
        return new TagPrefix(id, idFormat, enFormat,
                itemTagFormat, parentTag, isBlock, blockModelType,
                iconType, callerModId);
    }
}
