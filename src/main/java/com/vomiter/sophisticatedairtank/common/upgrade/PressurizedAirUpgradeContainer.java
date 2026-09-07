package com.vomiter.sophisticatedairtank.common.upgrade;

import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SlotSuppliedHandler;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;

import java.util.List;
import java.util.function.Supplier;

public class PressurizedAirUpgradeContainer extends UpgradeContainerBase<
        PressurizedAirUpgradeWrapper, PressurizedAirUpgradeContainer> {

    public static final ResourceLocation EMPTY_TANK_INPUT_SLOT_BACKGROUND = SophisticatedAirTank.modLoc("item/empty_tank_input_slot");
    public static final ResourceLocation EMPTY_TANK_OUTPUT_SLOT_BACKGROUND = SophisticatedAirTank.modLoc("item/empty_tank_output_slot");

    public static final UpgradeContainerType<PressurizedAirUpgradeWrapper, PressurizedAirUpgradeContainer> TYPE =
            new UpgradeContainerType<>(PressurizedAirUpgradeContainer::new);

    public PressurizedAirUpgradeContainer(Player player, int containerId,
            PressurizedAirUpgradeWrapper wrapper,
            UpgradeContainerType<PressurizedAirUpgradeWrapper, PressurizedAirUpgradeContainer> type) {
        super(player, containerId, wrapper, type);
        slots.add(new AirIOSlot(() -> this.upgradeWrapper.getAirIoInventory(),
                PressurizedAirTransferInventory.INPUT_SLOT).setBackground(InventoryMenu.BLOCK_ATLAS, EMPTY_TANK_INPUT_SLOT_BACKGROUND));
        slots.add(new AirIOSlot(() -> this.upgradeWrapper.getAirIoInventory(),
                PressurizedAirTransferInventory.OUTPUT_SLOT).setBackground(InventoryMenu.BLOCK_ATLAS, EMPTY_TANK_OUTPUT_SLOT_BACKGROUND));
    }

    public float getAir() {
        return PressurizedAirUpgradeWrapper.getAir(getUpgradeStack());
    }

    public int getCapacity() {
        return PressurizedAirUpgradeWrapper.getStoredCapacity(getUpgradeStack());
    }

    @Override
    public void handleMessage(CompoundTag data) {
        // Ordinary menu slot synchronization handles item movement.
    }

    public static final class AirIOSlot extends SlotSuppliedHandler {
        private final int ioSlot;

        private AirIOSlot(Supplier<IItemHandler> inventory, int ioSlot) {
            super(inventory, ioSlot, -100, -100);
            this.ioSlot = ioSlot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return PressurizedAirTransferInventory.accepts(stack) && super.mayPlace(stack);
        }

        public List<Component> getAirTooltip() {
            if (ioSlot == PressurizedAirTransferInventory.INPUT_SLOT) {
                return List.of(
                        Component.translatable("gui.sophisticatedairtank.pressurized_air.input")
                                .withStyle(ChatFormatting.GOLD));
            }
            return List.of(
                    Component.translatable("gui.sophisticatedairtank.pressurized_air.output")
                            .withStyle(ChatFormatting.GOLD));
        }
    }
}
