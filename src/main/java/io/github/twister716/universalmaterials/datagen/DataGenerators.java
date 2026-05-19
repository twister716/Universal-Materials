package io.github.twister716.universalmaterials.datagen;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.datagen.lang.EnUsLanguageProvider;
import io.github.twister716.universalmaterials.datagen.loot.MaterialLootTableProvider;
import io.github.twister716.universalmaterials.datagen.model.MaterialModelProvider;
import io.github.twister716.universalmaterials.datagen.tag.MaterialBlockTagProvider;
import io.github.twister716.universalmaterials.datagen.tag.MaterialTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Datagenで使うProviderを登録するクラス。
 * ./gradlew runData を実行するとこのクラスが呼ばれ、
 * src/generated/resources/ にJSONファイルが自動生成される。
 */
@EventBusSubscriber(modid = UniversalMaterials.MOD_ID)
public class DataGenerators {

    @SubscribeEvent
    public static void addProviders(GatherDataEvent event) {
        PackOutput output = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // ===== クライアント側データ（モデルJSON・言語ファイル）=====
        event.getGenerator().addProvider(event.includeClient(),
                new EnUsLanguageProvider(output));
        event.getGenerator().addProvider(event.includeClient(),
                new MaterialModelProvider(output));

        // ===== サーバー側データ（タグ・ルートテーブル）=====
        event.getGenerator().addProvider(event.includeServer(),
                new MaterialTagProvider(output, lookupProvider));
        event.getGenerator().addProvider(event.includeServer(),
                new MaterialBlockTagProvider(output, lookupProvider));
        event.getGenerator().addProvider(event.includeServer(),
                new MaterialLootTableProvider(output, lookupProvider));
    }
}
