package io.github.twister716.universalmaterials.api.material.iconset;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 素材のテクスチャセットを定義するクラス。
 * 素材ごとに見た目の種類（金属光沢・宝石・つや消しなど）を管理する。
 *
 * 親子継承によって、テクスチャが存在しない場合は親のテクスチャにフォールバックする。
 * 例: 「dull」の親を「metallic」にしておくと、
 *     「dull」にないテクスチャは「metallic」から借りてくる。
 *
 * 外部Modからの追加は可能だが、レジストリのロック後は追加・編集・削除が禁止される。
 * KubeJS（ModID: "kubejs"）のみ、同名のIconSetの上書きが許可される。
 */
public class MaterialIconSet {

    // 登録済みのIconSetを名前で管理するマップ（内部用）
    private static final Map<String, MaterialIconSet> REGISTRY = new HashMap<>();

    // 外部から参照できる読み取り専用のREGISTRYのビュー
    // 外部からREGISTRYを直接変更されないようにするためのもの
    private static final Map<String, MaterialIconSet> REGISTRY_VIEW =
            Collections.unmodifiableMap(REGISTRY);

    // レジストリがロックされているかどうかのフラグ
    // trueになると新規追加・編集・削除が一切禁止される
    private static boolean locked = false;

    // KubeJSのModID定数
    // KubeJSはスクリプトからIconSetを上書きできる特別扱いのMod
    private static final String KUBEJS_MODID = "kubejs";

    // ==================== デフォルトIconSet ====================

    /** 一般的な金属光沢テクスチャセット */
    public static final MaterialIconSet METALLIC = new MaterialIconSet("metallic", null, null);

    /** 艶消し金属テクスチャセット。METALLICを親とする */
    public static final MaterialIconSet DULL = new MaterialIconSet("dull", METALLIC, null);

    // ==================== フィールド ====================

    // このIconSetの識別名。例: "metallic", "gem", "dull"
    private final String name;

    // 親IconSet。nullの場合はルート（フォールバック先なし）
    // テクスチャが見つからない場合、親のテクスチャを使う
    @Nullable
    private final MaterialIconSet parent;

    // ==================== コンストラクタ ====================

    /**
     * IconSetを作成し、レジストリに登録する。
     *
     * @param name        識別名（例: "metallic"）
     * @param parent      親IconSet。フォールバック先がない場合はnull
     * @param callerModId 呼び出し元のModID。KubeJS以外は同名の上書き不可
     * @throws IllegalStateException    レジストリがロック済みの場合
     * @throws IllegalArgumentException KubeJS以外が同名のIconSetを登録しようとした場合
     */
    public MaterialIconSet(String name, @Nullable MaterialIconSet parent, @Nullable String callerModId) {
        // レジストリがロック済みなら新規追加を禁止する
        if (locked) {
            throw new IllegalStateException(
                    "The IconSet registry is locked, so you cannot add new entries!"
            );
        }

        // 同名のIconSetが既に登録されている場合の処理
        if (REGISTRY.containsKey(name)) {
            if (KUBEJS_MODID.equals(callerModId)) {
                // KubeJSからの呼び出しなら上書きを許可する
                REGISTRY.put(name, this);
            } else {
                // それ以外は重複登録としてエラーにする
                throw new IllegalArgumentException(
                        "IconSet \"" + name + "\" is already registered!"
                );
            }
        } else {
            // 新規登録
            REGISTRY.put(name, this);
        }

        this.name = name;
        this.parent = parent;
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
    public static Map<String, MaterialIconSet> getRegistry() {
        return REGISTRY_VIEW;
    }

    /**
     * 名前からIconSetを取得する。
     *
     * @param name 識別名
     * @return 対応するIconSet。存在しない場合はnull
     */
    @Nullable
    public static MaterialIconSet getByName(String name) {
        return REGISTRY.get(name);
    }

    // ==================== メソッド ====================

    /** このIconSetの識別名を返す。例: "metallic" */
    public String getName() {
        return name;
    }

    /** 親IconSetを返す。ルートの場合はnull */
    @Nullable
    public MaterialIconSet getParent() {
        return parent;
    }

    /**
     * テクスチャが見つからなかった場合のフォールバック先を返す。
     * 親を再帰的に辿り、ルート（parentがnull）に達したら自分自身を返す。
     */
    public MaterialIconSet getFallback() {
        if (parent == null) return this;
        return parent.getFallback();
    }

    /** デバッグ用。例: "MaterialIconSet{metallic, parent=dull}" */
    @Override
    public String toString() {
        return "MaterialIconSet{" + name
                + ", parent=" + (parent != null ? parent.getName() : "null") + "}";
    }
}