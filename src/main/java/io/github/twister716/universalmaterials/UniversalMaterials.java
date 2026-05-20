package io.github.twister716.universalmaterials;

import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.client.MaterialColorHandler;
import io.github.twister716.universalmaterials.content.item.UMItems;
import io.github.twister716.universalmaterials.content.tab.UMCreativeTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
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

        net.neoforged.fml.ModList.get().getModFiles().forEach(modFileInfo ->
                UMMaterialRegistry.initAll(modFileInfo.getFile().getScanResult())
        );

        UMItems.registerAll();

        UMMaterialRegistry.lockRegistry();

        // カラーハンドラーはクライアントサイドのみ登録する
        // @EventBusSubscriberではなくaddListener()で直接登録することで
        // イベントバスの問題を回避する
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(RegisterColorHandlersEvent.Item.class,
                    MaterialColorHandler::registerItemColors);
            modEventBus.addListener(RegisterColorHandlersEvent.Block.class,
                    MaterialColorHandler::registerBlockColors);
        }
    }
}