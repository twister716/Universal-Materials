package io.github.twister716.universalmaterials.datagen.tag;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

/**
 * ブロックタグJSON（data/minecraft/tags/block/ 等）を自動生成するProvider。
 *
 * 生成するタグ:
 *   minecraft:mineable/pickaxe    → 全素材ブロックをツルハシで採掘可能にする
 *   素材のminingLevelタグ         → 必要採掘ツールレベルを登録する
 *   c:storage_blocks/<素材名>     → 保管ブロックの個別タグ
 *   c:storage_blocks              → 保管ブロックの親タグ
 *   minecraft:beacon_base_blocks  → GENERATE_BEACON_MATERIALフラグを持つ素材のブロック
 *
 * GENERATE_BEACON_MATERIALはflags()で付ける素材特性フラグなので
 * hasPropertyFlag()で判定する。
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
        }
    }

    private void registerBlockTags(Material material, String materialName, TagPrefix prefix) {
        ResourceLocation blockId = ResourceLocation.fromNamespaceAndPath(
                UniversalMaterials.MOD_ID, prefix.formatId(materialName));

        // ツルハシで採掘可能にする
        tag(BlockTags.MINEABLE_WITH_PICKAXE).addOptional(blockId);

        // 素材の採掘レベルタグを登録する
        tag(material.getMiningLevel()).addOptional(blockId);

        // c:タグ（個別 → 親）を登録する
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

        // GENERATE_BEACON_MATERIALはflags()で付ける素材特性フラグ → hasPropertyFlag()で判定する
        if (material.hasPropertyFlag(MaterialFlags.GENERATE_BEACON_MATERIAL)) {
            tag(BlockTags.BEACON_BASE_BLOCKS).addOptional(blockId);
        }
    }

    @Override
    public String getName() {
        return "Universal Materials Block Tag Provider";
    }
}