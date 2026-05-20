package io.github.twister716.universalmaterials.api.material;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.iconset.MaterialIconSet;
import io.github.twister716.universalmaterials.api.material.property.IMaterialProperty;
import io.github.twister716.universalmaterials.api.material.property.PropertyKey;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 素材そのものを表すクラス。
 * MaterialIconSet・MaterialFlag・IMaterialPropertyを統合して管理する。
 */
public class Material {

    // ==================== フィールド ====================

    private String id;
    private final String englishName;
    private final int color;
    private final int secondaryColor;
    @Nullable private final MaterialIconSet iconSet;
    private final Set<MaterialFlag> partFlags;
    private final Set<MaterialFlag> propertyFlags;
    private final Set<MaterialFlag> impliedFlags;
    private final Map<PropertyKey<?>, IMaterialProperty> properties;
    private final Map<TagPrefix, List<ResourceLocation>> itemTagsByPrefix;
    private final Map<TagPrefix, List<ResourceLocation>> blockTagsByPrefix;
    private final List<String> requiredModIds;
    private final List<Material> requiredMaterials;

    // ツールチップ用
    /** 原子番号。合金・化合物の場合は -1 */
    private final int atomicNumber;
    /** 元素記号。例: "Sn"。合金・化合物の場合は null */
    @Nullable private final String elementSymbol;
    /** 素材のフルネーム（英語）。未設定の場合は englishName を使う */
    @Nullable private final String fullName;
    /** 素材説明文（英語）。翻訳キーは "material.description.<素材名>" になる */
    @Nullable private final String description;

    // ブロック物性
    /** 採掘に必要なツールレベルのタグ */
    private final TagKey<Block> miningLevel;
    /** ブロックの硬度 */
    private final float hardness;
    /** ブロックの爆発耐性 */
    private final float explosionResistance;
    /** ブロックのサウンドタイプ */
    private final SoundType soundType;

    // Mod連携
    /**
     * この素材が連携する単一ModのID。
     * requireMod()で1つだけ指定した場合に設定される。
     * compatName()と組み合わせてツールチップに表示する。
     */
    @Nullable private final String compatModId;
    /**
     * ツールチップに表示するMod名。
     * 例: "Thermal Expansion" → ツールチップに "Thermal Expansion Compat Material" と表示。
     * 設定されていない場合はツールチップに表示しない。
     */
    @Nullable private final String compatName;

    // ==================== コンストラクタ ====================

    private Material(Builder b) {
        this.id                  = b.name;
        this.englishName         = b.englishName;
        this.color               = b.color;
        this.secondaryColor      = b.resolveSecondaryColor();
        this.iconSet             = b.iconSet;
        this.partFlags           = Collections.unmodifiableSet(b.partFlags);
        this.propertyFlags       = Collections.unmodifiableSet(b.propertyFlags);
        this.impliedFlags        = Collections.unmodifiableSet(b.impliedFlags);
        this.properties          = Collections.unmodifiableMap(b.properties);
        this.itemTagsByPrefix    = Collections.unmodifiableMap(b.itemTagsByPrefix);
        this.blockTagsByPrefix   = Collections.unmodifiableMap(b.blockTagsByPrefix);
        this.requiredModIds      = Collections.unmodifiableList(b.requiredModIds);
        this.requiredMaterials   = Collections.unmodifiableList(b.requiredMaterials);
        this.atomicNumber        = b.atomicNumber;
        this.elementSymbol       = b.elementSymbol;
        this.fullName            = b.fullName;
        this.description         = b.description;
        this.miningLevel         = b.miningLevel;
        this.hardness            = b.hardness;
        this.explosionResistance = b.explosionResistance;
        this.soundType           = b.soundType;
        this.compatModId         = b.compatModId;
        this.compatName          = b.compatName;
    }

    // ==================== 基本情報 ====================

    void setId(String id) { this.id = id; }

    public boolean isEnabled() {
        if (!requiredModIds.isEmpty()) {
            boolean anyLoaded = requiredModIds.stream()
                    .anyMatch(modId -> ModList.get().isLoaded(modId));
            if (!anyLoaded) return false;
        }
        if (compatModId != null && !ModList.get().isLoaded(compatModId)) {
            return false;
        }
        if (!requiredMaterials.isEmpty()) {
            boolean allEnabled = requiredMaterials.stream().allMatch(Material::isEnabled);
            if (!allEnabled) return false;
        }
        return true;
    }


    public String getId()          { return id; }
    public String getEnglishName() { return englishName; }

    /** fullName が設定されていればそれを、未設定の場合は englishName を返す */
    public String getFullName()    { return fullName != null ? fullName : englishName; }

    public int getColor()          { return color; }
    public int getSecondaryColor() { return secondaryColor; }

    @Nullable public MaterialIconSet getIconSet() { return iconSet; }

    // ==================== フラグ ====================

