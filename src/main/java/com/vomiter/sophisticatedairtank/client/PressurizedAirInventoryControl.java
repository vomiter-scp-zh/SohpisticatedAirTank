package com.vomiter.sophisticatedairtank.client;

import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUpgradeContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeInventoryControlBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class PressurizedAirInventoryControl
        extends UpgradeInventoryControlBase {

    private static final int TANK_WIDTH = 18;
    private static final int INNER_WIDTH = 16;
    private static final int SEGMENT_HEIGHT = 18;

    private static final int AIR_TOP_COLOR = 0xFFDDF6FF;
    private static final int AIR_BOTTOM_COLOR = 0xFF639CB8;

    private static final TextureBlitData OVERLAY = new TextureBlitData(
            GuiHelper.GUI_CONTROLS,
            Dimension.SQUARE_256,
            new UV(47, 30),
            new Dimension(INNER_WIDTH, SEGMENT_HEIGHT)
    );

    private final PressurizedAirUpgradeContainer container;
    private final Position pos;
    private final int height;
    private final StorageScreenBase<?> screen;

    public PressurizedAirInventoryControl(
            int upgradeSlot,
            PressurizedAirUpgradeContainer container,
            Position pos,
            int height,
            StorageScreenBase<?> screen) {
        this.container = container;
        this.pos = pos;
        this.height = Math.max(0, height);
        this.screen = screen;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        if (height <= 2) {
            return;
        }

        renderBackground(graphics);
        renderAir(graphics);
        renderGlassOverlay(graphics);
    }

    private void renderBackground(GuiGraphics graphics) {
        int left = getTankLeft();
        int top = pos.y();
        int endHeight = Math.min(SEGMENT_HEIGHT, height / 2);

        GuiHelper.blit(
                graphics,
                left,
                top,
                GuiHelper.BAR_BACKGROUND_TOP,
                TANK_WIDTH,
                endHeight
        );

        int middleEnd = height - endHeight;

        for (int offset = endHeight;
             offset < middleEnd;
             offset += SEGMENT_HEIGHT) {

            int segmentHeight = Math.min(
                    SEGMENT_HEIGHT,
                    middleEnd - offset
            );

            GuiHelper.blit(
                    graphics,
                    left,
                    top + offset,
                    GuiHelper.BAR_BACKGROUND_MIDDLE,
                    TANK_WIDTH,
                    segmentHeight
            );
        }

        GuiHelper.blit(
                graphics,
                left,
                top + height - endHeight,
                GuiHelper.BAR_BACKGROUND_BOTTOM,
                TANK_WIDTH,
                endHeight
        );
    }

    private void renderAir(GuiGraphics graphics) {
        int capacity = getCapacity();
        if (capacity == 0) {
            return;
        }

        float air = getDisplayedAir(capacity);
        float ratio = air / capacity;

        int innerHeight = height - 2;
        int filledHeight = Mth.clamp(
                Mth.ceil(innerHeight * ratio),
                0,
                innerHeight
        );

        if (filledHeight == 0) {
            return;
        }

        int left = getTankLeft() + 1;
        int bottom = pos.y() + height - 1;

        graphics.fillGradient(
                left,
                bottom - filledHeight,
                left + INNER_WIDTH,
                bottom,
                AIR_TOP_COLOR,
                AIR_BOTTOM_COLOR
        );
    }

    private void renderGlassOverlay(GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 100.0F);

        for (int offset = 0; offset < height; offset += SEGMENT_HEIGHT) {
            int segmentHeight = Math.min(
                    SEGMENT_HEIGHT,
                    height - offset
            );

            GuiHelper.blit(
                    graphics,
                    getTankLeft() + 1,
                    pos.y() + offset,
                    OVERLAY,
                    INNER_WIDTH,
                    segmentHeight
            );
        }

        graphics.pose().popPose();
    }

    @Override
    public void renderTooltip(
            @NotNull StorageScreenBase<?> screen,
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY) {
        if (height <= 2) {
            return;
        }

        // render 使用 GUI 相對座標；滑鼠判斷使用螢幕絕對座標。
        int left = screen.getGuiLeft() + getTankLeft() + 1;
        int top = screen.getGuiTop() + pos.y() + 1;
        int innerHeight = height - 2;

        if (mouseX < left || mouseX >= left + INNER_WIDTH
                || mouseY < top || mouseY >= top + innerHeight) {
            return;
        }

        int capacity = getCapacity();
        float air = getDisplayedAir(capacity);

        List<Component> tooltip = List.of(
                Component.translatable(
                        "gui.sophisticatedairtank.pressurized_air"
                ).withStyle(ChatFormatting.AQUA),
                Component.translatable(
                        "gui.sophisticatedairtank.pressurized_air.contents_tooltip",
                        air,
                        capacity
                )
        );

        graphics.renderTooltip(
                Minecraft.getInstance().font,
                tooltip,
                Optional.empty(),
                mouseX,
                mouseY
        );
    }

    @Override
    public void renderErrorOverlay(GuiGraphics graphics) {
        if (height <= 2) {
            return;
        }

        screen.renderOverlay(
                graphics,
                StorageScreenBase.ERROR_SLOT_COLOR,
                getTankLeft() + 1,
                pos.y() + 1,
                INNER_WIDTH,
                height - 2
        );
    }

    private int getTankLeft() {
        // 每個 control 分配 36 pixels，18-pixel 氣槽置中。
        return pos.x() + 9;
    }

    private int getCapacity() {
        return Math.max(0, container.getCapacity());
    }

    private float getDisplayedAir(int capacity) {
        return Mth.clamp(container.getAir(), 0.0F, (float) capacity);
    }
}