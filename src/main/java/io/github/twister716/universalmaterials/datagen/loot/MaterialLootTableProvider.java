package io.github.twister716.universalmaterials.datagen.loot;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefixes;
import io.github.twister716.universalmaterials.api.ore.DimensionOreConfig;
import io.github.twister716.universalmaterials.api.ore.Ore;
import io.github.twister716.universalmaterials.api.ore.OreRegistry;
import io.github.twister716.universalmaterials.api.ore.OreSettings;
import io.github.twister716.universalmaterials.api.ore.StoneGroup;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * ブロックのルートテーブルJSON（data/universalmaterials/loot_table/blocks/）を自動生成するProvider。
 *
 * 登録するルートテーブル：
 *   保管ブロック（STORAGE_BLOCK等）→ dropSelf()で自分自身をドロップ
 *   鉱石ブロック → 3パターンを自動生成する
 *     ① シルクタッチ  → 鉱石ブロック自身をドロップ
 *     ② 通常採掘      → 原石（またはGEM素材はGEM）を dropMin〜dropMax 個ドロップ
 *     ③ 幸運          → ②にさらに幸運ボーナスが加算される（ApplyBonusCount）
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

        // 幸運エンチャントのHolderを取得するためにproviderを保持する
        private final HolderLookup.Provider provider;

        public MaterialBlockLootSubProvider(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
            this.provider = provider;
        }

        @Override
        protected void generate() {

            // ===== 保管ブロック・通常ブロック =====
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

                // ===== 鉱石ブロック =====
                if (material.hasPartFlag(MaterialFlags.GENERATE_ORE)
                        && OreRegistry.hasSettings(material)) {
                    registerOreDrop(material, materialName);
                }
            }
        }

        /**
         * 保管ブロック等の「自分自身をドロップ」するルートテーブルを登録する。
         */
        private void registerSelfDrop(String materialName, TagPrefix prefix) {
            String blockId = prefix.formatId(materialName);
            Block block = BuiltInRegistries.BLOCK.get(
                    ResourceLocation.fromNamespaceAndPath(UniversalMaterials.MOD_ID, blockId));
            dropSelf(block);
        }

        /**
         * 鉱石ブロックのドロップを全石種分登録する。
         *
         * ドロップするアイテムは素材フラグで決まる：
         *   IS_GEM フラグあり → GEM（宝石）アイテムをドロップ
         *   それ以外           → RAW_ORE（原石）アイテムをドロップ
         */
        private void registerOreDrop(Material material, String materialName) {
            OreSettings oreSettings = OreRegistry.getSettings(material);

            // 全ディメンション設定をまとめてOreを収集する
            List<DimensionOreConfig> configs = new ArrayList<>();
            configs.addAll(oreSettings.getOverworld());
            configs.addAll(oreSettings.getNether());
            configs.addAll(oreSettings.getEnd());

            // IS_GEM素材はGEMをドロップ、それ以外はRAW_OREをドロップ
            boolean isGem = material.hasPropertyFlag(MaterialFlags.IS_GEM);
            TagPrefix dropPrefix = isGem ? TagPrefixes.GEM : TagPrefixes.RAW_ORE;

            // バニラ素材（IS_VANILLA）はminecraftネームスペース、それ以外はUM
            String namespace = material.hasPropertyFlag(MaterialFlags.IS_VANILLA)
                    ? "minecraft"
                    : UniversalMaterials.MOD_ID;

            ResourceLocation dropLocation = ResourceLocation.fromNamespaceAndPath(
                    namespace, dropPrefix.formatId(materialName));
            Item dropItem = BuiltInRegistries.ITEM.get(dropLocation);

            // 同じOreを重複登録しないためSetで管理する
            Set<Ore> registered = new HashSet<>();
            for (DimensionOreConfig config : configs) {
                for (StoneGroup group : config.getStones()) {
                    for (Ore ore : group.getOres()) {
                        if (config.isExcluded(ore)) continue;
                        // vanillaStoneOres()で除外した石種はUMがブロックを登録しないためスキップする
                        if (oreSettings.getVanillaBaseOres().contains(ore)) continue;
                        if (!registered.add(ore)) continue;
                        registerOreBlockDrop(materialName, ore, dropItem, oreSettings);
                    }
                }
            }
        }

        /**
         * 鉱石ブロック1つ分のルートテーブルを登録する。
         *
         * createSilkTouchDispatchTable() でシルクタッチ判定を行い：
         *   シルクタッチあり → silkDropBlock（代表石種の鉱石ブロック）をドロップ
         *                      例: 花崗岩錫鉱石 → 石錫鉱石（STONEグループの代表）
         *   シルクタッチなし → dropItem を dropMin〜dropMax 個ドロップ
         *                      さらに幸運エンチャントで addOreBonusCount が加算される
         *
         * @param materialName 素材名（名前空間なし。例: "tin"）
         * @param ore          この鉱石ブロックの石種（例: OreTypes.GRANITE）
         * @param dropItem     通常採掘でドロップするアイテム（原石 or GEM）
         * @param oreSettings  ドロップ数の設定
         */
        private void registerOreBlockDrop(String materialName, Ore ore,
                                          Item dropItem, OreSettings oreSettings) {
            // 採掘対象のブロック（例: granite_tin_ore）
            String blockId = ore.formatBlockId(materialName);
            Block oreBlock = BuiltInRegistries.BLOCK.get(
                    ResourceLocation.fromNamespaceAndPath(UniversalMaterials.MOD_ID, blockId));

            // シルクタッチ時にドロップするブロック
            // → StoneGroupの代表Ore（リスト先頭）の鉱石ブロックに統一する
            // 例: 花崗岩錫鉱石をシルクタッチ → stone_tin_ore（石グループの代表）
            String silkBlockId = ore.getStoneGroup().getDefaultOre().formatBlockId(materialName);
            Block silkDropBlock = BuiltInRegistries.BLOCK.get(
                    ResourceLocation.fromNamespaceAndPath(UniversalMaterials.MOD_ID, silkBlockId));

            // ルートテーブルを構築して登録する
            // createSilkTouchDispatchTable() はNeoForgeのヘルパーで：
            //   第1引数: シルクタッチ時にドロップするブロック
            //   第2引数: 通常採掘時のルートエントリ（幸運ボーナス込み）
            add(oreBlock, createSilkTouchDispatchTable(
                    silkDropBlock,
                    applyExplosionDecay(oreBlock,
                            LootItem.lootTableItem(dropItem)
                                    // 通常ドロップ数: dropMin 〜 dropMax の範囲でランダム
                                    .apply(SetItemCountFunction.setCount(
                                            UniformGenerator.between(
                                                    oreSettings.getDropMin(),
                                                    oreSettings.getDropMax())))
                                    // 幸運エンチャント: ore_bonusルールでドロップ数を加算する
                                    // これはバニラの鉄・銅などと同じルールになる
                                    .apply(ApplyBonusCount.addOreBonusCount(
                                            provider.lookupOrThrow(Registries.ENCHANTMENT)
                                                    .getOrThrow(Enchantments.FORTUNE)))
                    )
            ));
        }

        /**
         * このModが登録した全ブロックを返す。
         * NeoForgeのLootTableProviderはここで返したブロック全部に
         * ルートテーブルが登録されているかを検証するため、
         * generate()で登録したブロックと一致させる必要がある。
         */
        @Override
        protected Iterable<Block> getKnownBlocks() {
            return BuiltInRegistries.BLOCK.stream()
                    .filter(block -> BuiltInRegistries.BLOCK
                            .getKey(block).getNamespace()
                            .equals(UniversalMaterials.MOD_ID))
                    .toList();
        }
    }
}