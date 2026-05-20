package io.github.twister716.universalmaterials.content.material;

import io.github.twister716.universalmaterials.api.material.AutoMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.iconset.MaterialIconSet;
import net.minecraft.tags.BlockTags;

import static io.github.twister716.universalmaterials.api.material.flag.MaterialFlags.*;

/**
 * 金属系素材の定義。
 * バニラには存在しない金属素材を登録する。
 */
@AutoMaterialRegistry
public class UMMetalMaterials extends UMMaterialRegistry {

    static {
        setModId("universalmaterials");
    }

    public UMMetalMaterials() {
        super("universalmaterials");
    }

    // 錫 / Tin
    public static final Material TIN = new Material.Builder("tin")
            .englishName("Tin")
            .element(50, "Sn")
            .description("A soft, malleable metal")
            .color(0xcbeaed).secondaryColor(0x909d9e)
            .iconSet(MaterialIconSet.DULL)
            .appendFlags(GENERATE_INGOT, GENERATE_NUGGET, GENERATE_STORAGE_BLOCK)
            .buildAndRegister();

    // 鉛 / Lead
    public static final Material LEAD = new Material.Builder("lead")
            .color(0x4A4A6A)
            .iconSet(MaterialIconSet.DULL)
            .miningLevel(BlockTags.NEEDS_IRON_TOOL)
            .appendFlags(GENERATE_INGOT, GENERATE_NUGGET, GENERATE_STORAGE_BLOCK)
            .buildAndRegister();

    // 銀 / Silver
    public static final Material SILVER = new Material.Builder("silver")
            .color(0xDCDCFF)
            .iconSet(MaterialIconSet.METALLIC)
            .miningLevel(BlockTags.NEEDS_IRON_TOOL)
            .appendFlags(GENERATE_INGOT, GENERATE_NUGGET, GENERATE_STORAGE_BLOCK)
            .buildAndRegister();
}