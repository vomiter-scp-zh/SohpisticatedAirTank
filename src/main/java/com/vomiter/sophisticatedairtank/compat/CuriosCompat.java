package com.vomiter.sophisticatedairtank.compat;

import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;
import com.vomiter.sophisticatedairtank.common.NetheriteDivingHelper;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.Objects;

public class CuriosCompat {
    public static boolean isWearingNetheriteBackpackWithAir(LivingEntity entity){
        var keys = CuriosApi.getCuriosInventory(entity).map(inv -> inv.getCurios().keySet()).orElse(null);
        return CuriosApi.getCuriosInventory(entity).map(
                inventory -> inventory.getStacksHandler("back")
                        .map(back -> {
                            for (int i = 0; i < back.getStacks().getSlots(); i++) {
                                var stack = back.getStacks().getStackInSlot(i);
                                if (stack.getItem() instanceof BackpackItem && stack.getItem().isFireResistant()){
                                    if (PressurizedAirUtils.getAir(stack) > 0) return true;
                                }
                            }
                            return false;
                        })
                        .orElse(false)
        ).orElse(false);
    }

    public static void onCurioChangeEvent(CurioChangeEvent event){
        if (!Objects.equals(event.getIdentifier(), "back")) return;
        LivingEntity entity = event.getEntity();
        EquipmentSlot slot = EquipmentSlot.CHEST;
        CompoundTag nbt = entity.getPersistentData();
        if (NetheriteDivingHelper.isWearingNetheriteBackpackWithAir(entity)){
            NetheriteDivingHandler.setBit(entity, slot);
        } else {
            NetheriteDivingHandler.clearBit(entity, slot);
        }
    }
}
