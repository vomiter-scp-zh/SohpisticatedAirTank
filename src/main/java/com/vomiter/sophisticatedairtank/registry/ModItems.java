package com.vomiter.sophisticatedairtank.registry;

import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import com.vomiter.sophisticatedairtank.common.upgrade.CreateJetPackUpgradeItem;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUpgradeItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, SophisticatedAirTank.MOD_ID);

    public static final DeferredHolder<Item, PressurizedAirUpgradeItem> PRESSURIZED_AIR_UPGRADE = ITEMS.register(
            "pressurized_air_upgrade", PressurizedAirUpgradeItem::new);
    public static final DeferredHolder<Item, CreateJetPackUpgradeItem> JET_PACK_UPGRADE = ITEMS.register(
            "create_jetpack_upgrade", CreateJetPackUpgradeItem::new
    );

}