    public boolean hasPartFlag(MaterialFlag flag)     { return partFlags.contains(flag); }
    public boolean hasPropertyFlag(MaterialFlag flag) { return propertyFlags.contains(flag); }
    public boolean isImpliedFlag(MaterialFlag flag)   { return impliedFlags.contains(flag); }

    public Set<MaterialFlag> getPartFlags()     { return partFlags; }
    public Set<MaterialFlag> getPropertyFlags() { return propertyFlags; }
    public Set<MaterialFlag> getImpliedFlags()  { return impliedFlags; }

    // ==================== プロパティ ====================

    public boolean hasProperty(PropertyKey<?> key) { return properties.containsKey(key); }

    @Nullable
    public <T extends IMaterialProperty> T getProperty(PropertyKey<T> key) {
        IMaterialProperty property = properties.get(key);
        if (property == null) return null;
        return key.cast(property);
    }

    // ==================== タグ ====================

    public List<ResourceLocation> getItemTags(TagPrefix prefix) {
        return itemTagsByPrefix.getOrDefault(prefix, Collections.emptyList());
    }

    public List<ResourceLocation> getBlockTags(TagPrefix prefix) {
        return blockTagsByPrefix.getOrDefault(prefix, Collections.emptyList());
    }

    // ==================== ツールチップ用 ====================

    /** 原子番号を返す。合金・化合物の場合は -1 */
    public int getAtomicNumber()       { return atomicNumber; }

    /** 元素記号を返す。合金・化合物の場合は null */
    @Nullable
    public String getElementSymbol()   { return elementSymbol; }

    /** 合金・化合物かどうかを返す（atomicNumber == -1 なら true） */
    public boolean isAlloyOrCompound() { return atomicNumber == -1; }

    /**
     * 素材説明文の翻訳キーを返す。description が未設定の場合は null。
     * 翻訳キー形式: "material.description.<素材名>"
     */
    @Nullable
    public String getDescriptionKey() {
        if (description == null) return null;
        return "material.description." + id.split(":")[1];
    }

    /** 素材説明文（英語）を返す。未設定の場合は null */
    @Nullable
    public String getDescription()     { return description; }

    /**
     * Local MatName 用の翻訳キーを返す。
     * 翻訳キー形式: "material.universalmaterials.<id>.local"
     */
    public String getLocalNameKey() {
        return "material." + UniversalMaterials.MOD_ID + "." + id.split(":")[1] + ".local";
    }

    // ==================== ブロック物性 ====================

    /** 採掘に必要なツールレベルのタグを返す */
    public TagKey<Block> getMiningLevel()    { return miningLevel; }

    /** ブロックの硬度を返す */
    public float getHardness()               { return hardness; }

    /** ブロックの爆発耐性を返す */
    public float getExplosionResistance()    { return explosionResistance; }

    /** ブロックのサウンドタイプを返す */
    public SoundType getSoundType()          { return soundType; }

    // ==================== Mod連携 ====================

    /**
     * ツールチップに表示するMod名を返す。
     * compatName()が設定されていない場合は null を返す。
     * null の場合はツールチップにMod連携表示をしない。
     */
    @Nullable
    public String getCompatName()            { return compatName; }

    @Nullable
    public String getCompatModId() { return compatModId; }

    // ==================== その他 ====================

    @Override
    public String toString() { return "Material{" + id + "}"; }

    // ==================== Builder ====================

    public static class Builder {

        private final String name;
        private String englishName;
        private int color = 0xFFFFFF;
        private int secondaryColor = -1;
        @Nullable private MaterialIconSet iconSet = null;

        private final Set<MaterialFlag> partFlags      = new HashSet<>();
        private final Set<MaterialFlag> propertyFlags  = new HashSet<>();
        private final Set<MaterialFlag> impliedFlags   = new HashSet<>();
        private final Map<PropertyKey<?>, IMaterialProperty> properties        = new HashMap<>();
        private final Map<TagPrefix, List<ResourceLocation>> itemTagsByPrefix  = new HashMap<>();
        private final Map<TagPrefix, List<ResourceLocation>> blockTagsByPrefix = new HashMap<>();
        private final List<String>   requiredModIds    = new ArrayList<>();
        private final List<Material> requiredMaterials = new ArrayList<>();

        // ツールチップ用
        private int atomicNumber = -1;
        @Nullable private String elementSymbol = null;
        @Nullable private String fullName      = null;
        @Nullable private String description   = null;

        // ブロック物性
        private TagKey<Block> miningLevel    = BlockTags.NEEDS_STONE_TOOL;
        private float hardness               = 3.0f;
        private float explosionResistance    = 6.0f;
        private SoundType soundType          = SoundType.METAL;

        // Mod連携
        @Nullable private String compatModId = null;
        @Nullable private String compatName  = null;

        public Builder(String name) {
            this.name        = name;
            this.englishName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }

