package io.github.twister716.universalmaterials.datagen.tag;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.api.ore.DimensionOreConfig;
import io.github.twister716.universalmaterials.api.ore.Ore;
import io.github.twister716.universalmaterials.api.ore.OreRegistry;
import io.github.twister716.universalmaterials.api.ore.StoneGroup;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * ブロックタグJSON（data/minecraft/tags/block/ 等）を自動生成するProvider。
 *
 * 生成するタグ:
 *   minecraft:mineable/pickaxe    → 全素材ブロック・鉱石ブロックをツルハシで採掘可能にする
 *   素材のminingLevelタグ         → 素材ブロック・鉱石ブロックの必要採掘ツールレベルを登録する
 *                                   例: BlockTags.NEEDS_IRON_TOOL → 鉄ピッケル以上が必要
 *   c:storage_blocks/<素材名>     → 保管ブロックの個別タグ
 *   c:storage_blocks              → 保管ブロックの親タグ
 *   minecraft:beacon_base_blocks  → GENERATE_BEACON_MATERIALフラグを持つ素材のブロック
 */
public class MaterialBlockTagProvider extends BlockTagsProvider {

    public MaterialBlockTagProvider(PackOutput output,
                                    CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, UniversalMaterials.MOD_ID, null);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            String materialName = material.getId().split(":")[1];

            // ===== 通常ブロック（保管ブロック等） =====
            for (MaterialFlag flag : material.getPartFlags()) {
                flag.getTagPrefix().ifPresent(prefix -> {
                    if (prefix.isBlock()) registerBlockTags(material, materialName, prefix);
                });
            }

            if (!material.hasPropertyFlag(MaterialFlags.IS_VANILLA)) {
                for (MaterialFlag flag : material.getImpliedFlags()) {
                    flag.getTagPrefix().ifPresent(prefix -> {
                        if (prefix.isBlock()) registerBlockTags(material, materialName, prefix);
                    });
                }
            }

            // ===== 鉱石ブロック =====
            // GENERATE_OREフラグがあり、OreRegistryに設定がある素材の全石種ブロックを登録する
            if (material.hasPartFlag(MaterialFlags.GENERATE_ORE)
                    && OreRegistry.hasSettings(material)) {
                registerOreBlockTags(material, materialName);
            }
        }
    }

    /**
     * 通常ブロック（保管ブロック等）のタグを登録する。
     */
    private void registerBlockTags(Material material, String materialName, TagPrefix prefix) {
        ResourceLocation blockId = ResourceLocation.fromNamespaceAndPath(
                UniversalMaterials.MOD_ID, prefix.formatId(materialName));

        tag(BlockTags.MINEABLE_WITH_PICKAXE).addOptional(blockId);
        tag(material.getMiningLevel()).addOptional(blockId);

        if (!prefix.getItemTagFormat().isEmpty()) {
            TagKey<Block> individualTag = TagKey.create(
                    Registries.BLOCK,
                    ResourceLocation.parse(prefix.formatItemTag(materialName)));
            tag(individualTag).addOptional(blockId);

            if (!prefix.getParentTag().isEmpty()) {
                TagKey<Block> parentTag = TagKey.create(
                        Registries.BLOCK,
                        ResourceLocation.parse(prefix.getParentTag()));
                tag(parentTag).addOptionalTag(individualTag);
            }
        }

        if (material.hasPropertyFlag(MaterialFlags.GENERATE_BEACON_MATERIAL)) {
            tag(BlockTags.BEACON_BASE_BLOCKS).addOptional(blockId);
        }
    }

    /**
     * 鉱石ブロックの採掘タグを全石種分登録する。
     *
     * 登録するタグ:
     *   minecraft:mineable/pickaxe  → ツルハシで採掘可能
     *   素材のminingLevelタグ       → 採掘に必要なツールレベル（素材の設定を流用）
     *
     * 例: TIN（miningLevel=NEEDS_IRON_TOOL）の deepslate_tin_ore →
     *   minecraft:mineable/pickaxe に追加
     *   minecraft:needs_iron_tool に追加
     *   → 鉄ピッケル以上でないと原石をドロップしない
     */
    private void registerOreBlockTags(Material material, String materialName) {
        var settings = OreRegistry.getSettings(material);

        Set<Ore> registered = new HashSet<>();
        for (DimensionOreConfig config : settings.getOverworld()) collectOres(config, registered);
        for (DimensionOreConfig config : settings.getNether())    collectOres(config, registered);
        for (DimensionOreConfig config : settings.getEnd())       collectOres(config, registered);

        for (Ore ore : registered) {
            ResourceLocation blockId = ResourceLocation.fromNamespaceAndPath(
                    UniversalMaterials.MOD_ID, ore.formatBlockId(materialName));

            // ツルハシで採掘可能にする
            tag(BlockTags.MINEABLE_WITH_PICKAXE).addOptional(blockId);

            // 素材のminingLevelタグを適用する（素材ビルダーで設定した値を流用）
            tag(material.getMiningLevel()).addOptional(blockId);
        }
    }

    private void collectOres(DimensionOreConfig config, Set<Ore> result) {
        for (StoneGroup group : config.getStones()) {
            for (Ore ore : group.getOres()) {
                if (!config.isExcluded(ore)) result.add(ore);
            }
        }
    }

    @Override
    public String getName() {
        return "Universal Materials Block Tag Provider";
    }
}