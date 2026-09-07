package com.vomiter.sophisticatedairtank.common.upgrade;

import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.p3pp3rf1y.sophisticatedcore.client.gui.INameableEmptySlot;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SlotSuppliedHandler;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;

import java.util.function.Supplier;

public class PressurizedAirUpgradeContainer
        extends UpgradeContainerBase<
        PressurizedAirUpgradeWrapper,
        PressurizedAirUpgradeContainer
        > {

    public static final ResourceLocation EMPTY_TANK_INPUT_SLOT_BACKGROUND =
            SophisticatedAirTank.modLoc(
                    "item/empty_tank_input_slot"
            );

    public static final ResourceLocation EMPTY_TANK_OUTPUT_SLOT_BACKGROUND =
            SophisticatedAirTank.modLoc(
                    "item/empty_tank_output_slot"
            );

    public static final UpgradeContainerType<
            PressurizedAirUpgradeWrapper,
            PressurizedAirUpgradeContainer
            > TYPE = new UpgradeContainerType<>(
            PressurizedAirUpgradeContainer::new
    );

    public PressurizedAirUpgradeContainer(
            Player player,
            int containerId,
            PressurizedAirUpgradeWrapper wrapper,
            UpgradeContainerType<
                    PressurizedAirUpgradeWrapper,
                    PressurizedAirUpgradeContainer
                    > type
    ) {
        super(player, containerId, wrapper, type);

        slots.add(
                new AirIOSlot(
                        supplyFromWrapper(
                                PressurizedAirUpgradeWrapper::getAirIoInventory
                        ),
                        PressurizedAirTransferInventory.INPUT_SLOT,
                        Component.translatable(
                                "gui.sophisticatedairtank.pressurized_air.input"
                        ).withStyle(ChatFormatting.GOLD)
                ).setBackground(
                        InventoryMenu.BLOCK_ATLAS,
                        EMPTY_TANK_INPUT_SLOT_BACKGROUND
                )
        );

        slots.add(
                new AirIOSlot(
                        supplyFromWrapper(
                                PressurizedAirUpgradeWrapper::getAirIoInventory
                        ),
                        PressurizedAirTransferInventory.OUTPUT_SLOT,
                        Component.translatable(
                                "gui.sophisticatedairtank.pressurized_air.output"
                        ).withStyle(ChatFormatting.GOLD)
                ).setBackground(
                        InventoryMenu.BLOCK_ATLAS,
                        EMPTY_TANK_OUTPUT_SLOT_BACKGROUND
                )
        );
    }

    @Override
    public void handlePacket(CompoundTag data) {
        // 普通 menu slot synchronization 負責物品移動。
    }

    public int getAir() {
        return upgradeWrapper.getAir();
    }

    public int getCapacity() {
        return upgradeWrapper.getCapacity();
    }

    public static final class AirIOSlot
            extends SlotSuppliedHandler
            implements INameableEmptySlot {

        private final Component emptyTooltip;

        private AirIOSlot(
                Supplier<IItemHandler> inventory,
                int slot,
                Component emptyTooltip
        ) {
            super(inventory, slot, -100, -100);
            this.emptyTooltip = emptyTooltip;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return PressurizedAirTransferInventory.accepts(stack)
                    && super.mayPlace(stack);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }

        @Override
        public boolean hasEmptyTooltip() {
            return true;
        }

        @Override
        public Component getEmptyTooltip() {
            return emptyTooltip;
        }
    }
}