package io.github.twister716.universalmaterials.api.ore;

import io.github.twister716.universalmaterials.api.material.Material;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 素材（Material）と鉱石生成設定（OreSettings）を結びつけるレジストリ。
 *
 * Material自体にはOreSettingsを持たせない設計にすることで、
 * MaterialのBuilderが肥大化するのを防ぐ。
 *
 * OreDefinitions.java でこのクラスの register() を使って登録する。
 *
 * 使い方:
 *   OreRegistry.register(UMMetalMaterials.TIN, OreSettings.TIN);
 *
 * DatagenやUMItemsからは getSettings(material) で設定を取得する。
 */
public class OreRegistry {

    // 登録順を保持するためLinkedHashMapを使う
    private static final Map<Material, OreSettings> REGISTRY = new LinkedHashMap<>();
    private static final Map<Material, OreSettings> REGISTRY_VIEW =
            Collections.unmodifiableMap(REGISTRY);

    /**
     * 素材と鉱石生成設定を結びつけて登録する。
     *
     * @param material    鉱石を生成する素材
     * @param oreSettings その素材の鉱石生成設定
     */
    public static void register(Material material, OreSettings oreSettings) {
        if (REGISTRY.containsKey(material)) {
            throw new IllegalArgumentException(
                    "OreSettings for \"" + material.getId() + "\" is already registered!");
        }
        REGISTRY.put(material, oreSettings);
    }

    /**
     * 素材に対応するOreSettingsを返す。
     * 登録されていない場合はnullを返す。
     *
     * @param material 素材
     * @return 対応するOreSettings。未登録の場合はnull
     */
    public static OreSettings getSettings(Material material) {
        return REGISTRY.get(material);
    }

    /** 素材がOreSettingsを持っているかどうかを返す。 */
    public static boolean hasSettings(Material material) {
        return REGISTRY.containsKey(material);
    }

    /** 登録済みの全エントリを返す（読み取り専用）。DatagenやUMItemsで使う。 */
    public static Map<Material, OreSettings> getAll() {
        return REGISTRY_VIEW;
    }

    /**
     * 指定した素材とOreSettingsの組み合わせから、UMが実際に登録するOreのSetを返す。
     *
     * vanillaStoneOres()で除外した石種と、excludeOres()で除外した石種は含まない。
     * UMItems・LootTable・BlockTag・ModelProvider・ColorHandlerなど、
     * 「UMが登録したブロック」を扱う全クラスでこのメソッドを使うことで
     * 除外ロジックを一箇所に集約できる。
     *
     * @param settings 鉱石生成設定
     * @return UMが登録するOreのSet（重複なし・登録順保持）
     */
    public static java.util.Set<Ore> getUMOres(OreSettings settings) {
        java.util.Set<Ore> seen   = new java.util.HashSet<>();
        java.util.Set<Ore> result = new java.util.LinkedHashSet<>();

        for (var config : settings.getOverworld()) collectUMOres(config, settings, seen, result);
        for (var config : settings.getNether())    collectUMOres(config, settings, seen, result);
        for (var config : settings.getEnd())       collectUMOres(config, settings, seen, result);

        return result;
    }

    private static void collectUMOres(
            io.github.twister716.universalmaterials.api.ore.DimensionOreConfig config,
            OreSettings settings,
            java.util.Set<Ore> seen,
            java.util.Set<Ore> result) {
        for (var group : config.getStones()) {
            for (Ore ore : group.getOres()) {
                if (config.isExcluded(ore)) continue;
                if (settings.getVanillaBaseOres().contains(ore)) continue;
                if (seen.add(ore)) result.add(ore);
            }
        }
    }

    private OreRegistry() {}
}