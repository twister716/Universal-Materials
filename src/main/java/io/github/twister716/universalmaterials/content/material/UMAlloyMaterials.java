package io.github.twister716.universalmaterials.content.material;

import io.github.twister716.universalmaterials.api.material.AutoMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.iconset.MaterialIconSet;

import static io.github.twister716.universalmaterials.api.material.flag.MaterialFlags.*;

/**
 * 合金系素材の定義。
 * 複数の素材を組み合わせて作る合金を登録する。
 * requireMaterial()で前提素材を指定し、前提素材が全て有効な場合のみ登録される。
 */
@AutoMaterialRegistry
public class UMAlloyMaterials extends UMMaterialRegistry {

    static {
        setModId("universalmaterials");
    }

    public UMAlloyMaterials() {
        super("universalmaterials");
    }

    // 鋼鉄 / Steel
    public static final Material STEEL = new Material.Builder("steel")
            .englishName("Steel")
            .color(0x808080)
            .iconSet(MaterialIconSet.METALLIC)
            .appendFlags(GENERATE_INGOT, GENERATE_NUGGET, GENERATE_STORAGE_BLOCK)
            .requireMaterial(UMVanillaMaterials.IRON)
            .buildAndRegister();

    // 青銅 / Bronze
    public static final Material BRONZE = new Material.Builder("bronze")
            .color(0xCD7F32)
            .iconSet(MaterialIconSet.METALLIC)
            .appendFlags(GENERATE_INGOT, GENERATE_NUGGET, GENERATE_STORAGE_BLOCK)
            .requireMaterial(UMVanillaMaterials.COPPER, UMMetalMaterials.TIN)
            .buildAndRegister();
}