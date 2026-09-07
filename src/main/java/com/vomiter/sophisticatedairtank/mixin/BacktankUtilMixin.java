package com.vomiter.sophisticatedairtank.mixin;

import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirIntegration;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUpgradeItem;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUpgradeWrapper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BacktankUtil.class, remap = false)
public abstract class BacktankUtilMixin {
    @Inject(
            method = "maxAir(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private static void sophisticatedairtank$maxAir(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof PressurizedAirUpgradeItem) {
            cir.setReturnValue(Math.max(1, PressurizedAirUpgradeWrapper.getStoredCapacity(stack)));
        }
    }

    @Inject(
            method = "consumeAir(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;F)V",
            at = @At("TAIL"),
            remap = false)
    private static void sophisticatedairtank$persistConsumedAir(
            LivingEntity entity,
            ItemStack stack,
            float amount,
            CallbackInfo ci) {
        if (stack.getItem() instanceof PressurizedAirUpgradeItem) {
            PressurizedAirIntegration.onExternalAirChanged(stack);
        }
    }
}
