package io.github.twister716.universalmaterials.api.material.tagprefix;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 素材から作られるアイテム・ブロックの種類を定義するクラス。
 * 例: ingot（インゴット）、plate（板材）、ore（鉱石）など。
 *
 * TagPrefixがあることで「この素材はインゴットと板材を持つ」という
 * 情報を素材システムで一元管理できる。
 * 将来的にTextureType・RecipeTemplate・SoundTypeなどを追加予定。
 *
 * 外部Modからの追加は可能だが、レジストリのロック後は追加・編集・削除が禁止される。
 * KubeJS（ModID: "kubejs"）のみ、同名のTagPrefixの上書きが許可される。
 */
public class TagPrefix {

    // 登録済みのTagPrefixを名前で管理するマップ（内部用）
    private static final Map<String, TagPrefix> REGISTRY = new HashMap<>();

    // 外部から参照できる読み取り専用のREGISTRYのビュー
    private static final Map<String, TagPrefix> REGISTRY_VIEW =
            Collections.unmodifiableMap(REGISTRY);

    // レジストリがロックされているかどうかのフラグ
    private static boolean locked = false;

    // KubeJSのModID定数
    private static final String KUBEJS_MODID = "kubejs";

    // ==================== フィールド ====================

    // このTagPrefixの識別名。例: "ingot"
    private final String id;

    // アイテムIDの生成フォーマット。%sに素材IDが入る
    // 例: "%s_ingot" → "tin_ingot"
    private final String idFormat;

    // 英語名の生成フォーマット。%sに素材英語名が入る
    // 例: "%s Ingot" → "Tin Ingot"
    private final String enFormat;

    // 個別アイテムタグの生成フォーマット。%sに素材IDが入る
    // 例: "c:ingots/%s" → "c:ingots/tin"
    private final String itemTagFormat;

    // このTagPrefixに共通する親タグ
    // 例: "c:ingots"（全インゴットに付く共通タグ）
    private final String parentTag;

    // このTagPrefixがブロックかどうか
    private final boolean isBlock;

    // ==================== コンストラクタ ====================

    /**
     * TagPrefixBuilderからのみ呼び出されるコンストラクタ。
     * 直接インスタンスを作らず、TagPrefixBuilderを使うこと。
     */
    TagPrefix(String id, String idFormat, String enFormat,
              String itemTagFormat, String parentTag, boolean isBlock,
              @Nullable String callerModId) {
        // レジストリがロック済みなら新規追加を禁止する
        if (locked) {
            throw new IllegalStateException(
                    "The TagPrefix registry is locked, so you cannot add new entries!"
            );
        }

        // 同名のTagPrefixが既に登録されている場合の処理
        if (REGISTRY.containsKey(id)) {
            if (KUBEJS_MODID.equals(callerModId)) {
                // KubeJSからの呼び出しなら上書きを許可する
                REGISTRY.put(id, this);
            } else {
                throw new IllegalArgumentException(
                        "TagPrefix \"" + id + "\" is already registered!"
                );
            }
        } else {
            // 新規登録
            REGISTRY.put(id, this);
        }

        this.id            = id;
        this.idFormat      = idFormat;
        this.enFormat      = enFormat;
        this.itemTagFormat = itemTagFormat;
        this.parentTag     = parentTag;
        this.isBlock       = isBlock;
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
    public static Map<String, TagPrefix> getRegistry() {
        return REGISTRY_VIEW;
    }

    /**
     * 名前からTagPrefixを取得する。
     *
     * @param name 識別名
     * @return 対応するTagPrefix。存在しない場合はnull
     */
    @Nullable
    public static TagPrefix getByName(String name) {
        return REGISTRY.get(name);
    }

    // ==================== メソッド ====================

    /** このTagPrefixの識別名を返す。例: "ingot" */
    public String getId() {
        return id;
    }

    /**
     * 素材IDからアイテムIDを生成する。
     * 例: formatId("tin") → "tin_ingot"
     */
    public String formatId(String materialId) {
        return String.format(idFormat, materialId);
    }

    /**
     * 素材英語名からアイテム英語表示名を生成する。
     * 例: formatEnName("Tin") → "Tin Ingot"
     */
    public String formatEnName(String materialEnName) {
        return String.format(enFormat, materialEnName);
    }

    /**
     * 素材IDから個別アイテムタグを生成する。
     * 例: formatItemTag("tin") → "c:ingots/tin"
     */
    public String formatItemTag(String materialId) {
        return String.format(itemTagFormat, materialId);
    }

    /** この TagPrefix の共通親タグを返す。例: "c:ingots" */
    public String getParentTag() {
        return parentTag;
    }

    /** このTagPrefixがブロックかどうかを返す。 */
    public boolean isBlock() {
        return isBlock;
    }

    /** デバッグ用。例: "TagPrefix{ingot}" */
    @Override
    public String toString() {
        return "TagPrefix{" + id + "}";
    }
}