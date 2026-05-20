package io.github.twister716.universalmaterials.datagen.tag;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
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
 * ブロックタグJSON（data/minecraft/tags/block/・data/c/tags/block/）を自動生成するProvider。
 *
 * 生成するタグ:
 * - minecraft:mineable/pickaxe（全素材ブロックをツルハシで採掘可能にする）
 * - c:storage_blocks/<素材名>（保管ブロックの個別タグ）
 * - c:storage_blocks（保管ブロックの親タグ）
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

    /**
     * 1つのTagPrefixのブロックにタグを付ける。
     *
     * 例（STORAGE_BLOCKの場合）:
     *   minecraft:mineable/pickaxe → universalmaterials:tin_block を追加
     *   c:storage_blocks/tin      → universalmaterials:tin_block を追加
     *   c:storage_blocks          → #c:storage_blocks/tin を追加
     */
    private void registerBlockTags(Material material, String materialName, TagPrefix prefix) {
        ResourceLocation blockId = ResourceLocation.fromNamespaceAndPath(
                UniversalMaterials.MOD_ID, prefix.formatId(materialName));

        // ツルハシで採掘可能にする
        tag(BlockTags.MINEABLE_WITH_PICKAXE).addOptional(blockId);

        // 素材の採掘レベルタグを登録する
        // 例: BlockTags.NEEDS_STONE_TOOL → 石ツルハシ以上が必要
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
    }

    @Override
    public String getName() {
        return "Universal Materials Block Tag Provider";
    }
}

