package io.github.twister716.universalmaterials.client.texture;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.iconset.MaterialIconSet;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import io.github.twister716.universalmaterials.api.ore.DimensionOreConfig;
import io.github.twister716.universalmaterials.api.ore.Ore;
import io.github.twister716.universalmaterials.api.ore.OreRegistry;
import io.github.twister716.universalmaterials.api.ore.StoneGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 素材テクスチャ・鉱石テクスチャを動的に生成してVirtualPackResourcesに登録するクラス。
 *
 * テクスチャのパス構築:
 *   通常アイテム: universalmaterials:textures/item/material_sets/<rootIconSet>/<iconType>.png
 *   鉱石オーバーレイ: universalmaterials:textures/block/material_sets/<rootIconSet>/ore.png
 *   石テクスチャ: minecraft:textures/<ore.getTexturePath()>.png
 *
 * ルートIconSetはparentチェーンを辿って取得する（Minecraftのリソース機構に依存しない方法）。
 */
@EventBusSubscriber(modid = UniversalMaterials.MOD_ID, value = Dist.CLIENT)
public class TextureGenerator {

    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    private static boolean isGenerated = false;

    static final VirtualPackResources VIRTUAL_PACK = new VirtualPackResources(
            new PackLocationInfo(
                    UniversalMaterials.MOD_ID + "_virtual",
                    Component.literal("Universal Materials Virtual Pack"),
                    PackSource.BUILT_IN,
                    Optional.empty()
            )
    );

    public static void setResourceManager(ResourceManager manager) {
        VIRTUAL_PACK.setResourceManager(manager);
        isGenerated = false;
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        event.addRepositorySource(packConsumer -> {
            Pack pack = Pack.readMetaAndCreate(
                    new PackLocationInfo(
                            UniversalMaterials.MOD_ID + "_virtual",
                            Component.literal("Universal Materials Virtual Pack"),
                            PackSource.BUILT_IN,
                            Optional.empty()
                    ),
                    new Pack.ResourcesSupplier() {
                        @Override
                        public net.minecraft.server.packs.PackResources openPrimary(
                                PackLocationInfo info) {
                            return VIRTUAL_PACK;
                        }
                        @Override
                        public net.minecraft.server.packs.PackResources openFull(
                                PackLocationInfo info, Pack.Metadata metadata) {
                            return VIRTUAL_PACK;
                        }
                    },
                    PackType.CLIENT_RESOURCES,
                    new PackSelectionConfig(true, Pack.Position.TOP, false)
            );
            if (pack != null) packConsumer.accept(pack);
        });
    }

    public static synchronized void generateAll(ResourceManager resourceManager) {
        if (isGenerated) return;
        isGenerated = true;

        VIRTUAL_PACK.clear();
        LOGGER.info("[TextureGenerator] テクスチャ生成を開始します...");
        int count = 0;

        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {

            // GENERATE_OREフラグを持つ素材は鉱石テクスチャを生成する（IS_VANILLAでも）
            if (material.hasPartFlag(MaterialFlags.GENERATE_ORE)
                    && OreRegistry.hasSettings(material)) {
                count += generateOreBlockTextures(resourceManager, material);
            }

            if (material.hasPropertyFlag(MaterialFlags.IS_VANILLA)) {
                count += processFlags(resourceManager, material, material.getPartFlags());
                continue;
            }

            count += processFlags(resourceManager, material, material.getPartFlags());
            count += processFlags(resourceManager, material, material.getImpliedFlags());
        }

        LOGGER.info("[TextureGenerator] テクスチャ生成完了: {}枚", count);
    }

    private static int processFlags(ResourceManager resourceManager,
                                    Material material, Set<MaterialFlag> flags) {
        int count = 0;
        for (MaterialFlag flag : flags) {
            if (flag.getTagPrefix().isEmpty()) continue;
            TagPrefix prefix = flag.getTagPrefix().get();
            if (prefix.getIconType() == null) continue;

            String materialName = material.getId().split(":")[1];
            String id = prefix.formatId(materialName);

            ResourceLocation customLocation = getCustomTextureLocation(prefix, id);
            if (resourceManager.getResource(customLocation).isPresent()) continue;

            // material.noColorize(prefix) が設定されていれば着色生成をスキップする
            // （バニラ素材の専用テクスチャを持つPrefixはバニラ側のテクスチャをそのまま使う）
            if (material.isNoColorize(prefix)) continue;

            BufferedImage generated = generateColoredTexture(resourceManager, material, prefix);
            if (generated == null) continue;

            ResourceLocation outLocation = getGeneratedTextureLocation(prefix, id);
            VIRTUAL_PACK.addTexture(outLocation, generated);
            count++;
        }
        return count;
    }

