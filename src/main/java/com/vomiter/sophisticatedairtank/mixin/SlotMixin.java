package com.vomiter.sophisticatedairtank.mixin;

import com.vomiter.sophisticatedairtank.common.ISlotMutable;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Slot.class)
public class SlotMixin implements ISlotMutable {

    @Mutable
    @Shadow
    public int x;

    @Mutable
    @Shadow
    public int y;

    @Override
    public void sophisticatedairtank$setX(int x) {
        this.x = x;
    }

    @Override
    public void sophisticatedairtank$setY(int y) {
        this.y = y;
    }
}
