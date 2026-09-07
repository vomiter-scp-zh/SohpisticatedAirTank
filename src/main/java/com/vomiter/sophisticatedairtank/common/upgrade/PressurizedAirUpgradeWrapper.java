package com.vomiter.sophisticatedairtank.common.upgrade;

import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IStackableContentsUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;

public class PressurizedAirUpgradeWrapper
        extends UpgradeWrapperBase<PressurizedAirUpgradeWrapper, PressurizedAirUpgradeItem>
        implements ITickableUpgrade, IStackableContentsUpgrade {
    public static final String AIR_TAG = "Air";
    public static final String CAPACITY_TAG = "Capacity";

    @Nullable
    private PressurizedAirTransferInventory airIoInventory;

    public PressurizedAirUpgradeWrapper(IStorageWrapper storageWrapper,
            ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    public PressurizedAirTransferInventory getAirIoInventory() {
        if (airIoInventory == null) {
            airIoInventory = new PressurizedAirTransferInventory(
                    storageWrapper, upgrade, this::getAir, this::getCapacity,
                    this::writeAir, this::save);
        }
        return airIoInventory;
    }

    public static float getAir(ItemStack stack) {
        return stack.getOrCreateTag().getFloat(AIR_TAG);
    }

    public static int getStoredCapacity(ItemStack stack) {
        return stack.getOrCreateTag().getInt(CAPACITY_TAG);
    }

    public float getAir() {
        return Math.min(getAir(upgrade), getCapacity());
    }

    public int getCapacity() {
        return upgradeItem.getMaxAir(storageWrapper);
    }

    public boolean isFull() {
        return getAir() >= getCapacity();
    }

    public float addAir(float amount) {
        if (amount <= 0.0F) {
            return 0.0F;
        }
        float previous = getAir();
        setAir(previous + amount);
        return getAir() - previous;
    }

    public float consumeAir(float amount) {
        if (amount <= 0.0F) {
            return 0.0F;
        }
        float previous = getAir();
        setAir(previous - amount);
        return previous - getAir();
    }

    /** Update NBT without saving; I/O saves this together with both slot items. */
    private void writeAir(float air) {
        int capacity = getCapacity();
        CompoundTag tag = upgrade.getOrCreateTag();
        tag.putFloat(AIR_TAG, Math.max(0.0F, Math.min(air, capacity)));
        tag.putInt(CAPACITY_TAG, capacity);
    }

    public void setAir(float air) {
        writeAir(air);
        save();
    }

    public void synchronizeExternalChange() {
        writeAir(getAir(upgrade));
        save();
    }

    private void synchronizeCapacity(boolean saveChanges) {
        CompoundTag tag = upgrade.getOrCreateTag();
        int capacity = getCapacity();
        float air = Math.max(0.0F, Math.min(tag.getFloat(AIR_TAG), capacity));
        boolean changed = tag.getInt(CAPACITY_TAG) != capacity
                || Float.compare(tag.getFloat(AIR_TAG), air) != 0;
        tag.putInt(CAPACITY_TAG, capacity);
        tag.putFloat(AIR_TAG, air);
        if (changed && saveChanges) {
            save();
        }
    }

    @Override
    public void tick(@Nullable Entity entity, Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        synchronizeCapacity(true);
        getAirIoInventory().tick();
    }

    @Override
    public int getMinimumMultiplierRequired() {
        int base = Math.max(1, upgradeItem.getMaxAirBase(storageWrapper));
        return (int) Math.ceil(getAir() / base);
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public void onAdded() {
        synchronizeCapacity(true);
    }

}
