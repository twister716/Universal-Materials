package io.github.twister716.universalmaterials.content.material;

import io.github.twister716.universalmaterials.api.material.AutoMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.iconset.MaterialIconSet;

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
            .color(0xC8C8C8)
            .iconSet(MaterialIconSet.METALLIC)
            .buildAndRegister();

    // 金 / Gold
    public static final Material GOLD = new Material.Builder("gold")
            .color(0xFFD700)
            .iconSet(MaterialIconSet.METALLIC)
            .buildAndRegister();

    // 銅 / Copper
    public static final Material COPPER = new Material.Builder("copper")
            .color(0xFF6600)
            .iconSet(MaterialIconSet.METALLIC)
            .buildAndRegister();

    // ネザライト / Netherite
    public static final Material NETHERITE = new Material.Builder("netherite")
            .color(0x4A4A4A)
            .iconSet(MaterialIconSet.METALLIC)
            .appendFlags(GENERATE_INGOT, GENERATE_NUGGET, GENERATE_STORAGE_BLOCK)
            .buildAndRegister();
}