package io.github.twister716.universalmaterials;

import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.content.item.UMItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(UniversalMaterials.MOD_ID)
public class UniversalMaterials {

    public static final String MOD_ID = "universalmaterials";

    // アイテム登録用DeferredRegister
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);

    // ブロック登録用DeferredRegister
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MOD_ID);

    public UniversalMaterials(IEventBus modEventBus, ModContainer modContainer) {
        // DeferredRegisterをイベントバスに登録する
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);

        // @AutoMaterialRegistryが付いた全クラスをスキャンして素材を登録する
        net.neoforged.fml.ModList.get().getModFiles().forEach(modFileInfo ->
                UMMaterialRegistry.initAll(modFileInfo.getFile().getScanResult())
        );

        // 素材登録完了後にアイテム・ブロックを自動生成する
        UMItems.registerAll();

        // 全素材の登録が完了したのでレジストリをロックする
        UMMaterialRegistry.lockRegistry();
    }
}