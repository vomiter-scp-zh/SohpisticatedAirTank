package com.vomiter.sophisticatedairtank.common.upgrade;

import com.simibubi.create.content.equipment.armor.BacktankUtil;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.Config;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeSlotChangeResult;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.battery.BatteryUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeItem;

public class PressurizedAirUpgradeItem extends UpgradeItemBase<PressurizedAirUpgradeWrapper> {
    public static final UpgradeType<PressurizedAirUpgradeWrapper> TYPE = new UpgradeType<>(PressurizedAirUpgradeWrapper::new);
    public static final int BAR_COLOR = 0xEFEFEF;

    private static final List<IUpgradeItem.UpgradeConflictDefinition> CONFLICTS = List.of(
            new IUpgradeItem.UpgradeConflictDefinition(
                    PressurizedAirUpgradeItem.class::isInstance,
                    0,
                    Component.translatable("gui.sophisticatedairtank.error.pressurized_air_exists")),
            new IUpgradeItem.UpgradeConflictDefinition(
                    BatteryUpgradeItem.class::isInstance,
                    0,
                    Component.translatable("gui.sophisticatedairtank.error.battery_conflict")));

    public PressurizedAirUpgradeItem() {
        super(Config.SERVER.maxUpgradesPerStorage);
    }

    @Override
    public UpgradeType<PressurizedAirUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public int getInventoryColumnsTaken() {
        return 2;
    }

    @Override
    public List<IUpgradeItem.UpgradeConflictDefinition> getUpgradeConflicts() {
        return CONFLICTS;
    }

    @Override
    public UpgradeSlotChangeResult canSwapUpgradeFor(
            ItemStack upgradeStackToPut,
            int upgradeSlot,
            IStorageWrapper storageWrapper,
            boolean isClientSide) {
        if (upgradeStackToPut.getItem() == this) {
            return checkExtraInsertConditions(upgradeStackToPut, storageWrapper, isClientSide, upgradeSlot, this);
        }
        return super.canSwapUpgradeFor(upgradeStackToPut, upgradeSlot, storageWrapper, isClientSide);
    }

    @Override
    public UpgradeSlotChangeResult checkExtraInsertConditions(
            ItemStack upgradeStack,
            IStorageWrapper storageWrapper,
            boolean isClientSide,
            int upgradeSlot,
            @Nullable IUpgradeItem<?> upgradeInSlot) {
        double resultingMultiplier = storageWrapper.getInventoryHandler().getStackSizeMultiplier();
        if (upgradeInSlot instanceof StackUpgradeItem stackUpgrade) {
            resultingMultiplier /= stackUpgrade.getStackSizeMultiplier();
        }

        int capacityAfterInsertion = multiplySafely(getMaxAirBase(storageWrapper), resultingMultiplier);
        double multiplierRequired = PressurizedAirUpgradeWrapper.getAir(upgradeStack) / Math.max(1.0D, capacityAfterInsertion);
        if (multiplierRequired > 1.0D) {
            DecimalFormat format = new DecimalFormat("0.#");
            String required = format.format(Math.ceil(multiplierRequired * 10.0D) / 10.0D);
            return UpgradeSlotChangeResult.fail(
                    Component.translatable("gui.sophisticatedairtank.error.air_too_high", required),
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptySet());
        }
        return UpgradeSlotChangeResult.success();
    }

    public int getMaxAir(IStorageWrapper storageWrapper) {
        return multiplySafely(getMaxAirBase(storageWrapper), storageWrapper.getInventoryHandler().getStackSizeMultiplier());
    }

    public int getMaxAirBase(IStorageWrapper storageWrapper) {
        int configuredCopperRows = Math.max(1, (Config.SERVER.copperBackpack.inventorySlotCount.get() + 8) / 9);
        double rowRatio = (double) storageWrapper.getNumberOfSlotRows() / configuredCopperRows;
        return multiplySafely(BacktankUtil.maxAirWithoutEnchants(), rowRatio);
    }

    private static int multiplySafely(int base, double multiplier) {
        double result = base * multiplier;
        return result >= Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(1, (int) Math.round(result));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return PressurizedAirUpgradeWrapper.getAir(stack) < PressurizedAirUpgradeWrapper.getStoredCapacity(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int capacity = PressurizedAirUpgradeWrapper.getStoredCapacity(stack);
        return capacity <= 0 ? 0 : Math.round(13.0F * PressurizedAirUpgradeWrapper.getAir(stack) / capacity);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }
}
