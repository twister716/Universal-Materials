package io.github.twister716.universalmaterials.api.material;

import io.github.twister716.universalmaterials.UniversalMaterials;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.iconset.MaterialIconSet;
import io.github.twister716.universalmaterials.api.material.property.IMaterialProperty;
import io.github.twister716.universalmaterials.api.material.property.PropertyKey;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 素材そのものを表すクラス。
 * MaterialIconSet・MaterialFlag・IMaterialPropertyを統合して管理する。
 */
public class Material {

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

    // ツールチップ用フィールド
    /** 原子番号。合金・化合物の場合は-1 */
    private final int atomicNumber;
    /** 元素記号。例: "Sn"。合金・化合物の場合はnull */
    @Nullable private final String elementSymbol;
    /** 採掘に必要なツールレベルのタグ。例: BlockTags.NEEDS_STONE_TOOL */
    private final TagKey<Block> miningLevel;
    /** ブロックの硬度。壊れやすさに影響する */
    private final float hardness;
    /** ブロックの爆発耐性。未設定の場合はhardnessと同じ値になる */
    private final float explosionResistance;
    /** 素材説明文（英語）。翻訳キーは素材名から自動生成される */
    @Nullable private final String description;
    /** 素材のフルネーム（英語）。未設定の場合はenglishNameを使う。ツールチップのEnglish MatNameに表示 */
    @Nullable private final String fullName;

    private Material(Builder builder) {
        this.id               = builder.name;
        this.englishName      = builder.englishName;
        this.color            = builder.color;
        this.secondaryColor   = builder.resolveSecondaryColor();
        this.iconSet          = builder.iconSet;
        this.partFlags        = Collections.unmodifiableSet(builder.partFlags);
        this.propertyFlags    = Collections.unmodifiableSet(builder.propertyFlags);
        this.impliedFlags     = Collections.unmodifiableSet(builder.impliedFlags);
        this.properties       = Collections.unmodifiableMap(builder.properties);
        this.itemTagsByPrefix = Collections.unmodifiableMap(builder.itemTagsByPrefix);
        this.blockTagsByPrefix= Collections.unmodifiableMap(builder.blockTagsByPrefix);
        this.requiredModIds   = Collections.unmodifiableList(builder.requiredModIds);
        this.requiredMaterials= Collections.unmodifiableList(builder.requiredMaterials);
        this.atomicNumber     = builder.atomicNumber;
        this.elementSymbol    = builder.elementSymbol;
        this.miningLevel         = builder.miningLevel;
        this.hardness            = builder.hardness;
        this.explosionResistance  = builder.explosionResistance >= 0
                ? builder.explosionResistance : builder.hardness;
        this.description         = builder.description;
        this.fullName         = builder.fullName;
    }

    void setId(String id) { this.id = id; }

    public boolean isEnabled() {
        if (!requiredModIds.isEmpty()) {
            boolean anyModLoaded = requiredModIds.stream()
                    .anyMatch(modId -> ModList.get().isLoaded(modId));
            if (!anyModLoaded) return false;
        }
        if (!requiredMaterials.isEmpty()) {
            boolean allEnabled = requiredMaterials.stream().allMatch(Material::isEnabled);
            if (!allEnabled) return false;
        }
        return true;
    }

    public boolean hasPartFlag(MaterialFlag flag)     { return partFlags.contains(flag); }
    public boolean hasPropertyFlag(MaterialFlag flag) { return propertyFlags.contains(flag); }
    public boolean isImpliedFlag(MaterialFlag flag)   { return impliedFlags.contains(flag); }

    public String getId()          { return id; }
    public String getEnglishName() { return englishName; }

    public String getTranslationKey() {
        return "material." + id.replace(":", ".");
    }

    /**
     * 素材説明文の翻訳キーを返す。
     * descriptionIdが設定されていない場合はnullを返す。
     * 翻訳キー形式: "material.description.<descriptionId>"
     */
    /**
     * 素材説明文の翻訳キーを返す。descriptionが未設定の場合はnullを返す。
     * 翻訳キー形式: "material.description.<素材名>"
     */
    @Nullable
    public String getDescriptionKey() {
        if (description == null) return null;
        return "material.description." + id.split(":")[1];
    }

    /** 素材説明文（英語）を返す。未設定の場合はnull */
    @Nullable
    public String getDescription() {
        return description;
    }

