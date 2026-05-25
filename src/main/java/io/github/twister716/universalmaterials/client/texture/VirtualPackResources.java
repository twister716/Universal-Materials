package io.github.twister716.universalmaterials.client.texture;

import io.github.twister716.universalmaterials.UniversalMaterials;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 実際のファイルを持たない仮想リソースパック。
 *
 * Minecraftがテクスチャを要求したとき、メモリ上で動的に生成した
 * 着色済みPNGを返す仕組み。
 *
 * 流れ:
 *   1. TextureGeneratorがaddTexture()で生成済み画像を登録する
 *   2. MinecraftがlistResources()を呼ぶ → TextureGeneratorが遅延生成する
 *   3. MinecraftがgetResource()を呼ぶ → 登録済み画像をInputStreamで返す
 *
 * listResources()が呼ばれた時点でResourceManagerを取得する方法:
 *   - setResourceManager()でセットされていればそれを使う
 *   - セットされていなければMinecraft.getInstance().getResourceManager()から取得する
 *   これにより、リロードリスナーのタイミングに依存しない。
 */
public class VirtualPackResources extends AbstractPackResources {

    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    private final Map<String, BufferedImage> textures = new HashMap<>();

    // setResourceManager()でセットされた場合のみ非null
    private ResourceManager cachedResourceManager = null;

    public VirtualPackResources(PackLocationInfo info) {
        super(info);
    }

    public void addTexture(ResourceLocation location, BufferedImage image) {
        textures.put(toPackPath(location), image);
    }

    public void clear() {
        textures.clear();
    }

    public void setResourceManager(ResourceManager manager) {
        this.cachedResourceManager = manager;
    }

    // ===== AbstractPackResources の実装 =====

    @Override
    public IoSupplier<InputStream> getRootResource(String... paths) {
        if (paths.length == 1 && paths[0].equals("pack.mcmeta")) {
            String mcmeta = """
                {
                  "pack": {
                    "description": "Universal Materials Virtual Pack",
                    "pack_format": 34
                  }
                }
                """;
            return () -> new ByteArrayInputStream(
                    mcmeta.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.CLIENT_RESOURCES) return null;
        BufferedImage image = textures.get(toPackPath(location));
        if (image == null) return null;
        return () -> bufferedImageToStream(image);
    }

    /**
     * テクスチャアトラス構築の直前に呼ばれる。
     *
     * ResourceManagerの取得優先順位:
     *   1. setResourceManager()でセットされた値（リロードリスナー経由）
     *   2. Minecraft.getInstance().getResourceManager()（直接取得）
     *
     * 両方ともnullの場合は生成をスキップする（起動直後など）。
     */
    @Override
    public void listResources(PackType type, String namespace, String path,
                              ResourceOutput resourceOutput) {
        if (type != PackType.CLIENT_RESOURCES) return;
        if (!namespace.equals(UniversalMaterials.MOD_ID)) return;

        if (textures.isEmpty()) {
            // ResourceManagerを取得する（セット済みがなければMinecraftから直接取得）
            ResourceManager manager = cachedResourceManager;
            if (manager == null) {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null) {
                    manager = mc.getResourceManager();
                }
            }

            if (manager != null) {
                TextureGenerator.generateAll(manager);
            } else {
                LOGGER.warn("[VirtualPack] ResourceManager could not be obtained. Texture generation will be skipped.");
                return;
            }
        }

        LOGGER.debug("[VirtualPack] listResources: namespace={} path={} textures={}",
                namespace, path, textures.size());

        String prefix = namespace + ":" + path;
        for (Map.Entry<String, BufferedImage> entry : textures.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                ResourceLocation location = toResourceLocation(entry.getKey());
                if (location != null) {
                    BufferedImage image = entry.getValue();
                    resourceOutput.accept(location, () -> bufferedImageToStream(image));
                }
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type == PackType.CLIENT_RESOURCES) return Set.of(UniversalMaterials.MOD_ID);
        return Set.of();
    }

    @Override
    public void close() {}

    @Override
    public String packId() {
        return UniversalMaterials.MOD_ID + "_virtual";
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    private static String toPackPath(ResourceLocation location) {
        return location.getNamespace() + ":" + location.getPath();
    }

    private static ResourceLocation toResourceLocation(String packPath) {
        int colon = packPath.indexOf(':');
        if (colon < 0) return null;
        return ResourceLocation.fromNamespaceAndPath(
                packPath.substring(0, colon),
                packPath.substring(colon + 1));
    }

    private static InputStream bufferedImageToStream(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }
}