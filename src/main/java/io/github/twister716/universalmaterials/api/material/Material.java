package io.github.twister716.universalmaterials.api.material;

import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.iconset.MaterialIconSet;
import io.github.twister716.universalmaterials.api.material.property.IMaterialProperty;
import io.github.twister716.universalmaterials.api.material.property.PropertyKey;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 素材そのものを表すクラス。
 * MaterialIconSet・MaterialFlag・IMaterialPropertyを統合して管理する。
 *
 * インスタンスはMaterial.Builderを通じて作成し、
 * UMMaterialRegistry.register()経由で登録する。
 *
 * requireMod・requireMaterialで登録条件を指定できる。
 * 条件を満たさない素材はゲームに登録されない。
 */
public class Material {

    // ==================== フィールド ====================

    // この素材の識別ID。UMMaterialRegistry.register()時にModIDが付与される
    // 例: "universalmaterials:tin"
    private String id;

    // この素材の英語名。翻訳キーのen_us生成に使う。例: "Tin"
    private final String englishName;

    // この素材のメインカラー（0xRRGGBB形式）
    private final int color;

    // この素材のサブカラー（0xRRGGBB形式）
    // 指定なしの場合はメインカラーを70%暗くして自動計算する
    private final int secondaryColor;

    // この素材のテクスチャセット
    @Nullable
    private final MaterialIconSet iconSet;

    // 部品生成フラグのセット（appendFlags()で追加）
    // TagPrefixと紐付いており、対応するアイテム・ブロックを自動生成する
    private final Set<MaterialFlag> partFlags;

    // 素材特性フラグのセット（flags()で追加）
    // TagPrefixを持たず、レシピ・ツールチップなどの条件分岐に使う
    private final Set<MaterialFlag> propertyFlags;

    // 前提フラグとして自動追加されたフラグのセット
    // appendFlags()でrequiredFlagsが再帰的に解決されてここに追加される
    // IS_VANILLAフラグを持つ素材はimpliedFlagsのアイテムを生成しない
    private final Set<MaterialFlag> impliedFlags;

    // この素材が持つPropertyのマップ
    private final Map<PropertyKey<?>, IMaterialProperty> properties;

    // TagPrefixごとに付くアイテムタグのマップ
    private final Map<TagPrefix, List<ResourceLocation>> itemTagsByPrefix;

    // TagPrefixごとに付くブロックタグのマップ
    private final Map<TagPrefix, List<ResourceLocation>> blockTagsByPrefix;

    // この素材の登録に必要なModIDのリスト（OR条件）
    // リストのうち1つでも導入されていれば登録される。空の場合は常に登録される
    private final List<String> requiredModIds;

    // この素材の登録に必要な前提素材のリスト（AND条件）
    // リストの素材が全て有効な場合のみ登録される。空の場合は常に登録される
    private final List<Material> requiredMaterials;

    // ==================== コンストラクタ ====================

    // Builderからのみインスタンスを作れるようにprivateにする
    private Material(Builder builder) {
        this.id             = builder.name;
        this.englishName    = builder.englishName;
        this.color          = builder.color;
        this.secondaryColor = builder.resolveSecondaryColor();
        this.iconSet        = builder.iconSet;
        this.partFlags      = Collections.unmodifiableSet(builder.partFlags);
        this.propertyFlags  = Collections.unmodifiableSet(builder.propertyFlags);
        this.impliedFlags   = Collections.unmodifiableSet(builder.impliedFlags);
        this.properties     = Collections.unmodifiableMap(builder.properties);
        this.itemTagsByPrefix  = Collections.unmodifiableMap(builder.itemTagsByPrefix);
        this.blockTagsByPrefix = Collections.unmodifiableMap(builder.blockTagsByPrefix);
        this.requiredModIds    = Collections.unmodifiableList(builder.requiredModIds);
        this.requiredMaterials = Collections.unmodifiableList(builder.requiredMaterials);
    }

    // ==================== パッケージプライベートメソッド ====================

    /**
     * IDを設定する。
     * UMMaterialRegistry.register()からのみ呼び出される。
     *
     * @param id "modid:name"形式のID
     */
    void setId(String id) {
        this.id = id;
    }

    // ==================== 登録条件チェック ====================

