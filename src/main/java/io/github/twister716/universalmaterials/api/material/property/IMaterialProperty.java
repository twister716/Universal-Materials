package io.github.twister716.universalmaterials.api.material.property;

import io.github.twister716.universalmaterials.api.material.Material;

/**
 * 素材が持てる追加データを定義するインターフェース。
 *
 * MaterialFlagがON/OFFのスイッチなのに対し、
 * IMaterialPropertyは具体的なデータを素材に付与できる。
 * 例: OreProperty（鉱石の生成範囲・生成量など）
 *     FluidProperty（流体の温度・粘度など）
 *
 * 外部ModはこのインターフェースをImplementsすることで
 * 独自のPropertyを追加できる。
 */
public interface IMaterialProperty {

    /**
     * このPropertyが素材に対して正しく設定されているか検証する。
     * Material.Builder.build()時に呼び出される。
     * 不正な状態の場合は例外を投げる。
     *
     * @param material 検証対象の素材
     * @throws IllegalStateException Propertyの設定が不正な場合
     */
    void verifyProperty(Material material);
}