package com.vomiter.sophisticatedairtank.common.upgrade;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;

import java.lang.ref.WeakReference;
import java.util.*;

public final class PressurizedAirIntegration {
    private static final Map<ItemStack, WeakReference<PressurizedAirUpgradeWrapper>> TRACKED_WRAPPERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static List<ItemStack> findAirSources(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return List.of();
        }

        List<ItemStack> sources = new ArrayList<>();


        PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, handlerName, identifier, slot) -> {
            if (PlayerInventoryProvider.MAIN_INVENTORY.equals(handlerName)
                    || PlayerInventoryProvider.OFFHAND_INVENTORY.equals(handlerName)) {
                return false;
            }
            IBackpackWrapper wrapper = BackpackWrapper.fromStack(backpack);
            wrapper.getUpgradeHandler().getTypeWrappers(PressurizedAirUpgradeItem.TYPE).forEach(upgradeWrapper -> {
                track(upgradeWrapper);
                sources.add(upgradeWrapper.getUpgradeStack());
            });
            return false;
        });
        return sources;
    }

    public static void track(PressurizedAirUpgradeWrapper wrapper) {
        TRACKED_WRAPPERS.put(wrapper.getUpgradeStack(), new WeakReference<>(wrapper));
    }

    public static void onExternalAirChanged(ItemStack stack) {
        WeakReference<PressurizedAirUpgradeWrapper> reference = TRACKED_WRAPPERS.get(stack);
        PressurizedAirUpgradeWrapper wrapper = reference == null ? null : reference.get();
        if (wrapper != null) {
            wrapper.synchronizeExternalChange();
        } else {
            TRACKED_WRAPPERS.remove(stack);
        }
    }
}
