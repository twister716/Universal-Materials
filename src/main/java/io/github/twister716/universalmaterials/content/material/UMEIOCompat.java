package io.github.twister716.universalmaterials.content.material;

import io.github.twister716.universalmaterials.api.material.AutoMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.iconset.MaterialIconSet;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.SoundType;

import static io.github.twister716.universalmaterials.api.material.flag.MaterialFlags.*;

/**
 * 宝石系素材の定義。
 * ルビー・サファイアなどの宝石素材を登録する。
 * 将来的にGEM IconSetを追加予定。
 */
@AutoMaterialRegistry
public class UMEIOCompat extends UMMaterialRegistry {

    static {
        setModId("universalmaterials");
    }

    public UMEIOCompat() {
        super("universalmaterials");
    }

    // 脈動合金 / Pulsating Alloy
    public static final Material PULSATING_ALLOY = new Material.Builder("pulsating_alloy")
            .requireMod("enderio")
            .compatName("Ender IO")
            .englishName("Pulsating Alloy")
            .description("An alloy of iron with added Ender Power")
            .color(0x3cffb5)
            .iconSet(MaterialIconSet.METALLIC)
            .appendFlags(GENERATE_INGOT, GENERATE_NUGGET, GENERATE_STORAGE_BLOCK, GENERATE_DUST)
            .flags(GENERATE_BEACON_MATERIAL)
            .miningLevel(BlockTags.NEEDS_IRON_TOOL).soundType(SoundType.METAL)
            .buildAndRegister();

    // 電導合金 / Conductive Alloy
    public static final Material CONDUCTIVE_ALLOY = new Material.Builder("conductive_alloy")
            .requireMod("enderio")
            .compatName("Ender IO")
            .englishName("Conductive Alloy")
            .description("An electrically conductive alloy made of iron and copper")
            .color(0xffae8f)
            .iconSet(MaterialIconSet.METALLIC)
            .appendFlags(GENERATE_INGOT, GENERATE_NUGGET, GENERATE_STORAGE_BLOCK, GENERATE_DUST)
            .flags(GENERATE_BEACON_MATERIAL)
            .miningLevel(BlockTags.NEEDS_IRON_TOOL).soundType(SoundType.METAL)
            .buildAndRegister();
}