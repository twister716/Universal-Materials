package io.github.twister716.universalmaterials.api.material;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * このアノテーションを付けたUMMaterialRegistryのサブクラスは、
 * UM起動時に自動的にインスタンス化されてレジストリに登録される。
 *
 * UMMaterials.init()への追記は不要。
 * クラスにアノテーションを付けるだけでUMに素材が追加される。
 */
@Retention(RetentionPolicy.RUNTIME) // 実行時にアノテーション情報を読み取れるようにする
@Target(ElementType.TYPE)           // クラスにのみ付けられるアノテーション
public @interface AutoMaterialRegistry {
}