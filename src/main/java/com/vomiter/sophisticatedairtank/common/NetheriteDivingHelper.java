package com.vomiter.sophisticatedairtank.common;

import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUtils;
import com.vomiter.sophisticatedairtank.compat.CuriosCompat;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;

public class NetheriteDivingHelper {
    public static boolean isWearingNetheriteBackpackWithAir(LivingEntity entity){
        return isWearingNetheriteBackpackAtChest(entity) || isWearingNethertieBackpackWithAirInCurios(entity);
    }

    public static boolean isWearingNethertieBackpackWithAirInCurios(LivingEntity entity){
        if (ModList.get().isLoaded("curios")){
            return CuriosCompat.isWearingNetheriteBackpackWithAir(entity);
        }
        return false;
    }

    public static boolean isWearingNetheriteBackpackAtChest(LivingEntity entity){
        var chestItem = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chestItem.getItem() instanceof BackpackItem){
            return chestItem.getItem().isFireResistant() && PressurizedAirUtils.getAir(chestItem) > 0;
        }
        return false;
    }
}