    /**
     * この素材がゲームに登録される条件を満たしているか返す。
     *
     * requireModが指定されている場合: リストのうち1つでもModが導入されていればOK（OR条件）
     * requireMaterialが指定されている場合: 前提素材が全て有効であればOK（AND条件）
     * 両方指定されている場合: 両方の条件を満たす必要がある
     * どちらも指定されていない場合: 常にtrue
     *
     * @return 登録条件を満たしていればtrue
     */
    public boolean isEnabled() {
        // requireModのチェック（OR条件）
        if (!requiredModIds.isEmpty()) {
            boolean anyModLoaded = requiredModIds.stream()
                    .anyMatch(modId -> ModList.get().isLoaded(modId));
            if (!anyModLoaded) return false;
        }

        // requireMaterialのチェック（AND条件）
        if (!requiredMaterials.isEmpty()) {
            boolean allEnabled = requiredMaterials.stream()
                    .allMatch(Material::isEnabled);
            if (!allEnabled) return false;
        }

        return true;
    }

    // ==================== フラグ判定 ====================

    /**
     * この素材が指定した部品生成フラグを持つか返す。
     *
     * @param flag 確認するFlag
     * @return partFlagsに含まれていればtrue
     */
    public boolean hasPartFlag(MaterialFlag flag) {
        return partFlags.contains(flag);
    }

    /**
     * この素材が指定した素材特性フラグを持つか返す。
     *
     * @param flag 確認するFlag
     * @return propertyFlagsに含まれていればtrue
     */
    public boolean hasPropertyFlag(MaterialFlag flag) {
        return propertyFlags.contains(flag);
    }

    /**
     * このフラグが前提フラグとして自動追加されたものか返す。
     * IS_VANILLAフラグを持つ素材はimpliedFlagsのアイテムを生成しない。
     *
     * @param flag 確認するFlag
     * @return impliedFlagsに含まれていればtrue
     */
    public boolean isImpliedFlag(MaterialFlag flag) {
        return impliedFlags.contains(flag);
    }

    // ==================== ゲッター ====================

    /**
     * この素材の識別IDを返す。
     *
     * @return 識別ID（例: "universalmaterials:tin"）
     */
    public String getId() {
        return id;
    }

    /**
     * この素材の英語名を返す。
     *
     * @return 英語名（例: "Tin"）
     */
    public String getEnglishName() {
        return englishName;
    }

    /**
     * この素材の翻訳キーを返す。
     * en_us.jsonの自動生成に使う。
     *
     * @return 翻訳キー（例: "material.universalmaterials.tin"）
     */
    public String getTranslationKey() {
        // "modid:name" → "material.modid.name" に変換する
        return "material." + id.replace(":", ".");
    }

    /** この素材のメインカラーを返す。0xRRGGBB形式 */
    public int getColor() {
        return color;
    }

    /**
     * この素材のサブカラーを返す。0xRRGGBB形式。
     * 指定なしの場合はメインカラーを70%暗くした値が返る。
     */
    public int getSecondaryColor() {
        return secondaryColor;
    }

    /** この素材のIconSetを返す。設定されていない場合はnull */
    @Nullable
    public MaterialIconSet getIconSet() {
        return iconSet;
    }

    /** この素材の部品生成フラグのセットを返す。 */
    public Set<MaterialFlag> getPartFlags() {
        return partFlags;
    }

    /** この素材の素材特性フラグのセットを返す。 */
    public Set<MaterialFlag> getPropertyFlags() {
        return propertyFlags;
    }

    /** この素材の前提フラグのセットを返す。 */
    public Set<MaterialFlag> getImpliedFlags() {
        return impliedFlags;
    }

    /**
     * この素材が指定したPropertyを持つか返す。
     *
     * @param key PropertyKey
     * @return Propertyを持つならtrue
     */
    public boolean hasProperty(PropertyKey<?> key) {
        return properties.containsKey(key);
    }

    /**
     * 指定したPropertyKeyに対応するPropertyを返す。
     *
     * @param key PropertyKey
     * @param <T> Propertyの型
     * @return 対応するProperty。存在しない場合はnull
     */
    @Nullable
    public <T extends IMaterialProperty> T getProperty(PropertyKey<T> key) {
        IMaterialProperty property = properties.get(key);
        if (property == null) return null;
        return key.cast(property);
    }

    /**
     * 指定したTagPrefixに対して付くアイテムタグのリストを返す。
     *
     * @param prefix 対象のTagPrefix
     * @return ResourceLocationのリスト。指定がない場合は空リスト
     */
    public List<ResourceLocation> getItemTags(TagPrefix prefix) {
        return itemTagsByPrefix.getOrDefault(prefix, Collections.emptyList());
    }

    /**
     * 指定したTagPrefixに対して付くブロックタグのリストを返す。
     *
     * @param prefix 対象のTagPrefix
     * @return ResourceLocationのリスト。指定がない場合は空リスト
     */
    public List<ResourceLocation> getBlockTags(TagPrefix prefix) {
        return blockTagsByPrefix.getOrDefault(prefix, Collections.emptyList());
    }

