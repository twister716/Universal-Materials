package io.github.twister716.universalmaterials.datagen.loot;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * ブロックのルートテーブルJSON（data/universalmaterials/loot_table/blocks/）を自動生成するProvider。
 *
 * isBlock()がtrueのTagPrefixを持つ全ブロックに対して、
 * 「自分自身をドロップする」シンプルなルートテーブルを生成する。
 * 例: tin_block を壊すと tin_block がドロップする
 */
public class MaterialLootTableProvider extends LootTableProvider {

    public MaterialLootTableProvider(PackOutput output,
                                     CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(
                        MaterialBlockLootSubProvider::new,
                        LootContextParamSets.BLOCK
                )
        ), lookupProvider);
    }

    public static class MaterialBlockLootSubProvider extends BlockLootSubProvider {

        public MaterialBlockLootSubProvider(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
        }

        @Override
        protected void generate() {
            for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
                String materialName = material.getId().split(":")[1];

                for (MaterialFlag flag : material.getPartFlags()) {
                    flag.getTagPrefix().ifPresent(prefix -> {
                        if (prefix.isBlock()) registerSelfDrop(materialName, prefix);
                    });
                }

                if (!material.hasPropertyFlag(MaterialFlags.IS_VANILLA)) {
                    for (MaterialFlag flag : material.getImpliedFlags()) {
                        flag.getTagPrefix().ifPresent(prefix -> {
                            if (prefix.isBlock()) registerSelfDrop(materialName, prefix);
                        });
                    }
                }
            }
        }

        /**
         * ブロックを壊したときに自分自身をドロップするルートテーブルを登録する。
         * dropSelf()はNeoForge/Minecraftが用意している便利メソッドで、
         * 「このブロックをドロップする」という最もシンプルなルートテーブルを自動生成する。
         */
        private void registerSelfDrop(String materialName, TagPrefix prefix) {
            String blockId = prefix.formatId(materialName);
            Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                    .get(ResourceLocation.fromNamespaceAndPath(UniversalMaterials.MOD_ID, blockId));
            dropSelf(block);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            // このModのブロックだけを対象にする
            return net.minecraft.core.registries.BuiltInRegistries.BLOCK
                    .stream()
                    .filter(block -> net.minecraft.core.registries.BuiltInRegistries.BLOCK
                            .getKey(block).getNamespace()
                            .equals(UniversalMaterials.MOD_ID))
                    .toList();
        }
    }
}
