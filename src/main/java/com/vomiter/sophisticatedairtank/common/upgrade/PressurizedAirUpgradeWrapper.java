package com.vomiter.sophisticatedairtank.common.upgrade;

import com.simibubi.create.AllDataComponents;
import com.vomiter.sophisticatedairtank.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IStackableContentsUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class PressurizedAirUpgradeWrapper
        extends UpgradeWrapperBase<
        PressurizedAirUpgradeWrapper,
        PressurizedAirUpgradeItem
        >
        implements ITickableUpgrade, IStackableContentsUpgrade {

    @Nullable
    private PressurizedAirTransferInventory airIoInventory;

    public PressurizedAirUpgradeWrapper(
            IStorageWrapper storageWrapper,
            ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler
    ) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    public PressurizedAirTransferInventory getAirIoInventory() {
        if (airIoInventory == null) {
            airIoInventory = new PressurizedAirTransferInventory(
                    upgrade,
                    this::getAir,
                    this::getCapacity,
                    this::writeAir,
                    this::save
            );
        }

        return airIoInventory;
    }

    public static int getAir(ItemStack stack) {
        if (stack.has(AllDataComponents.BACKTANK_AIR)) {
            return stack.getOrDefault(AllDataComponents.BACKTANK_AIR, 0);
        }
        // 舊版本資料遷移前的 fallback
        return stack.getOrDefault(ModDataComponents.AIR, 0);
    }

    public static int getStoredCapacity(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.CAPACITY, 0);
    }

    public int getAir() {
        return Math.min(getAir(upgrade), getCapacity());
    }

    public int getCapacity() {
        return Math.max(0, upgradeItem.getMaxAir(storageWrapper));
    }

    public boolean isFull() {
        return getAir() >= getCapacity();
    }

    public int addAir(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int previous = getAir();
        setAir(previous + amount);
        return getAir() - previous;
    }

    public int consumeAir(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int previous = getAir();
        setAir(previous - amount);
        return previous - getAir();
    }

    private void writeAir(int air) {
        int capacity = getCapacity();

        upgrade.set(
                AllDataComponents.BACKTANK_AIR,
                Math.max(0, Math.min(air, capacity))
        );
        upgrade.set(ModDataComponents.CAPACITY, capacity);

        // 完成舊資料遷移後，避免存在兩份 air
        upgrade.remove(ModDataComponents.AIR);
    }

    public void setAir(int air) {
        writeAir(air);
        save();
    }

    /**
     * 外部直接更動 air 後，用來重新限制數值並同步 storage。
     */
    public void synchronizeExternalChange() {
        writeAir(getAir(upgrade));
        save();
    }

    private void synchronizeCapacity(boolean saveChanges) {
        int capacity = getCapacity();
        int previousCapacity = getStoredCapacity(upgrade);
        int previousAir = getAir(upgrade);
        int clampedAir = Math.max(0, Math.min(previousAir, capacity));

        boolean needsMigration =
                !upgrade.has(AllDataComponents.BACKTANK_AIR)
                        || upgrade.has(ModDataComponents.AIR);

        boolean changed =
                needsMigration
                        || previousCapacity != capacity
                        || previousAir != clampedAir;

        if (!changed) {
            return;
        }

        upgrade.set(ModDataComponents.CAPACITY, capacity);
        upgrade.set(AllDataComponents.BACKTANK_AIR, clampedAir);
        upgrade.remove(ModDataComponents.AIR);

        if (saveChanges) {
            save();
        }
    }

    @Override
    public void tick(
            @Nullable Entity entity,
            Level level,
            BlockPos pos
    ) {
        if (level.isClientSide) {
            return;
        }

        synchronizeCapacity(true);
        getAirIoInventory().tick();
    }

    @Override
    public int getMinimumMultiplierRequired() {
        int base = Math.max(
                1,
                upgradeItem.getMaxAirBase(storageWrapper)
        );

        return (int) Math.ceil((double) getAir() / base);
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