    /** 採掘に必要なツールレベルのタグを返す */
    public TagKey<Block> getMiningLevel()        { return miningLevel; }
    /** ブロックの硬度を返す */
    public float getHardness()                   { return hardness; }
    /** ブロックの爆発耐性を返す */
    public float getExplosionResistance()         { return explosionResistance; }

    /**
     * 素材のフルネーム（英語）を返す。
     * fullName()が設定されていればそれを、未設定の場合はenglishNameを返す。
     */
    public String getFullName() {
        return fullName != null ? fullName : englishName;
    }

    /**
     * Local MatName用の翻訳キーを返す。
     * 翻訳キー形式: "material.universalmaterials.<id>.local"
     */
    public String getLocalNameKey() {
        return "material." + UniversalMaterials.MOD_ID + "." + id.split(":")[1] + ".local";
    }

    public int getColor()           { return color; }
    public int getSecondaryColor()  { return secondaryColor; }

    /** 原子番号を返す。合金・化合物の場合は-1 */
    public int getAtomicNumber()    { return atomicNumber; }

    /** 元素記号を返す。合金・化合物の場合はnull */
    @Nullable
    public String getElementSymbol() { return elementSymbol; }

    /** 合金・化合物かどうかを返す（atomicNumber == -1 ならtrue） */
    public boolean isAlloyOrCompound() { return atomicNumber == -1; }

    @Nullable public MaterialIconSet getIconSet()           { return iconSet; }
    public Set<MaterialFlag> getPartFlags()                  { return partFlags; }
    public Set<MaterialFlag> getPropertyFlags()              { return propertyFlags; }
    public Set<MaterialFlag> getImpliedFlags()               { return impliedFlags; }
    public boolean hasProperty(PropertyKey<?> key)           { return properties.containsKey(key); }

    @Nullable
    public <T extends IMaterialProperty> T getProperty(PropertyKey<T> key) {
        IMaterialProperty property = properties.get(key);
        if (property == null) return null;
        return key.cast(property);
    }

    public List<ResourceLocation> getItemTags(TagPrefix prefix) {
        return itemTagsByPrefix.getOrDefault(prefix, Collections.emptyList());
    }

    public List<ResourceLocation> getBlockTags(TagPrefix prefix) {
        return blockTagsByPrefix.getOrDefault(prefix, Collections.emptyList());
    }

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
        private final Map<PropertyKey<?>, IMaterialProperty> properties = new HashMap<>();
        private final Map<TagPrefix, List<ResourceLocation>> itemTagsByPrefix  = new HashMap<>();
        private final Map<TagPrefix, List<ResourceLocation>> blockTagsByPrefix = new HashMap<>();
        private final List<String>   requiredModIds    = new ArrayList<>();
        private final List<Material> requiredMaterials = new ArrayList<>();

        // ツールチップ用フィールド
        /** 原子番号。合金・化合物の場合は設定しない（デフォルト-1） */
        private int atomicNumber = -1;
        /** 元素記号。例: "Sn" */
        @Nullable private String elementSymbol = null;
        /** 素材説明文の翻訳キー用ID */
        private TagKey<Block> miningLevel        = BlockTags.NEEDS_STONE_TOOL;
        private float hardness                   = 3.0f;
        /** -1の場合はhardnessと同じ値になる */
        private float explosionResistance        = -1;
        @Nullable private String description = null;
        /** 素材のフルネーム（英語）。ツールチップのEnglish MatNameに使う */
        @Nullable private String fullName = null;

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
         * 純粋な元素素材（鉄・錫・金など）に設定する。
         * 合金・化合物には設定しない。
         *
         * @param atomicNumber  原子番号（例: 錫=50）
         * @param elementSymbol 元素記号（例: "Sn"）
         */
        public Builder element(int atomicNumber, String elementSymbol) {
            this.atomicNumber  = atomicNumber;
            this.elementSymbol = elementSymbol;
            return this;
        }

        /**
         * 素材説明文の翻訳キーIDを設定する。
         * 翻訳キーは "material.description.<descriptionId>" になる。
         * en_us.jsonに対応する翻訳を追加する必要がある。
         *
         * 複数の素材で同じ説明文を使いたい場合は同じIDを指定できる。
         * 例: .description("tin") → "material.description.tin"
         */
        public Builder description(String descriptionId) {
            this.description = descriptionId;
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

        public Builder requireMod(String... modIds) {
            this.requiredModIds.addAll(Arrays.asList(modIds));
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