        public Builder englishName(String englishName)    { this.englishName = englishName;       return this; }
        public Builder color(int color)                   { this.color = color;                   return this; }
        public Builder secondaryColor(int secondaryColor) { this.secondaryColor = secondaryColor; return this; }
        public Builder iconSet(MaterialIconSet iconSet)   { this.iconSet = iconSet;               return this; }

        /**
         * 原子番号と元素記号を設定する。
         * 純粋な元素素材（鉄・錫・金など）に設定する。合金・化合物には不要。
         */
        public Builder element(int atomicNumber, String elementSymbol) {
            this.atomicNumber  = atomicNumber;
            this.elementSymbol = elementSymbol;
            return this;
        }

        /**
         * 素材のフルネーム（英語）を設定する。
         * アイテム名に短縮名を使う場合にフルネームをここに設定する。
         * 未設定の場合は englishName() の値が使われる。
         */
        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        /**
         * 素材の説明文（英語）を設定する。
         * 翻訳キーは素材名から自動生成される。
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * 採掘に必要なツールレベルを設定する。デフォルトは NEEDS_STONE_TOOL。
         */
        public Builder miningLevel(TagKey<Block> miningLevel) {
            this.miningLevel = miningLevel;
            return this;
        }

        /**
         * ブロックの硬度を設定する。デフォルトは 3.0f。
         */
        public Builder hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        /**
         * ブロックの爆発耐性を設定する。デフォルトは 6.0f。
         */
        public Builder explosionResistance(float explosionResistance) {
            this.explosionResistance = explosionResistance;
            return this;
        }

        /**
         * ブロックのサウンドタイプを設定する。デフォルトは SoundType.METAL。
         */
        public Builder soundType(SoundType soundType) {
            this.soundType = soundType;
            return this;
        }

        /**
         * この素材を有効化するのに必要なModIDを複数指定する。
         * いずれかのModが存在しない場合、素材は無効化されてゲームに追加されない。
         * 複数指定する場合はツールチップにMod名は表示されない。
         * 1つのModとMod名を表示したい場合は requireMod() + compatName() を使う。
         */
        public Builder requireMods(String... modIds) {
            this.requiredModIds.addAll(Arrays.asList(modIds));
            return this;
        }

        /**
         * この素材が連携する単一ModのIDを設定する。
         * そのModが存在しない場合、素材は無効化されてゲームに追加されない。
         * compatName() と組み合わせるとツールチップにMod名が表示される。
         *
         * 例:
         *   .requireMod("thermalexpansion")
         *   .compatName("Thermal Expansion")
         *   → ツールチップ: "Thermal Expansion Compat Material"
         */
        public Builder requireMod(String modId) {
            this.compatModId = modId;
            return this;
        }

        /**
         * ツールチップに表示するMod名を設定する。
         * requireMod() と組み合わせて使う。
         * 例: .compatName("Thermal Expansion")
         */
        public Builder compatName(String compatName) {
            this.compatName = compatName;
            return this;
        }

        public Builder appendFlags(MaterialFlag... flags) {
            for (MaterialFlag flag : flags) {
                partFlags.add(flag);
                resolveRequiredFlags(flag);
            }
            return this;
        }

        public Builder flags(MaterialFlag... flags) {
            propertyFlags.addAll(Arrays.asList(flags));
            return this;
        }

        private void resolveRequiredFlags(MaterialFlag flag) {
            for (MaterialFlag required : flag.getRequiredFlags()) {
                if (!partFlags.contains(required)) impliedFlags.add(required);
                resolveRequiredFlags(required);
            }
        }

        public <T extends IMaterialProperty> Builder property(PropertyKey<T> key, T property) {
            this.properties.put(key, property);
            return this;
        }

        public Builder itemTag(TagPrefix prefix, String... tags) {
            itemTagsByPrefix.computeIfAbsent(prefix, k -> new ArrayList<>())
                    .addAll(Arrays.stream(tags).map(ResourceLocation::parse).toList());
            return this;
        }

        public Builder blockTag(TagPrefix prefix, String... tags) {
            blockTagsByPrefix.computeIfAbsent(prefix, k -> new ArrayList<>())
                    .addAll(Arrays.stream(tags).map(ResourceLocation::parse).toList());
            return this;
        }

        public Builder requireMaterial(Material... materials) {
            this.requiredMaterials.addAll(Arrays.asList(materials));
            return this;
        }

        int resolveSecondaryColor() {
            if (secondaryColor != -1) return secondaryColor;
            int r = (int) (((color >> 16) & 0xFF) * 0.7f);
            int g = (int) (((color >>  8) & 0xFF) * 0.7f);
            int b = (int) (( color        & 0xFF) * 0.7f);
            return (r << 16) | (g << 8) | b;
        }

        public Material build() {
            Material material = new Material(this);
            for (IMaterialProperty property : properties.values()) {
                property.verifyProperty(material);
            }
            return material;
        }

        public Material buildAndRegister() {
            return UMMaterialRegistry.register(build());
        }
    }
}