package com.vomiter.sophisticatedairtank.common.upgrade;

import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;

public final class PressurizedAirUtils {

    public static Optional<PressurizedAirUpgradeWrapper> find(ItemStack backpack) {
        return backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance()).resolve().flatMap(PressurizedAirUtils::find);
    }

    public static Optional<PressurizedAirUpgradeWrapper> find(IStorageWrapper storageWrapper) {
        return storageWrapper.getUpgradeHandler().getTypeWrappers(PressurizedAirUpgradeItem.TYPE).stream().findFirst();
    }

    public static boolean hasAirUpgrade(BackpackBlockEntity backpackBlockEntity){
        return backpackBlockEntity.getBackpackWrapper()
                .getRenderInfo()
                .getUpgradeItems()
                .stream()
                .anyMatch(stack ->
                        stack.getItem() instanceof PressurizedAirUpgradeItem
                );
    }

    public static boolean hasAirUpgrade(ItemStack backpackItem){
        return new BackpackWrapper(backpackItem)
                .getRenderInfo()
                .getUpgradeItems()
                .stream()
                .anyMatch(stack ->
                        stack.getItem() instanceof PressurizedAirUpgradeItem
                );
    }

    public static boolean hasJetUpgrade(ItemStack backpackItem){
        return new BackpackWrapper(backpackItem)
                .getRenderInfo()
                .getUpgradeItems()
                .stream()
                .anyMatch(stack ->
                        stack.getItem() instanceof CreateJetPackUpgradeItem
                );
    }


    public static float getAir(ItemStack backpack) {
        return find(backpack).map(PressurizedAirUpgradeWrapper::getAir).orElse(0.0F);
    }

    public static int getCapacity(ItemStack backpack) {
        return find(backpack).map(PressurizedAirUpgradeWrapper::getCapacity).orElse(0);
    }

    public static float consumeAir(ItemStack backpack, float amount) {
        return find(backpack).map(wrapper -> wrapper.consumeAir(amount)).orElse(0.0F);
    }

    public static float addAir(ItemStack backpack, float amount) {
        return find(backpack).map(wrapper -> wrapper.addAir(amount)).orElse(0.0F);
    }
}
