package io.github.twister716.universalmaterials;

import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.ore.OreTypes;
import io.github.twister716.universalmaterials.api.ore.StoneGroups;
import io.github.twister716.universalmaterials.client.MaterialColorHandler;
import io.github.twister716.universalmaterials.client.texture.TextureGenerator;
import io.github.twister716.universalmaterials.content.item.UMItems;
import io.github.twister716.universalmaterials.content.ore.OreDefinitions;
import io.github.twister716.universalmaterials.content.tab.UMCreativeTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(UniversalMaterials.MOD_ID)
public class UniversalMaterials {

    public static final String MOD_ID = "universalmaterials";

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MOD_ID);

    public UniversalMaterials(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);

        UMCreativeTabs.register(modEventBus);

        // 素材クラスをスキャンして登録する
        net.neoforged.fml.ModList.get().getModFiles().forEach(modFileInfo ->
                UMMaterialRegistry.initAll(modFileInfo.getFile().getScanResult())
        );

        // 鉱石生成の初期化
        // 順序が重要: StoneGroups → OreTypes → OreDefinitions
        StoneGroups.init();
        OreTypes.init();
        OreDefinitions.init();

        // フラグに基づいてアイテム・ブロックを登録する
        // OreDefinitions.init()より後に呼ぶこと（鉱石ブロック登録のため）
        UMItems.registerAll();

        UMMaterialRegistry.lockRegistry();

        // クライアントサイドのみの処理
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(RegisterColorHandlersEvent.Item.class,
                    MaterialColorHandler::registerItemColors);
            modEventBus.addListener(RegisterColorHandlersEvent.Block.class,
                    MaterialColorHandler::registerBlockColors);

            // VirtualPackResources にリソースマネージャーを渡すリロードリスナーを登録する。
            // このリスナーが prepare() で ResourceManager をセットし、
            // テクスチャアトラス構築前に TextureGenerator.generateAll() が呼ばれる。
            modEventBus.addListener(RegisterClientReloadListenersEvent.class, event ->
                    event.registerReloadListener(new ResourceManagerReloadListener() {
                        @Override
                        public void onResourceManagerReload(ResourceManager manager) {
                            // リソースリロード（起動時・F3+T）のたびに ResourceManager をセットする。
                            // generateAll() はlistResources()から遅延呼び出しされる。
                            TextureGenerator.setResourceManager(manager);
                        }
                    })
            );
        }
    }
}