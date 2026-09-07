package com.vomiter.sophisticatedairtank.network;

import net.minecraft.client.Minecraft;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;

public class ModNetworkClientHandler {
    static void handleSATSyncResponse(ModNetwork.SATSyncResponse response){
        assert Minecraft.getInstance().player != null;
        PlayerInventoryProvider.get().runOnBackpacks(
                Minecraft.getInstance().player,
                ((backpack, s, s1, i) -> {
                    IBackpackWrapper backpackWrapper = BackpackWrapper.fromStack(backpack);
                    backpackWrapper.onContentsNbtUpdated();
                    return true;
        }));
    }
}
