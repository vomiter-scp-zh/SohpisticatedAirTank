package com.vomiter.sophisticatedairtank.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.vomiter.sophisticatedairtank.common.block.PressurizedBackpackBlockEntity;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import org.jetbrains.annotations.NotNull;

public class PressurizedBackpackRenderer implements BlockEntityRenderer<PressurizedBackpackBlockEntity> {

    public PressurizedBackpackRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static ThreadLocal<Boolean> renderCharging = ThreadLocal.withInitial(() -> false);

    @Override
    public void render(
            PressurizedBackpackBlockEntity blockEntity,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight,
            int packedOverlay) {
        ItemStack backpack = blockEntity.getDelegateBackpack();
        if (backpack.isEmpty()) {
            return;
        }

        Direction facing = blockEntity.getDelegateState().getValue(BackpackBlock.FACING);
        poseStack.pushPose();

        /*
        Minecraft.getInstance().getBlockRenderer().renderBatched(
                blockEntity.getDelegateState(),
                blockEntity.getBlockPos(),
                blockEntity.getLevel(),
                poseStack,
                buffer.getBuffer(RenderType.cutout()),
                true,
                blockEntity.getLevel().random
        );

         */

        poseStack.translate(0.5D, 0.5D, 0.5D);
        float rotation = -(facing.toYRot() + 180);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        try {
            renderCharging.set(true);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    backpack,
                    ItemDisplayContext.NONE,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    buffer,
                    blockEntity.getLevel(),
                    0);

        } finally {
            renderCharging.remove();
            poseStack.popPose();
        }

        SuperByteBuffer shaft = CachedBuffers.partial(
                AllPartialModels.COPPER_BACKTANK_SHAFT,
                blockEntity.getBlockState()
        );

        KineticBlockEntityRenderer.renderRotatingBuffer(
                blockEntity,
                shaft,
                poseStack,
                buffer.getBuffer(RenderType.solid()),
                packedLight
        );

        SuperByteBuffer cogs = CachedBuffers.partial(
                AllPartialModels.COPPER_BACKTANK_COGS,
                blockEntity.getBlockState()
        );

        poseStack.pushPose();
        double distance = 2 / 16D;

        poseStack.translate(facing.getStepX() * distance, -distance, facing.getStepZ() * distance);

        cogs.center()
                .rotateYDegrees(180 + AngleHelper.horizontalAngle(facing.getOpposite()))
                .uncenter()
                .translate(0, 6.5f / 16, 11f / 16)
                .rotate(AngleHelper.rad(blockEntity.getSpeed() / 4f * AnimationTickHolder.getRenderTime(blockEntity.getLevel()) % 360),
                        Direction.EAST)
                .translate(0, -6.5f / 16, -11f / 16);

        cogs.light(packedLight)
                .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));

        poseStack.popPose();

    }
}