    /** デバッグ用。例: "Material{universalmaterials:tin}" */
    @Override
    public String toString() {
        return "Material{" + id + "}";
    }

    // ==================== Builder ====================

    /**
     * Materialを構築するためのBuilderクラス。
     * メソッドチェーンで素材の設定を組み立て、buildAndRegister()でMaterialを生成・登録する。
     * ModIDはUMMaterialRegistry.register()時に自動付与される。
     */
    public static class Builder {

        // 素材の名前（ModIDなし）。例: "tin"
        private final String name;

        // 素材の英語名。省略時はnameの先頭を大文字にしたものを使う
        private String englishName;

        // 素材のメインカラー（デフォルト: 白）
        private int color = 0xFFFFFF;

        // 素材のサブカラー。-1で未指定（resolveSecondaryColor()で自動計算される）
        private int secondaryColor = -1;

        // 素材のIconSet（任意）
        @Nullable
        private MaterialIconSet iconSet = null;

        // 部品生成フラグ（appendFlags()で追加）
        private final Set<MaterialFlag> partFlags = new HashSet<>();

        // 素材特性フラグ（flags()で追加）
        private final Set<MaterialFlag> propertyFlags = new HashSet<>();

        // 前提フラグとして自動追加されたフラグ（resolveRequiredFlags()で追加）
        private final Set<MaterialFlag> impliedFlags = new HashSet<>();

        // 素材が持つProperty
        private final Map<PropertyKey<?>, IMaterialProperty> properties = new HashMap<>();

        // TagPrefixごとのアイテムタグ
        private final Map<TagPrefix, List<ResourceLocation>> itemTagsByPrefix = new HashMap<>();

        // TagPrefixごとのブロックタグ
        private final Map<TagPrefix, List<ResourceLocation>> blockTagsByPrefix = new HashMap<>();

        // 登録に必要なModIDのリスト（OR条件）
        private final List<String> requiredModIds = new ArrayList<>();

        // 登録に必要な前提素材のリスト（AND条件）
        private final List<Material> requiredMaterials = new ArrayList<>();

        /**
         * Builderを作成する。
         * 英語名は省略可能で、省略時はnameの先頭を大文字にしたものが使われる。
         *
         * @param name 素材の名前（例: "tin"）
         */
        public Builder(String name) {
            this.name        = name;
            // デフォルト英語名: "tin" → "Tin"
            this.englishName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }

        /**
         * 素材の英語名を指定する。
         * 合金など名前が複数単語になる場合に使う。例: "Stainless Steel"
         *
         * @param englishName 英語名
         * @return このBuilderインスタンス
         */
        public Builder englishName(String englishName) {
            this.englishName = englishName;
            return this;
        }

        /**
         * 素材のメインカラーを設定する。
         *
         * @param color メインカラー（0xRRGGBB形式）
         * @return このBuilderインスタンス
         */
        public Builder color(int color) {
            this.color = color;
            return this;
        }

        /**
         * 素材のサブカラーを設定する。
         * 省略時はメインカラーを70%暗くした値が自動的に使われる。
         *
         * @param secondaryColor サブカラー（0xRRGGBB形式）
         * @return このBuilderインスタンス
         */
        public Builder secondaryColor(int secondaryColor) {
            this.secondaryColor = secondaryColor;
            return this;
        }

        /**
         * 素材のIconSetを設定する。
         *
         * @param iconSet 使用するIconSet
         * @return このBuilderインスタンス
         */
        public Builder iconSet(MaterialIconSet iconSet) {
            this.iconSet = iconSet;
            return this;
        }

        /**
         * 部品生成フラグを追加する。
         * TagPrefixと紐付いており、対応するアイテム・ブロックが自動生成される。
         * requiredFlagsに指定されたフラグはimpliedFlagsに自動追加される。
         *
         * @param flags 追加するFlag（可変長引数）
         * @return このBuilderインスタンス
         */
        public Builder appendFlags(MaterialFlag... flags) {
            for (MaterialFlag flag : flags) {
                partFlags.add(flag);
                // 依存フラグを再帰的に解決してimpliedFlagsに追加する
                resolveRequiredFlags(flag);
            }
            return this;
        }

        /**
         * 素材特性フラグを追加する。
         * TagPrefixを持たず、レシピ・ツールチップなどの条件分岐に使う。
         *
         * @param flags 追加するFlag（可変長引数）
         * @return このBuilderインスタンス
         */
        public Builder flags(MaterialFlag... flags) {
            propertyFlags.addAll(Arrays.asList(flags));
            return this;
        }

