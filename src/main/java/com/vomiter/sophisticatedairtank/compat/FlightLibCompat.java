package com.vomiter.sophisticatedairtank.compat;

import com.possible_triangle.flightlib.api.IJetpack;
import com.possible_triangle.flightlib.neoforge.api.NeoForgeFlightLib;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUtils;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class FlightLibCompat {
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                NeoForgeFlightLib.ITEM_CAPABILITY,
                (stack, context) -> {
                    if (!PressurizedAirUtils.hasJetUpgrade(stack)) {
                        return null;
                    }

                    return (IJetpack) stack.getItem();
                },
                net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.BACKPACK.get(),
                net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.COPPER_BACKPACK.get(),
                net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.IRON_BACKPACK.get(),
                net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.DIAMOND_BACKPACK.get(),
                net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.NETHERITE_BACKPACK.get()
        );
    }

}
