package com.vomiter.sophisticatedairtank.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUtils;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BackpackBlockEntityRenderer.class, remap = false)
public class BackpackBlockEntityRendererMixin {
    @Inject(method = "render(Lnet/p3pp3rf1y/sophisticatedbackpacks/backpack/BackpackBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("TAIL"))
    private void sophisticatedairtank$render(BackpackBlockEntity backpackBlockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, CallbackInfo ci){
        ItemStack backpack = backpackBlockEntity.getBackpackWrapper().getBackpack();
        if (backpack.isEmpty()) {
            return;
        }
        if (!PressurizedAirUtils.hasAirUpgrade(backpackBlockEntity)) {
            return;
        }

        Direction facing = backpackBlockEntity.getBlockState().getValue(BackpackBlock.FACING);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        float rotation = -(facing.toYRot() + 180);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.popPose();

        SuperByteBuffer cogs = CachedBuffers.partial(
                AllPartialModels.COPPER_BACKTANK_COGS,
                backpackBlockEntity.getBlockState()
        );

        poseStack.pushPose();
        double distance = 2 / 16D;

        poseStack.translate(facing.getStepX() * distance, -distance, facing.getStepZ() * distance);

        cogs.center()
                .rotateYDegrees(180 + AngleHelper.horizontalAngle(facing.getOpposite()))
                .uncenter()
                .translate(0, 6.5f / 16, 11f / 16)
                .rotate(AngleHelper.rad(8 / 4f * AnimationTickHolder.getRenderTime(backpackBlockEntity.getLevel()) % 360),
                        Direction.EAST)
                .translate(0, -6.5f / 16, -11f / 16);

        cogs.light(packedLight)
                .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));

        poseStack.popPose();
    }
}
