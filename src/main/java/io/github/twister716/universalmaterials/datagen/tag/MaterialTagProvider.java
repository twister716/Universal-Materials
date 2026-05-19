package io.github.twister716.universalmaterials.datagen.tag;

import io.github.twister716.universalmaterials.UniversalMaterials;
import io.github.twister716.universalmaterials.api.material.Material;
import io.github.twister716.universalmaterials.api.material.UMMaterialRegistry;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlag;
import io.github.twister716.universalmaterials.api.material.flag.MaterialFlags;
import io.github.twister716.universalmaterials.api.material.tagprefix.TagPrefix;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

/**
 * アイテムタグJSON（data/c/tags/item/）を自動生成するProvider。
 *
 * TagPrefixが持つ itemTagFormat・parentTag の情報をもとに
 * 「個別タグ（c:ingots/tin）」と「親タグ（c:ingots）」の2段階のタグを生成する。
 * これによりJEI・REI・他Modとのタグ互換が取れる。
 */
public class MaterialTagProvider extends ItemTagsProvider {

    public MaterialTagProvider(PackOutput output,
                               CompletableFuture<HolderLookup.Provider> lookupProvider) {
        // 第3引数はBlockTagProviderへの依存（今回はアイテムのみなのでcompletedFutureを渡す）
        super(output, lookupProvider,
                CompletableFuture.completedFuture(tagName -> null),
                UniversalMaterials.MOD_ID, null);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (Material material : UMMaterialRegistry.getRegistrationOrder()) {
            String materialName = material.getId().split(":")[1];

            for (MaterialFlag flag : material.getPartFlags()) {
                flag.getTagPrefix().ifPresent(prefix ->
                        registerItemTags(material, materialName, prefix));
            }

            if (!material.hasPropertyFlag(MaterialFlags.IS_VANILLA)) {
                for (MaterialFlag flag : material.getImpliedFlags()) {
                    flag.getTagPrefix().ifPresent(prefix ->
                            registerItemTags(material, materialName, prefix));
                }
            }
        }
    }

    /**
     * 1つのTagPrefixに対してアイテムタグを登録する。
     *
     * 例（INGOTの場合）:
     *   c:ingots/tin → universalmaterials:tin_ingot を追加
     *   c:ingots     → #c:ingots/tin を追加（タグ参照）
     */
    private void registerItemTags(Material material, String materialName, TagPrefix prefix) {
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(
                UniversalMaterials.MOD_ID, prefix.formatId(materialName));

        if (!prefix.getItemTagFormat().isEmpty()) {
            // 個別タグ（例: c:ingots/tin）にアイテムを追加する
            TagKey<Item> individualTag = TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.parse(prefix.formatItemTag(materialName)));
            tag(individualTag).addOptional(itemId);

            // 親タグ（例: c:ingots）に個別タグを参照として追加する
            if (!prefix.getParentTag().isEmpty()) {
                TagKey<Item> parentTag = TagKey.create(
                        Registries.ITEM,
                        ResourceLocation.parse(prefix.getParentTag()));
                tag(parentTag).addOptionalTag(individualTag);
            }
        }
    }

    @Override
    public String getName() {
        return "Universal Materials Item Tag Provider";
    }
}