        /**
         * 依存フラグを再帰的に解決してimpliedFlagsに追加する。
         * 既にpartFlagsに含まれているフラグはimpliedFlagsに追加しない。
         *
         * @param flag 解決対象のFlag
         */
        private void resolveRequiredFlags(MaterialFlag flag) {
            for (MaterialFlag required : flag.getRequiredFlags()) {
                if (!partFlags.contains(required)) {
                    impliedFlags.add(required);
                }
                // 再帰的に依存フラグも解決する
                resolveRequiredFlags(required);
            }
        }

        /**
         * 素材にPropertyを追加する。
         *
         * @param key      PropertyKey
         * @param property 追加するProperty
         * @param <T>      Propertyの型
         * @return このBuilderインスタンス
         */
        public <T extends IMaterialProperty> Builder property(PropertyKey<T> key, T property) {
            this.properties.put(key, property);
            return this;
        }

        /**
         * 指定したTagPrefixのアイテムに付くタグを追加する。
         *
         * @param prefix 対象のTagPrefix
         * @param tags   タグ文字列（可変長引数）例: "c:ingots/tin"
         * @return このBuilderインスタンス
         */
        public Builder itemTag(TagPrefix prefix, String... tags) {
            itemTagsByPrefix
                    .computeIfAbsent(prefix, k -> new ArrayList<>())
                    .addAll(Arrays.stream(tags)
                            .map(ResourceLocation::parse)
                            .toList()
                    );
            return this;
        }

        /**
         * 指定したTagPrefixのブロックに付くタグを追加する。
         *
         * @param prefix 対象のTagPrefix
         * @param tags   タグ文字列（可変長引数）例: "c:ores/tin"
         * @return このBuilderインスタンス
         */
        public Builder blockTag(TagPrefix prefix, String... tags) {
            blockTagsByPrefix
                    .computeIfAbsent(prefix, k -> new ArrayList<>())
                    .addAll(Arrays.stream(tags)
                            .map(ResourceLocation::parse)
                            .toList()
                    );
            return this;
        }

        /**
         * この素材の登録に必要なModIDを指定する（OR条件）。
         * リストのうち1つでも導入されていれば登録される。
         * 指定しない場合は常に登録される。
         *
         * @param modIds 必要なModIDの配列（可変長引数）
         * @return このBuilderインスタンス
         */
        public Builder requireMod(String... modIds) {
            this.requiredModIds.addAll(Arrays.asList(modIds));
            return this;
        }

        /**
         * この素材の登録に必要な前提素材を指定する（AND条件）。
         * 指定した素材が全て有効な場合のみ登録される。
         * 合金の前提素材チェックに使う。
         *
         * @param materials 必要な素材の配列（可変長引数）
         * @return このBuilderインスタンス
         */
        public Builder requireMaterial(Material... materials) {
            this.requiredMaterials.addAll(Arrays.asList(materials));
            return this;
        }

        /**
         * サブカラーを解決して返す。
         * secondaryColorが指定されている場合はそのまま返す。
         * 指定なし（-1）の場合はメインカラーを70%暗くして返す。
         *
         * @return 解決されたサブカラー
         */
        int resolveSecondaryColor() {
            if (secondaryColor != -1) return secondaryColor;
            // RGBそれぞれを70%に暗くする
            int r = (int) (((color >> 16) & 0xFF) * 0.7f);
            int g = (int) (((color >>  8) & 0xFF) * 0.7f);
            int b = (int) (( color        & 0xFF) * 0.7f);
            return (r << 16) | (g << 8) | b;
        }

        /**
         * 設定を元にMaterialを生成する。
         * 全PropertyのverifyProperty()を呼び出して検証する。
         *
         * @return 生成されたMaterial
         * @throws IllegalStateException Propertyの検証に失敗した場合
         */
        public Material build() {
            Material material = new Material(this);
            for (IMaterialProperty property : properties.values()) {
                property.verifyProperty(material);
            }
            return material;
        }

        /**
         * 設定を元にMaterialを生成し、UMMaterialRegistryに登録する。
         * CURRENT_MOD_IDにModIDがセットされている必要がある。
         * 各カテゴリのMaterialクラス（UMMetalMaterialsなど）内で使う想定。
         *
         * @return 登録されたMaterial
         * @throws IllegalStateException Propertyの検証に失敗した場合
         */
        public Material buildAndRegister() {
            return UMMaterialRegistry.register(build());
        }
    }
}