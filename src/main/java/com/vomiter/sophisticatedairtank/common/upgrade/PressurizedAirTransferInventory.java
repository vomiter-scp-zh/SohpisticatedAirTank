package com.vomiter.sophisticatedairtank.common.upgrade;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Two persistent slots belonging to the upgrade item, not to an open menu. */
public final class PressurizedAirTransferInventory extends ItemStackHandler {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final float TRANSFER_RATE = 4.0F;

    private static final String INVENTORY_TAG = "sophisticatedairtank:air_io_inventory";
    private static final String AIR_TAG = "Air";

    private final ItemStack upgrade;
    private final IStorageWrapper storageWrapper;
    private final Supplier<Float> getStoredAir;
    private final IntSupplier getCapacity;
    private final Consumer<Float> setStoredAir;
    private final Runnable saveUpgrade;

    public PressurizedAirTransferInventory(IStorageWrapper storageWrapper, ItemStack upgrade, Supplier<Float> getStoredAir,
            IntSupplier getCapacity, Consumer<Float> setStoredAir, Runnable saveUpgrade) {
        super(2);
        this.storageWrapper = storageWrapper;
        this.upgrade = upgrade;
        this.getStoredAir = getStoredAir;
        this.getCapacity = getCapacity;
        this.setStoredAir = setStoredAir;
        this.saveUpgrade = saveUpgrade;

        CompoundTag tag = upgrade.getTag();
        if (tag != null && tag.contains(INVENTORY_TAG, Tag.TAG_COMPOUND)) {
            // Retain exactly two slots, including when loading an unexpected Size value.
            CompoundTag inventoryNbt = tag.getCompound(INVENTORY_TAG).copy();
            inventoryNbt.putInt("Size", 2);
            deserializeNBT(inventoryNbt);
        }
    }

    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && AllTags.AllItemTags.PRESSURIZED_AIR_SOURCES.matches(stack);
    }

    public static float amount(float sourceAir, float targetAir, int targetCapacity, float rate) {
        if (!Float.isFinite(sourceAir) || !Float.isFinite(targetAir)
                || !Float.isFinite(rate) || sourceAir <= 0 || targetAir < 0
                || targetCapacity <= 0 || rate <= 0) {
            return 0;
        }
        float amount = Math.min(rate, Math.min(sourceAir, Math.max(0, targetCapacity - targetAir)));
        // Very large float values may not change when a small amount is added/subtracted.
        if (amount <= 0 || sourceAir - amount == sourceAir || targetAir + amount == targetAir) {
            return 0;
        }
        return amount;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return (slot == INPUT_SLOT || slot == OUTPUT_SLOT) && stack != upgrade
                && accepts(stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    protected void onContentsChanged(int slot) {
        persist();
    }

    private void persist() {
        upgrade.getOrCreateTag().put(INVENTORY_TAG, serializeNBT());
        saveUpgrade.run();
    }

    /** Call once per server upgrade tick, after refreshing the host capacity. */
    public void tick() {
        float air = getStoredAir.get();
        int capacity = getCapacity.getAsInt();
        if (!Float.isFinite(air) || air < 0 || capacity <= 0) {
            return;
        }

        boolean changed = false;
        ItemStack input = getStackInSlot(INPUT_SLOT);
        ItemStack output = getStackInSlot(OUTPUT_SLOT);
        if (input == upgrade || output == upgrade || (!input.isEmpty() && input == output)) {
            return;
        }

        // Input is processed first. Each direction has its own 4-unit limit.
        Optional<ItemAirAccess> inputAccess = getItemAirAccess(input);
        if (inputAccess.isPresent()) {
            ItemAirAccess source = inputAccess.get();
            float inputAir = source.getAir().get();
            float amount = amount(inputAir, air, capacity, TRANSFER_RATE);
            if (amount > 0) {
                source.setAir().accept(inputAir - amount);
                air += amount;
                changed = true;
            }
        }

        Optional<ItemAirAccess> outputAccess = getItemAirAccess(output);
        if (outputAccess.isPresent()) {
            ItemAirAccess target = outputAccess.get();
            float outputAir = target.getAir().get();
            float amount = amount(air, outputAir, target.capacity().getAsInt(), TRANSFER_RATE);
            if (amount > 0) {
                target.setAir().accept(outputAir + amount);
                air -= amount;
                changed = true;
            }
        }

        if (changed) {
            // This setter must update host state in memory without recreating this inventory.
            setStoredAir.accept(air);
            // Serialize both changed item stacks, even when input/output leave host air unchanged.
            persist();
        }
    }

    private Optional<ItemAirAccess> getItemAirAccess(ItemStack stack) {
        if (stack.getCount() != 1 || !accepts(stack)) {
            return Optional.empty();
        }
        if (stack.getItem() instanceof BackpackItem) {
            // A backpack's air belongs to its installed upgrade, not its root ItemStack NBT.
            return PressurizedAirUtils.find(stack).map(wrapper ->
                    new ItemAirAccess(wrapper::getAir, wrapper::getCapacity, wrapper::setAir));
        }
        if (stack.getItem() instanceof PressurizedAirUpgradeItem) {
            // An uninstalled upgrade has only its last stored capacity available.
            return Optional.of(new ItemAirAccess(
                    () -> PressurizedAirUpgradeWrapper.getAir(stack),
                    () -> PressurizedAirUpgradeWrapper.getStoredCapacity(stack),
                    air -> writeItemAir(stack, air)));
        }
        return Optional.of(new ItemAirAccess(
                () -> BacktankUtil.getAir(stack), () -> BacktankUtil.maxAir(stack),
                air -> writeItemAir(stack, air)));
    }

    private static void writeItemAir(ItemStack stack, float air) {
        // Create's backtank uses a root float "Air" tag.
        stack.getOrCreateTag().putFloat(AIR_TAG, air);
    }

    private record ItemAirAccess(Supplier<Float> getAir, IntSupplier capacity, Consumer<Float> setAir) {
    }
}
