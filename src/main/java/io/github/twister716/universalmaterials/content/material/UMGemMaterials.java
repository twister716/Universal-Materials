package io.github.twister716.universalmaterials.content.material;

import io.github.twister716.universalmaterials.api.material.AutoMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.iconset.MaterialIconSet;

import static io.github.twister716.universalmaterials.api.material.flag.MaterialFlags.*;

/**
 * 宝石系素材の定義。
 * ルビー・サファイアなどの宝石素材を登録する。
 * 将来的にGEM IconSetを追加予定。
 */
@AutoMaterialRegistry
public class UMGemMaterials extends UMMaterialRegistry {

    static {
        setModId("universalmaterials");
    }

    public UMGemMaterials() {
        super("universalmaterials");
    }

    // ルビー / Ruby
    public static final Material RUBY = new Material.Builder("ruby")
            .color(0xFF0000)
            .iconSet(MaterialIconSet.METALLIC)
            .buildAndRegister();

    // サファイア / Sapphire
    public static final Material SAPPHIRE = new Material.Builder("sapphire")
            .color(0x0000FF)
            .iconSet(MaterialIconSet.METALLIC)
            .appendFlags(GENERATE_STORAGE_BLOCK)
            .buildAndRegister();
}