package com.vomiter.sophisticatedairtank.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import com.vomiter.sophisticatedairtank.common.NetheriteDivingHelper;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
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
            boolean noFireResistantSource,
            LivingBreatheEvent event
    ) {
        return noFireResistantSource
                && !NetheriteDivingHelper.isWearingNetheriteBackpackWithAir(event.getEntity());
    }
}