    /**
     * 1つのTagPrefix分のテクスチャを着色して返す。
     *
     * パス構築: "textures/<type>/material_sets/<rootIconSet>/<iconType>.png"
     * rootIconSetはparentチェーンを辿って取得する（hasOwnTexture()に依存しない）。
     */
    private static BufferedImage generateColoredTexture(ResourceManager resourceManager,
                                                        Material material, TagPrefix prefix) {
        MaterialIconSet iconSet = material.getIconSet();
        if (iconSet == null) return null;

        String type      = prefix.isBlock() ? "block" : "item";
        String iconType  = prefix.getIconType();

        // IconSet自身のフォルダにテクスチャがあればそれを使い、なければ親を辿る
        ResourceLocation texLocation = resolveTextureLocation(
                resourceManager, iconSet, type, iconType);
        if (texLocation == null) return null;

        BufferedImage base = loadTexture(resourceManager, texLocation);
        if (base == null) return null;

        return TextureColorizer.colorize(base, material.getColor(), material.getSecondaryColor());
    }

    // ===== 鉱石ブロック専用テクスチャ生成 =====

    private static int generateOreBlockTextures(ResourceManager resourceManager,
                                                Material material) {
        var settings = OreRegistry.getSettings(material);
        String materialName = material.getId().split(":")[1];
        int count = 0;

        List<DimensionOreConfig> configs = new ArrayList<>();
        configs.addAll(settings.getOverworld());
        configs.addAll(settings.getNether());
        configs.addAll(settings.getEnd());

        Set<Ore> registered = new HashSet<>();
        for (DimensionOreConfig config : configs) {
            for (StoneGroup group : config.getStones()) {
                for (Ore ore : group.getOres()) {
                    if (config.isExcluded(ore)) continue;
                    // vanillaStoneOres()で指定した石種はUMがテクスチャを生成しない
                    if (settings.getVanillaBaseOres().contains(ore)) continue;
                    if (!registered.add(ore)) continue;
                    // OreSettingsをgenerateOreTextureForOreに渡す（着色色の優先解決のため）
                    if (generateOreTextureForOre(resourceManager, material, materialName, ore, settings)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static boolean generateOreTextureForOre(ResourceManager resourceManager,
                                                    Material material, String materialName,
                                                    Ore ore,
                                                    io.github.twister716.universalmaterials.api.ore.OreSettings settings) {
        String blockId = ore.formatBlockId(materialName);

        ResourceLocation customLocation = ResourceLocation.fromNamespaceAndPath(
                UniversalMaterials.MOD_ID,
                "textures/block/custom/ore/" + blockId + ".png");
        if (resourceManager.getResource(customLocation).isPresent()) return false;

        BufferedImage generated = generateOreTexture(resourceManager, material, ore, settings);
        if (generated == null) return false;

        ResourceLocation outLocation = ResourceLocation.fromNamespaceAndPath(
                UniversalMaterials.MOD_ID,
                "textures/block/generated/" + blockId + ".png");
        VIRTUAL_PACK.addTexture(outLocation, generated);
        return true;
    }

    /**
     * 石テクスチャ + 着色済みオーバーレイを合成して返す。
     *
     * 着色色の優先順位:
     *   1. OreSettings.getOrePrimaryColor() が -1 以外 → OreSettings.oreColor()で設定した色
     *   2. Ore.getOrePrimaryColor() が -1 以外        → OreBuilder.oreColor()で設定した色（石種別）
     *   3. いずれも -1                                → material.getColor()にフォールバック
     */
    private static BufferedImage generateOreTexture(ResourceManager resourceManager,
                                                    Material material, Ore ore,
                                                    io.github.twister716.universalmaterials.api.ore.OreSettings settings) {
        // 石テクスチャを読み込む
        ResourceLocation stoneLocation = ResourceLocation.withDefaultNamespace(
                "textures/" + ore.getTexturePath() + ".png");
        BufferedImage stoneBase = loadTexture(resourceManager, stoneLocation);
        if (stoneBase == null) {
            LOGGER.warn("[TextureGenerator] 石テクスチャが見つかりません: {}", stoneLocation);
            return null;
        }

        // アニメーションテクスチャは先頭フレームだけ使う
        int tileSize = stoneBase.getWidth();
        if (stoneBase.getHeight() > tileSize) {
            stoneBase = stoneBase.getSubimage(0, 0, tileSize, tileSize);
        }
        if (stoneBase.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage converted = new BufferedImage(
                    stoneBase.getWidth(), stoneBase.getHeight(), BufferedImage.TYPE_INT_ARGB);
            converted.getGraphics().drawImage(stoneBase, 0, 0, null);
            stoneBase = converted;
        }

        // オーバーレイテクスチャを読み込む
        // IconSet自身のフォルダにore.pngがあればそれを使い、なければ親を辿る
        MaterialIconSet iconSet = material.getIconSet();
        if (iconSet == null) return stoneBase;

        BufferedImage overlay = loadOverlayWithFallback(resourceManager, iconSet);
        if (overlay == null) {
            LOGGER.warn("[TextureGenerator] 鉱石オーバーレイが見つかりません（フォールバック含む）: {}",
                    iconSet.getName());
            return stoneBase;
        }

        if (overlay.getHeight() > tileSize) {
            overlay = overlay.getSubimage(0, 0, tileSize, tileSize);
        }

        // 着色色を決定する: OreSettings → Ore（石種別） → 素材 の順で優先する
        int primary, secondary;
        if (settings != null && settings.getOrePrimaryColor() != -1) {
            // OreSettings.oreColor()で明示的に設定された色を使う
            primary   = settings.getOrePrimaryColor();
            secondary = settings.getOreSecondaryColor() != -1
                    ? settings.getOreSecondaryColor()
                    : material.getSecondaryColor();
        } else if (ore.getOrePrimaryColor() != -1) {
            // OreBuilder.oreColor()で石種別に設定された色を使う
            primary   = ore.getOrePrimaryColor();
            secondary = ore.getOreSecondaryColor() != -1
                    ? ore.getOreSecondaryColor()
                    : material.getSecondaryColor();
        } else {
            // 未設定の場合は素材の色にフォールバックする
            primary   = material.getColor();
            secondary = material.getSecondaryColor();
        }

        // colorizeOverlayもOreSettings → Ore の順で優先する
        boolean shouldColorize = (settings != null)
                ? settings.isColorizeOverlay()
                : ore.isColorizeOverlay();

        // shouldColorize=true  → グレースケールオーバーレイを素材色で着色してから合成
        // shouldColorize=false → オーバーレイをそのまま（着色なしで）合成する
        BufferedImage coloredOverlay = shouldColorize
                ? TextureColorizer.colorize(overlay, primary, secondary, true)
                : overlay;

        return TextureColorizer.compositeOver(stoneBase, coloredOverlay);
    }

    // ===== ユーティリティ =====

    /**
     * IconSet自身のフォルダにテクスチャがあればそのパスを返し、
     * なければ親を辿ってフォールバックする。
     * どこにも見つからなければnullを返す。
     */
    private static ResourceLocation resolveTextureLocation(ResourceManager resourceManager,
                                                           MaterialIconSet iconSet,
                                                           String type, String iconType) {
        MaterialIconSet current = iconSet;
        while (current != null) {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(
                    UniversalMaterials.MOD_ID,
                    "textures/" + type + "/material_sets/" + current.getName() + "/" + iconType + ".png");
            if (resourceManager.getResource(loc).isPresent()) return loc;
            current = current.getParent();
        }
        return null;
    }

    /**
     * 鉱石オーバーレイ（ore.png）をIconSetのフォルダから検索し、
     * なければ親を辿ってフォールバックしたテクスチャを返す。
     */
    private static BufferedImage loadOverlayWithFallback(ResourceManager resourceManager,
                                                         MaterialIconSet iconSet) {
        MaterialIconSet current = iconSet;
        while (current != null) {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(
                    UniversalMaterials.MOD_ID,
                    "textures/block/material_sets/" + current.getName() + "/ore.png");
            BufferedImage img = loadTexture(resourceManager, loc);
            if (img != null) {
                LOGGER.debug("[TextureGenerator] オーバーレイ取得: {}", loc);
                return img;
            }
            current = current.getParent();
        }
        return null;
    }

    private static BufferedImage loadTexture(ResourceManager manager, ResourceLocation location) {
        return manager.getResource(location).map(resource -> {
            try (InputStream in = resource.open()) {
                return ImageIO.read(in);
            } catch (IOException e) {
                LOGGER.error("[TextureGenerator] 読み込みエラー: {} / {}", location, e.getMessage());
                return null;
            }
        }).orElse(null);
    }

    private static ResourceLocation getCustomTextureLocation(TagPrefix prefix, String id) {
        String path = prefix.isBlock()
                ? "textures/block/custom/" + prefix.getId() + "/" + id + ".png"
                : "textures/item/custom/" + prefix.getId() + "/" + id + ".png";
        return ResourceLocation.fromNamespaceAndPath(UniversalMaterials.MOD_ID, path);
    }

    private static ResourceLocation getGeneratedTextureLocation(TagPrefix prefix, String id) {
        String path = prefix.isBlock()
                ? "textures/block/generated/" + id + ".png"
                : "textures/item/generated/" + id + ".png";
        return ResourceLocation.fromNamespaceAndPath(UniversalMaterials.MOD_ID, path);
    }
}