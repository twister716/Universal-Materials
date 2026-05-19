package io.github.twister716.universalmaterials.api.material.tagprefix;

/**
 * ブロックモデルの形状タイプを定義するenum。
 * TagPrefixがどのモデル形状を使うか（モデルJSONの生成方法）を決める。
 */
public enum BlockModelType {

    /** 通常のキューブ（保管ブロック用）。全面同じテクスチャ */
    CUBE,

    /** 鉱石ブロック用。ベース石テクスチャ + 鉱石オーバーレイテクスチャ */
    ORE
}
