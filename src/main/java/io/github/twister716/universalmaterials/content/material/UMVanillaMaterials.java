package io.github.twister716.universalmaterials.content.material;

import io.github.twister716.universalmaterials.api.material.AutoMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.iconset.MaterialIconSet;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefixes;
import net.minecraft.tags.BlockTags;

import static io.github.twister716.universalmaterials.api.material.flag.MaterialFlags.*;

/**
 * バニラMinecraftに存在する素材の定義。
 * 鉄・金・銅など、Minecraft本体が持つ素材をUMの素材システムに登録する。
 */
@AutoMaterialRegistry
public class UMVanillaMaterials extends UMMaterialRegistry {

    static {
        setModId("universalmaterials");
    }

    public UMVanillaMaterials() {
        super("universalmaterials");
    }

    // 鉄 / Iron
    public static final Material IRON = new Material.Builder("iron")
            .color(0xf4f4ee)
            .iconSet(MaterialIconSet.IRON)
            .appendFlags(GENERATE_ORE)
            .flags(IS_VANILLA)
            .buildAndRegister();

    // 金 / Gold
    public static final Material GOLD = new Material.Builder("gold")
            .color(0xFFD700)
            .iconSet(MaterialIconSet.GOLD)
            .miningLevel(BlockTags.NEEDS_IRON_TOOL)
            .appendFlags(GENERATE_ORE)
            .flags(IS_VANILLA)
            .buildAndRegister();

    // 銅 / Copper
    public static final Material COPPER = new Material.Builder("copper")
            .color(0xFF6600)
            .iconSet(MaterialIconSet.COPPER)
            .appendFlags(GENERATE_NUGGET, GENERATE_ORE)
            .flags(IS_VANILLA)
            .buildAndRegister();

    // ネザライト / Netherite
    public static final Material NETHERITE = new Material.Builder("netherite")
            .color(0x4A4A4A)
            .iconSet(MaterialIconSet.METALLIC)
            .miningLevel(BlockTags.NEEDS_DIAMOND_TOOL)
            .appendFlags(GENERATE_NUGGET)
            .flags(IS_VANILLA)
            .buildAndRegister();
}