package io.github.twister716.universalmaterials;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * このModのメインクラス
 */
@Mod(UniversalMaterials.MOD_ID)
public class UniversalMaterials {

    // ModIDを定数として定義しておくことで、他のクラスからも参照できる
    public static final String MOD_ID = "universalmaterials";

    public UniversalMaterials(IEventBus modEventBus) {
    }
}