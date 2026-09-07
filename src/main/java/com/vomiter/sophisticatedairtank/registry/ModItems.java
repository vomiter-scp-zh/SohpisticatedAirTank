package com.vomiter.sophisticatedairtank.registry;

import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import com.vomiter.sophisticatedairtank.common.upgrade.CreateJetPackUpgradeItem;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUpgradeItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SophisticatedAirTank.MOD_ID);

    public static final RegistryObject<PressurizedAirUpgradeItem> PRESSURIZED_AIR_UPGRADE = ITEMS.register(
            "pressurized_air_upgrade", PressurizedAirUpgradeItem::new);
    public static final RegistryObject<CreateJetPackUpgradeItem> JET_PACK_UPGRADE = ITEMS.register(
            "create_jetpack_upgrade", CreateJetPackUpgradeItem::new
    );

}
