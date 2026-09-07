package com.vomiter.sophisticatedairtank.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.vomiter.sophisticatedairtank.client.PressurizedBackpackRenderer;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUtils;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.RenderTypeHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(
            method = "render(Lnet/minecraft/world/item/ItemStack;"
                    + "Lnet/minecraft/world/item/ItemDisplayContext;"
                    + "ZLcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;"
                    + "IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void sophisticatedairtank$renderCogs(
            ItemStack itemStack,
            ItemDisplayContext context,
            boolean leftHand,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            BakedModel model,
            CallbackInfo ci) {

        if (!(itemStack.getItem() instanceof BackpackItem)) {
            return;
        }

        if (!PressurizedAirUtils.hasAirUpgrade(itemStack)) {
            return;
        }

        if (Boolean.TRUE.equals(
                PressurizedBackpackRenderer.renderCharging.get())) {
            return;
        }

        BlockState state = ModBlocks.BACKPACK.get().defaultBlockState();
        Direction facing = state.getValue(BackpackBlock.FACING);

        SuperByteBuffer cogs = CachedBuffers.partial(
                AllPartialModels.COPPER_BACKTANK_COGS,
                state
        );

        Minecraft minecraft = Minecraft.getInstance();
        float renderTime = minecraft.level == null
                ? 0
                : AnimationTickHolder.getRenderTime(minecraft.level);

        float angle = AngleHelper.rad(
                (8 / 4f * renderTime) % 360
        );

        poseStack.pushPose();
        try {
            poseStack.translate(0, -0.1, -0.1);

            cogs.center()
                    .rotateYDegrees(
                            180 + AngleHelper.horizontalAngle(
                                    facing.getOpposite()
                            )
                    )
                    .uncenter()
                    .translate(0, 6.5f / 16, 11f / 16)
                    .rotate(angle, Direction.EAST)
                    .translate(0, -6.5f / 16, -11f / 16);

            RenderType itemRenderType =
                    RenderTypeHelper.getEntityRenderType(
                            RenderType.solid(),
                            false
                    );

            cogs.light(packedLight)
                    .renderInto(
                            poseStack,
                            bufferSource.getBuffer(itemRenderType)
                    );
        } finally {
            poseStack.popPose();
        }
    }
}