package com.vomiter.sophisticatedairtank.client;

import com.vomiter.sophisticatedairtank.common.ISlotMutable;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirTransferInventory;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUpgradeContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeSettingsTab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import java.util.List;

public class PressurizedAirUpgradeTab
        extends UpgradeSettingsTab<PressurizedAirUpgradeContainer> {

    public PressurizedAirUpgradeTab(
            PressurizedAirUpgradeContainer container,
            Position position,
            StorageScreenBase<?> screen
    ) {
        super(
                container,
                position,
                screen,
                Component.translatable(
                        "item.sophisticatedairtank.pressurized_air_upgrade"
                ),
                Component.translatable(
                        "gui.sophisticatedairtank.pressurized_air"
                )
        );

        openTabDimension = new Dimension(48, 48);
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            Minecraft minecraft,
            int mouseX,
            int mouseY
    ) {
        super.renderBg(graphics, minecraft, mouseX, mouseY);

        if (!getContainer().isOpen()) {
            return;
        }

        GuiHelper.renderSlotsBackground(
                graphics,
                x + 3,
                y + 24,
                1,
                1
        );

        GuiHelper.renderSlotsBackground(
                graphics,
                x + 24,
                y + 24,
                1,
                1
        );
    }

    @Override
    protected void moveSlotsToTab() {
        List<Slot> slots = getContainer().getSlots();

        positionSlot(
                slots.get(PressurizedAirTransferInventory.INPUT_SLOT),
                4
        );

        positionSlot(
                slots.get(PressurizedAirTransferInventory.OUTPUT_SLOT),
                25
        );
    }

    private void positionSlot(Slot slot, int xOffset) {
        if (slot instanceof ISlotMutable mutable){
            mutable.sophisticatedairtank$setX(x - screen.getGuiLeft() + xOffset);
            mutable.sophisticatedairtank$setY(y - screen.getGuiTop() + 25);
        }
    }
}