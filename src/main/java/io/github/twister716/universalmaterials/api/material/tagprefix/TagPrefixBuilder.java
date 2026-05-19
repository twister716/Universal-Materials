package io.github.twister716.universalmaterials.api.material.tagprefix;

import javax.annotation.Nullable;

/**
 * TagPrefixを組み立てるためのBuilderクラス。
 * メソッドチェーンでTagPrefixの設定を組み立て、build()でTagPrefixを生成する。
 *
 * 使用例:
 *   TagPrefix INGOT = new TagPrefixBuilder("ingot")
 *       .idFormat("%s_ingot")
 *       .enFormat("%s Ingot")
 *       .itemTagFormat("c:ingots/%s")
 *       .parentTag("c:ingots")
 *       .build();
 */
public class TagPrefixBuilder {

    // TagPrefixの識別名（必須）
    final String id;

    // アイテムIDフォーマット（デフォルト: "%s"）
    String idFormat = "%s";

    // 英語名フォーマット（デフォルト: "%s"）
    String enFormat = "%s";

    // 個別タグフォーマット（デフォルト: 空文字）
    String itemTagFormat = "";

    // 共通親タグ（デフォルト: 空文字）
    String parentTag = "";

    // ブロックかどうか（デフォルト: false）
    boolean isBlock = false;

    // 呼び出し元ModID（KubeJSからの上書き登録時に使う）
    @Nullable
    String callerModId = null;

    /**
     * Builderを作成する。
     *
     * @param id TagPrefixの識別名（例: "ingot"）
     */
    public TagPrefixBuilder(String id) {
        this.id = id;
    }

    /**
     * アイテムIDの生成フォーマットを設定する。
     * %sに素材IDが入る。
     *
     * @param idFormat フォーマット（例: "%s_ingot"）
     * @return このBuilderインスタンス
     */
    public TagPrefixBuilder idFormat(String idFormat) {
        this.idFormat = idFormat;
        return this;
    }

    /**
     * 英語名の生成フォーマットを設定する。
     * %sに素材英語名が入る。
     *
     * @param enFormat フォーマット（例: "%s Ingot"）
     * @return このBuilderインスタンス
     */
    public TagPrefixBuilder enFormat(String enFormat) {
        this.enFormat = enFormat;
        return this;
    }

    /**
     * 個別アイテムタグの生成フォーマットを設定する。
     * %sに素材IDが入る。
     *
     * @param itemTagFormat フォーマット（例: "c:ingots/%s"）
     * @return このBuilderインスタンス
     */
    public TagPrefixBuilder itemTagFormat(String itemTagFormat) {
        this.itemTagFormat = itemTagFormat;
        return this;
    }

    /**
     * 共通親タグを設定する。
     *
     * @param parentTag 親タグ（例: "c:ingots"）
     * @return このBuilderインスタンス
     */
    public TagPrefixBuilder parentTag(String parentTag) {
        this.parentTag = parentTag;
        return this;
    }

    /**
     * このTagPrefixをブロックとして扱う。
     * 鉱石ブロックなどに使う。
     *
     * @return このBuilderインスタンス
     */
    public TagPrefixBuilder asBlock() {
        this.isBlock = true;
        return this;
    }

    /**
     * 呼び出し元のModIDを設定する。
     * KubeJSからの上書き登録時に使う。
     *
     * @param callerModId 呼び出し元のModID
     * @return このBuilderインスタンス
     */
    public TagPrefixBuilder callerModId(String callerModId) {
        this.callerModId = callerModId;
        return this;
    }

    /**
     * 設定を元にTagPrefixを生成してレジストリに登録する。
     *
     * @return 生成されたTagPrefix
     */
    public TagPrefix build() {
        return new TagPrefix(id, idFormat, enFormat,
                itemTagFormat, parentTag, isBlock, callerModId);
    }
}