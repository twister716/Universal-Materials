package io.github.twister716.universalmaterials.api.material.flag;

import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 素材の性質・特徴を定義するクラス。
 *
 * フラグには2種類ある：
 * - 部品生成フラグ: TagPrefixと紐付いており、appendFlags()で素材に付ける
 *   例: GENERATE_INGOT → インゴットを自動生成する
 * - 素材特性フラグ: TagPrefixを持たず、flags()で素材の性質を表す
 *   例: IS_METAL → この素材は金属である
 *
 * requiredFlagsで依存関係を定義でき、
 * appendFlags()時に依存フラグが自動的にimpliedFlagsとして追加される。
 * 例: GENERATE_RAW_ORE_BLOCKはGENERATE_RAW_OREに依存する
 *
 * 外部Modからの追加は可能だが、レジストリのロック後は追加・編集・削除が禁止される。
 * KubeJS（ModID: "kubejs"）のみ、同名のFlagの上書きが許可される。
 */
public class MaterialFlag {

    // 登録済みのFlagを名前で管理するマップ（内部用）
    private static final Map<String, MaterialFlag> REGISTRY = new HashMap<>();

    // 外部から参照できる読み取り専用のREGISTRYのビュー
    private static final Map<String, MaterialFlag> REGISTRY_VIEW =
            Collections.unmodifiableMap(REGISTRY);

    // レジストリがロックされているかどうかのフラグ
    private static boolean locked = false;

    // KubeJSのModID定数
    private static final String KUBEJS_MODID = "kubejs";

    // ==================== フィールド ====================

    // このFlagの識別名。例: "generate_ingot", "is_metal"
    private final String name;

    // このFlagが対応するTagPrefix（部品生成フラグのみ持つ）
    // Optionalを使うことで、getTagPrefix().ifPresent(...)のように安全に扱える
    // 素材特性フラグ（IS_METALなど）はOptional.empty()
    private final Optional<TagPrefix> tagPrefix;

    // このFlagが依存する他のFlagのリスト
    // appendFlags()時に依存フラグが自動でimpliedFlagsに追加される
    // 例: GENERATE_RAW_ORE_BLOCKはGENERATE_RAW_OREに依存する
    private final List<MaterialFlag> requiredFlags;

    // ==================== コンストラクタ ====================

    /**
     * 部品生成フラグを作成する（TagPrefixあり）。
     * appendFlags()で素材に付けることでアイテム・ブロックを自動生成する。
     *
     * @param name          識別名（例: "generate_ingot"）
     * @param tagPrefix     対応するTagPrefix（例: TagPrefixes.INGOT）。素材特性フラグの場合はnull
     * @param requiredFlags 依存する他のFlag（可変長引数）
     */
    public MaterialFlag(String name, @Nullable TagPrefix tagPrefix, MaterialFlag... requiredFlags) {
        this(name, tagPrefix, null, requiredFlags);
    }

    /**
     * Flagを作成し、レジストリに登録する（KubeJS上書き対応版）。
     *
     * @param name          識別名（例: "generate_ingot"）
     * @param tagPrefix     対応するTagPrefix。素材特性フラグの場合はnull
     * @param callerModId   呼び出し元のModID。KubeJS以外は同名の上書き不可
     * @param requiredFlags 依存する他のFlag（可変長引数）
     * @throws IllegalStateException    レジストリがロック済みの場合
     * @throws IllegalArgumentException KubeJS以外が同名のFlagを登録しようとした場合
     */
    public MaterialFlag(String name, @Nullable TagPrefix tagPrefix,
                        @Nullable String callerModId, MaterialFlag... requiredFlags) {
        // レジストリがロック済みなら新規追加を禁止する
        if (locked) {
            throw new IllegalStateException(
                    "The MaterialFlag registry is locked, so you cannot add new entries!"
            );
        }

        // 同名のFlagが既に登録されている場合の処理
        if (REGISTRY.containsKey(name)) {
            if (KUBEJS_MODID.equals(callerModId)) {
                // KubeJSからの呼び出しなら上書きを許可する
                REGISTRY.put(name, this);
            } else {
                throw new IllegalArgumentException(
                        "MaterialFlag \"" + name + "\" is already registered!"
                );
            }
        } else {
            // 新規登録
            REGISTRY.put(name, this);
        }

        this.name          = name;
        this.tagPrefix     = Optional.ofNullable(tagPrefix);
        this.requiredFlags = Collections.unmodifiableList(Arrays.asList(requiredFlags));
    }

    // ==================== レジストリ操作 ====================

    /**
     * レジストリをロックする。
     * ロック後は新規追加・編集・削除が一切禁止される。
     */
    public static void lockRegistry() {
        locked = true;
    }

    /** レジストリがロック済みかどうかを返す。 */
    public static boolean isLocked() {
        return locked;
    }

    /** 読み取り専用のレジストリビューを返す。 */
    public static Map<String, MaterialFlag> getRegistry() {
        return REGISTRY_VIEW;
    }

    /**
     * 名前からFlagを取得する。
     *
     * @param name 識別名
     * @return 対応するFlag。存在しない場合はnull
     */
    @Nullable
    public static MaterialFlag getByName(String name) {
        return REGISTRY.get(name);
    }

    // ==================== メソッド ====================

    /** このFlagの識別名を返す。例: "generate_ingot" */
    public String getName() {
        return name;
    }

    /**
     * このFlagに対応するTagPrefixを返す。
     * 素材特性フラグ（IS_METALなど）の場合はOptional.empty()。
     * getTagPrefix().ifPresent(prefix -> ...) のように使う。
     */
    public Optional<TagPrefix> getTagPrefix() {
        return tagPrefix;
    }

    /**
     * このFlagが依存する他のFlagのリストを返す。
     * appendFlags()時に依存フラグが自動でimpliedFlagsに追加される。
     */
    public List<MaterialFlag> getRequiredFlags() {
        return requiredFlags;
    }

    /** デバッグ用。例: "MaterialFlag{generate_ingot}" */
    @Override
    public String toString() {
        return "MaterialFlag{" + name + "}";
    }
}