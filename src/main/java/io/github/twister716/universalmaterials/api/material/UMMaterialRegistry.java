package io.github.twister716.universalmaterials.api.material;

import net.neoforged.neoforgespi.language.ModFileScanData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 素材登録の窓口となる基底クラス。
 * このクラスを継承してModIDを宣言することで、
 * 登録する素材に自動的にModIDが付与される。
 *
 * 使用例（UM側）:
 *   public class UMMaterials extends UMMaterialRegistry {
 *       public UMMaterials() { super("universalmaterials"); }
 *
 *       public static final Material TIN = register(
 *           new Material.Builder("tin")
 *               .color(0xDCDCDC)
 *               .iconSet(MaterialIconSet.DULL)
 *               .build()
 *       );
 *   }
 *
 * requireMod・requireMaterialの条件を満たさない素材は登録をスキップする。
 */
public abstract class UMMaterialRegistry {

    // 登録済みのMaterialをIDで管理するマップ（内部用）
    private static final Map<String, Material> REGISTRY = new HashMap<>();

    // 外部から参照できる読み取り専用のREGISTRYのビュー
    private static final Map<String, Material> REGISTRY_VIEW =
            Collections.unmodifiableMap(REGISTRY);

    // 登録順を保持するリスト（CreativeTabの並び順などに使う）
    private static final List<Material> REGISTRATION_ORDER = new ArrayList<>();

    // レジストリがロックされているかどうかのフラグ
    private static boolean locked = false;

    // KubeJSのModID定数
    private static final String KUBEJS_MODID = "kubejs";

    // static初期化子でregister()が呼ばれるときModIDを渡すためのThreadLocal
    // ThreadLocalを使うことで、複数のRegistryクラスが同時に動いても混線しない
    private static final ThreadLocal<String> CURRENT_MOD_ID = new ThreadLocal<>();

    // ==================== コンストラクタ ====================

    /**
     * UMMaterialRegistryを初期化する。
     * 継承先はsuper("modid")でModIDを宣言する。
     *
     * @param modId このRegistryを持つModのModID
     */
    protected UMMaterialRegistry(String modId) {
        CURRENT_MOD_ID.set(modId);
    }

    // ==================== レジストリ操作 ====================

    /**
     * Materialをレジストリに登録する。
     * CURRENT_MOD_IDから呼び出し元のModIDを自動取得してIDを付与する。
     *
     * requireMod・requireMaterialの条件を満たさない素材は登録をスキップする。
     * スキップされた素材はアイテム・ブロック・レシピも生成されない。
     *
     * @param material 登録するMaterial
     * @return 登録されたMaterial（条件を満たさない場合もMaterialオブジェクト自体は返す）
     * @throws IllegalStateException    レジストリがロック済みの場合
     * @throws IllegalArgumentException KubeJS以外のModが同名Materialを登録しようとした場合
     */
    protected static Material register(Material material) {
        if (locked) {
            throw new IllegalStateException(
                    "The Material registry is locked, so you cannot add new entries!"
            );
        }

        // ThreadLocalからModIDを取得してIDを付与する
        String modId = CURRENT_MOD_ID.get();
        String fullId = modId + ":" + material.getId();
        material.setId(fullId);

        // requireMod・requireMaterialの条件チェック
        // 条件を満たさない場合は登録をスキップする
        if (!material.isEnabled()) {
            return material;
        }

        // 同名のMaterialが既に登録されている場合の処理
        if (REGISTRY.containsKey(fullId)) {
            if (KUBEJS_MODID.equals(modId)) {
                // KubeJSからの呼び出しなら上書きを許可する
                REGISTRY.put(fullId, material);
            } else {
                throw new IllegalArgumentException(
                        "Material \"" + fullId + "\" is already registered!"
                );
            }
        } else {
            // 新規登録
            REGISTRY.put(fullId, material);
            REGISTRATION_ORDER.add(material);
        }

        return material;
    }

    /** 現在のModIDを手動で設定する。外部Modがstaticブロック外で使う場合に使用する。 */
    protected static void setModId(String modId) {
        CURRENT_MOD_ID.set(modId);
    }

    /**
     * @AutoMaterialRegistry が付いた全クラスを検索してインスタンス化する。
     * NeoForgeのModFileScanDataを使ってクラスパス上の全クラスをスキャンする。
     * UniversalMaterials.javaのFMLLoadCompleteEventで呼び出す想定。
     *
     * @param scanData NeoForgeがModロード時に生成するスキャンデータ
     */
    public static void initAll(ModFileScanData scanData) {
        // アノテーションのクラス名を取得する
        // Type.getDescriptor()でJVM内部形式の文字列（例: "Lio/github/.../AutoMaterialRegistry;"）を得る
        String annotationDesc = org.objectweb.asm.Type
                .getDescriptor(AutoMaterialRegistry.class);

        // scanDataから@AutoMaterialRegistryが付いたクラスを全て取得する
        scanData.getAnnotations().stream()
                .filter(data -> annotationDesc.equals(data.annotationType().getDescriptor()))
                .forEach(data -> {
                    try {
                        // クラス名からClassオブジェクトを取得する
                        Class<?> clazz = Class.forName(data.clazz().getClassName());

                        // UMMaterialRegistryのサブクラスかどうか確認する
                        if (!UMMaterialRegistry.class.isAssignableFrom(clazz)) return;

                        // 引数なしコンストラクタでインスタンス化する
                        // これによりsuper("modid")が呼ばれてCURRENT_MOD_IDがセットされ、
                        // staticフィールドの初期化（buildAndRegister()）が走る
                        clazz.getDeclaredConstructor().newInstance();

                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Failed to initialize MaterialRegistry: "
                                        + data.clazz().getClassName(), e
                        );
                    }
                });
    }

    /**
     * レジストリをロックする。
     * ロック後は新規追加・編集・削除が一切禁止される。
     * UMの初期化完了時に呼び出す想定。
     */
    public static void lockRegistry() {
        locked = true;
    }

    /** レジストリがロック済みかどうかを返す。 */
    public static boolean isLocked() {
        return locked;
    }

    /** 読み取り専用のレジストリビューを返す。 */
    public static Map<String, Material> getRegistry() {
        return REGISTRY_VIEW;
    }

    /**
     * 登録順のMaterialリストを返す。
     * CreativeTabの並び順などに使う。
     *
     * @return 登録順の読み取り専用Materialリスト
     */
    public static List<Material> getRegistrationOrder() {
        return Collections.unmodifiableList(REGISTRATION_ORDER);
    }

    /**
     * IDからMaterialを取得する。
     *
     * @param id 識別ID（例: "universalmaterials:tin"）
     * @return 対応するMaterial。存在しない場合はnull
     */
    @Nullable
    public static Material getById(String id) {
        return REGISTRY.get(id);
    }
}