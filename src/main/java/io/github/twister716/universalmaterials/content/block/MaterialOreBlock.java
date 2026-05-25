package io.github.twister716.universalmaterials.content.block;

import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.ore.Ore;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 素材と石の種類の組み合わせから自動生成される鉱石ブロック。
 *
 * 例: 素材TIN + Ore DEEPSLATE → deepslate_tin_ore ブロック
 *
 * 物性（硬度・爆発耐性・サウンド）は素材とOreの設定から決まる。
 * 採掘ツール要件（鉄ピッケル以上など）は requiresCorrectToolForDrops() で有効にし、
 * どのツールが必要かは MaterialBlockTagProvider で素材の miningLevel タグに
 * 鉱石ブロックを登録することで定義する。
 */
public class MaterialOreBlock extends Block {

    private final Material material;
    private final Ore ore;

    public MaterialOreBlock(Material material, Ore ore) {
        super(BlockBehaviour.Properties.of()
                .strength(3.0f + ore.getHardness(), 3.0f + ore.getResistance())
                .sound(ore.getSoundType())
                // これを付けることで「正しいツールでないとドロップしない」が有効になる
                // どのツールが「正しい」かはBlockTagで定義する（MaterialBlockTagProvider参照）
                .requiresCorrectToolForDrops());
        this.material = material;
        this.ore      = ore;
    }

    public Material getMaterial() { return material; }
    public Ore getOre()           { return ore; }
}