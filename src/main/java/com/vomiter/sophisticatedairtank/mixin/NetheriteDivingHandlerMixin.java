package com.vomiter.sophisticatedairtank.mixin;

import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;
import com.vomiter.sophisticatedairtank.common.NetheriteDivingHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetheriteDivingHandler.class, remap = false)
public class NetheriteDivingHandlerMixin {
    @Shadow
    @Final
    public static String NETHERITE_DIVING_BITS_KEY;

    @Shadow
    public static void setFireImmune(LivingEntity entity, boolean fireImmune) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "onLivingEquipmentChange", at = @At("TAIL"))
    private static void sophisticatedairtank$onEquip(LivingEquipmentChangeEvent event, CallbackInfo ci){
        LivingEntity entity = event.getEntity();
        EquipmentSlot slot = EquipmentSlot.CHEST;
        CompoundTag nbt = entity.getPersistentData();
        byte bits = nbt.getByte(NETHERITE_DIVING_BITS_KEY);
        if ((bits & 0b1111) == 0b1111) {
            return;
        }
        if ((bits & (1 << slot.getIndex())) != 0) return;
        if (NetheriteDivingHelper.isWearingNetheriteBackpackWithAir(entity)){
            bits |= (byte) (1 << slot.getIndex());
        }
        nbt.putByte(NETHERITE_DIVING_BITS_KEY, bits);
        if ((bits & 0b1111) == 0b1111) {
            setFireImmune(entity, true);
        }
    }
}
