package com.vomiter.sophisticatedairtank.common.upgrade;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.vomiter.sophisticatedairtank.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedcore.inventory.StatefulComponentItemHandler;

import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class PressurizedAirTransferInventory extends StatefulComponentItemHandler {

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int TRANSFER_RATE = 4;

    private final ItemStack upgrade;
    private final IntSupplier getStoredAir;
    private final IntSupplier getCapacity;
    private final IntConsumer setStoredAir;
    private final Runnable saveUpgrade;

    public PressurizedAirTransferInventory(
            ItemStack upgrade,
            IntSupplier getStoredAir,
            IntSupplier getCapacity,
            IntConsumer setStoredAir,
            Runnable saveUpgrade
    ) {
        super(upgrade, ModDataComponents.AIR_IO_INVENTORY, 2);

        this.upgrade = upgrade;
        this.getStoredAir = getStoredAir;
        this.getCapacity = getCapacity;
        this.setStoredAir = setStoredAir;
        this.saveUpgrade = saveUpgrade;
    }

    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty()
                && AllTags.AllItemTags.PRESSURIZED_AIR_SOURCES.matches(stack);
    }

    public static int amount(
            int sourceAir,
            int targetAir,
            int targetCapacity,
            int rate
    ) {
        if (sourceAir <= 0
                || targetAir < 0
                || targetCapacity <= 0
                || rate <= 0) {
            return 0;
        }

        return Math.min(
                rate,
                Math.min(
                        sourceAir,
                        Math.max(0, targetCapacity - targetAir)
                )
        );
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        return (slot == INPUT_SLOT || slot == OUTPUT_SLOT)
                && stack != upgrade
                && accepts(stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    protected void onContentsChanged(
            int slot,
            ItemStack oldStack,
            ItemStack newStack
    ) {
        super.onContentsChanged(slot, oldStack, newStack);
        saveUpgrade.run();
    }

    /**
     * 每個 server-side upgrade tick 呼叫一次。
     * 呼叫前 wrapper 應先更新容量。
     */
    public void tick() {
        int air = getStoredAir.getAsInt();
        int capacity = getCapacity.getAsInt();

        if (air < 0 || capacity <= 0) {
            return;
        }

        ItemStack input = getStackInSlot(INPUT_SLOT);
        ItemStack output = getStackInSlot(OUTPUT_SLOT);

        if (input == upgrade
                || output == upgrade
                || (!input.isEmpty() && input == output)) {
            return;
        }

        boolean inputChanged = false;
        boolean outputChanged = false;

        Optional<ItemAirAccess> inputAccess = getItemAirAccess(input);
        if (inputAccess.isPresent()) {
            ItemAirAccess source = inputAccess.get();
            int inputAir = source.getAir().getAsInt();

            int transferred = amount(
                    inputAir,
                    air,
                    capacity,
                    TRANSFER_RATE
            );

            if (transferred > 0) {
                source.setAir().accept(inputAir - transferred);
                air += transferred;
                inputChanged = true;
            }
        }

        Optional<ItemAirAccess> outputAccess = getItemAirAccess(output);
        if (outputAccess.isPresent()) {
            ItemAirAccess target = outputAccess.get();
            int outputAir = target.getAir().getAsInt();

            int transferred = amount(
                    air,
                    outputAir,
                    target.capacity().getAsInt(),
                    TRANSFER_RATE
            );

            if (transferred > 0) {
                target.setAir().accept(outputAir + transferred);
                air -= transferred;
                outputChanged = true;
            }
        }

        if (!inputChanged && !outputChanged) {
            return;
        }

        setStoredAir.accept(air);
        onSlotChanged(inputChanged ? INPUT_SLOT : OUTPUT_SLOT);
    }

    private Optional<ItemAirAccess> getItemAirAccess(ItemStack stack) {
        if (stack.isEmpty()
                || stack.getCount() != 1
                || !accepts(stack)) {
            return Optional.empty();
        }

        if (stack.getItem() instanceof BackpackItem) {
            /*
             * Sophisticated Backpack 的 air 存在其 air upgrade，
             * 而不是 backpack ItemStack 本身。
             */
            return PressurizedAirUtils.find(stack)
                    .map(wrapper -> new ItemAirAccess(
                            wrapper::getAir,
                            wrapper::getCapacity,
                            wrapper::setAir
                    ));
        }

        if (stack.getItem() instanceof PressurizedAirUpgradeItem) {
            /*
             * 未安裝的 upgrade 沒有 storage wrapper，
             * 因此使用最後保存的 capacity。
             */
            return Optional.of(new ItemAirAccess(
                    () -> PressurizedAirUpgradeWrapper.getAir(stack),
                    () -> PressurizedAirUpgradeWrapper
                            .getStoredCapacity(stack),
                    air -> writeUpgradeAir(stack, air)
            ));
        }

        /*
         * Create 1.21.1 使用 AllDataComponents.BACKTANK_AIR，
         * 不再使用 root float "Air"。
         */
        return Optional.of(new ItemAirAccess(
                () -> BacktankUtil.getAir(stack),
                () -> BacktankUtil.maxAir(stack),
                air -> stack.set(
                        AllDataComponents.BACKTANK_AIR,
                        Math.max(0, Math.min(air, BacktankUtil.maxAir(stack)))
                )
        ));
    }

    private static void writeUpgradeAir(ItemStack stack, int air) {
        int capacity =
                PressurizedAirUpgradeWrapper.getStoredCapacity(stack);

        stack.set(
                AllDataComponents.BACKTANK_AIR,
                Math.max(0, Math.min(air, capacity))
        );
    }

    private record ItemAirAccess(
            IntSupplier getAir,
            IntSupplier capacity,
            IntConsumer setAir
    ) {
    }
}