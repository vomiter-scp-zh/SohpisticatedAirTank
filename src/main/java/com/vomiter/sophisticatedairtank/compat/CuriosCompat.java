package com.vomiter.sophisticatedairtank.compat;

import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;
import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import com.vomiter.sophisticatedairtank.common.NetheriteDivingHelper;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.Objects;

import static com.simibubi.create.content.equipment.armor.NetheriteDivingHandler.NETHERITE_DIVING_BITS_KEY;
import static com.simibubi.create.content.equipment.armor.NetheriteDivingHandler.setFireImmune;

public class CuriosCompat {
    public static boolean isWearingNetheriteBackpackWithAir(LivingEntity entity){
        var keys = CuriosApi.getCuriosInventory(entity).map(inv -> inv.getCurios().keySet()).orElse(null);
        SophisticatedAirTank.LOGGER.info("KEYS = {}", keys);
        return CuriosApi.getCuriosInventory(entity).map(
                inventory -> inventory.getStacksHandler("back")
                        .map(back -> {
                            SophisticatedAirTank.LOGGER.info("HAS BACK");
                            for (int i = 0; i < back.getStacks().getSlots(); i++) {
                                var stack = back.getStacks().getStackInSlot(i);
                                if (stack.getItem() instanceof BackpackItem && stack.getItem().isFireResistant()){
                                    SophisticatedAirTank.LOGGER.info("HAS BACKPACK");
                                    SophisticatedAirTank.LOGGER.info("AIR REMAIN = {}", PressurizedAirUtils.getAir(stack));
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
