package com.vomiter.sophisticatedairtank.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import com.vomiter.sophisticatedairtank.common.NetheriteDivingHelper;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = DivingHelmetItem.class, remap = false)
public abstract class DivingHelmetItemMixin {
    @ModifyExpressionValue(
            method = "breatheUnderwater",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;noneMatch(Ljava/util/function/Predicate;)Z"
            )
    )
    private static boolean sophisticatedairtank$acceptNetheriteBackpack(
            boolean original,
            @Local(argsOnly = true) LivingBreatheEvent event
            ) {
        return original
                && !NetheriteDivingHelper.isWearingNetheriteBackpackWithAir(event.getEntity());
    }
}