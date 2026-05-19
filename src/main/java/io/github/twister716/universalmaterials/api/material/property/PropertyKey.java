package io.github.twister716.universalmaterials.api.material.property;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Propertyを識別するためのキークラス。
 * ジェネリクスによって取り出すPropertyの型を保証する。
 *
 * 使用例:
 *   // UMでキーを定義する
 *   public static final PropertyKey<OreProperty> ORE
 *       = new PropertyKey<>("ore", OreProperty.class, null);
 *
 *   // 素材からPropertyを型安全に取り出せる
 *   OreProperty ore = material.getProperty(PropertyKey.ORE);
 *
 * 外部Modからの追加は可能だが、レジストリのロック後は追加・編集・削除が禁止される。
 * KubeJS（ModID: "kubejs"）のみ、同名のKeyの上書きが許可される。
 */
public class PropertyKey<T extends IMaterialProperty> {

    // 登録済みのPropertyKeyを名前で管理するマップ（内部用）
    private static final Map<String, PropertyKey<?>> REGISTRY = new HashMap<>();

    // 外部から参照できる読み取り専用のREGISTRYのビュー
    private static final Map<String, PropertyKey<?>> REGISTRY_VIEW =
            Collections.unmodifiableMap(REGISTRY);

    // レジストリがロックされているかどうかのフラグ
    private static boolean locked = false;

    // KubeJSのModID定数
    private static final String KUBEJS_MODID = "kubejs";

    // ==================== フィールド ====================

    // このキーの識別名。例: "ore", "fluid"
    private final String name;

    // このキーに対応するPropertyのクラス情報
    // 型安全なキャストに使う
    private final Class<T> type;

    // ==================== コンストラクタ ====================

    /**
     * PropertyKeyを作成し、レジストリに登録する。
     *
     * @param name        識別名（例: "ore"）
     * @param type        対応するPropertyのクラス（例: OreProperty.class）
     * @param callerModId 呼び出し元のModID。KubeJS以外は同名の上書き不可
     * @throws IllegalStateException    レジストリがロック済みの場合
     * @throws IllegalArgumentException KubeJS以外が同名のKeyを登録しようとした場合
     */
    public PropertyKey(String name, Class<T> type, @Nullable String callerModId) {
        // レジストリがロック済みなら新規追加を禁止する
        if (locked) {
            throw new IllegalStateException(
                    "The PropertyKey registry is locked, so you cannot add new entries!"
            );
        }

        // 同名のKeyが既に登録されている場合の処理
        if (REGISTRY.containsKey(name)) {
            if (KUBEJS_MODID.equals(callerModId)) {
                // KubeJSからの呼び出しなら上書きを許可する
                REGISTRY.put(name, this);
            } else {
                // それ以外は重複登録としてエラーにする
                throw new IllegalArgumentException(
                        "PropertyKey \"" + name + "\" is already registered!"
                );
            }
        } else {
            // 新規登録
            REGISTRY.put(name, this);
        }

        this.name = name;
        this.type = type;
    }

    // ==================== レジストリ操作 ====================

    /**
     * レジストリをロックする。
     * ロック後は新規追加・編集・削除が一切禁止される。
     * UMの初期化完了時に呼び出す想定。
     */
    public static void lockRegistry() {
        locked = true;
    }

    /** レジストリがロック済みかどうかを返す。 */
    public static boolean isLocked() {
        return locked;
    }

    /** 読み取り専用のレジストリビューを返す。 */
    public static Map<String, PropertyKey<?>> getRegistry() {
        return REGISTRY_VIEW;
    }

    /**
     * 名前からPropertyKeyを取得する。
     *
     * @param name 識別名
     * @return 対応するPropertyKey。存在しない場合はnull
     */
    @Nullable
    public static PropertyKey<?> getByName(String name) {
        return REGISTRY.get(name);
    }

    // ==================== メソッド ====================

    /** このキーの識別名を返す。例: "ore" */
    public String getName() {
        return name;
    }

    /** このキーに対応するPropertyのクラス情報を返す。 */
    public Class<T> getType() {
        return type;
    }

    /**
     * 指定したIMaterialPropertyをこのキーの型にキャストして返す。
     * 型が一致しない場合はnullを返す。
     *
     * @param property キャスト対象のProperty
     * @return キャスト成功ならT型のProperty、失敗ならnull
     */
    @Nullable
    public T cast(IMaterialProperty property) {
        // type.isInstance()で型チェックしてからキャストする
        // ClassCastExceptionが起きない安全なキャスト
        if (type.isInstance(property)) {
            return type.cast(property);
        }
        return null;
    }

    /** デバッグ用。例: "PropertyKey{ore, type=OreProperty}" */
    @Override
    public String toString() {
        return "PropertyKey{" + name + ", type=" + type.getSimpleName() + "}";
    }
}