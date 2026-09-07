package com.vomiter.sophisticatedairtank.mixin;

import com.possible_triangle.create_jetpack.config.Configs;
import com.possible_triangle.flightlib.api.*;
import com.possible_triangle.flightlib.api.sources.CuriosSource;
import com.possible_triangle.flightlib.api.sources.EquipmentSource;
import com.possible_triangle.flightlib.forge.api.ForgeFlightLib;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import com.simibubi.create.foundation.particle.AirParticleData;


import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Mixin(BackpackItem.class)
public class BackpackItem_JetpackMixin implements IJetpack, ICapabilityProvider {
    @Unique
    private final LazyOptional<IJetpack> sophisticatedairtank$jetpackCapability =
            LazyOptional.of(() -> this);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeFlightLib.JETPACK_CAPABILITY) {
            return sophisticatedairtank$jetpackCapability.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public @NotNull ControlType activeType(@NotNull IJetpack.Context context) {
        return ControlType.TOGGLE;
    }

    @Override
    public double horizontalSpeed(@NotNull IJetpack.Context context) {
        return Configs.INSTANCE.getSERVER().getHorizontalSpeed();
    }

    @Override
    public double verticalSpeed(@NotNull IJetpack.Context context) {
        return Configs.INSTANCE.getSERVER().getVerticalSpeed();
    }

    @Override
    public double acceleration(@NotNull IJetpack.Context context) {
        return Configs.INSTANCE.getSERVER().getAcceleration();
    }

    @Override
    public @NotNull ControlType hoverType(@NotNull IJetpack.Context context) {
        return ControlType.TOGGLE;
    }

    @Override
    public double hoverSpeed(@NotNull IJetpack.Context context) {
        return Configs.INSTANCE.getSERVER().getHoverSpeed();
    }

    @Override
    public double hoverVerticalSpeed(@NotNull IJetpack.Context context) {
        return verticalSpeed(context) * 0.8;
    }

    @Override
    public double hoverHorizontalSpeed(@NotNull IJetpack.Context context) {
        return horizontalSpeed(context) * 0.8;
    }

    @Override
    public double swimModifier(@NotNull IJetpack.Context context) {
        return Configs.INSTANCE.getSERVER().getSwimModifier();
    }

    @Override
    public boolean boostsElytra() {
        return elytraBoost() > 0;
    }

    @Override
    public double elytraBoost() {
        return Configs.INSTANCE.getSERVER().getElytraBoost();
    }

    @Override
    public boolean isValid(@NotNull IJetpack.Context context) {
        var source = context.getSource();
        if (source instanceof EquipmentSource equipmentSource){
            if (equipmentSource.getSlot().equals(EquipmentSlot.CHEST)){
                if (equipmentSource.getStack().getItem() instanceof BackpackItem){
                    return PressurizedAirUtils.hasJetUpgrade(equipmentSource.getStack());
                }
            }
            return equipmentSource.getSlot().equals(EquipmentSlot.CHEST);
        }
        if (source instanceof CuriosSource curiosSource){
            if (curiosSource.getStack().getItem() instanceof BackpackItem){
                return PressurizedAirUtils.hasJetUpgrade(curiosSource.getStack());
            }
        }
        return false;
    }

    @Override
    public boolean isUsable(@NotNull IJetpack.Context context) {
        var tank = BacktankUtil.getAllWithAir(context.getEntity())
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(ItemStack.EMPTY);
        if(tank.isEmpty()) return false;
        var air = BacktankUtil.getAir(tank);
        var cost = BacktankUtil.maxAirWithoutEnchants() / sophisticatedjetpack$secondsPerTank(context);
        return air >= cost;
    }

    @Override
    public void onUse(@NotNull IJetpack.Context context) {
        if (context.getWorld().getGameTime() % 20 != 0L) return;
        BacktankUtil.canAbsorbDamage(context.getEntity(), sophisticatedjetpack$secondsPerTank(context));
    }

    @Unique
    private int sophisticatedjetpack$secondsPerTank(Context context) {
        return (isHovering(context))?
                Configs.INSTANCE.getSERVER().getSecondsPerTankHover()
                : Configs.INSTANCE.getSERVER().getSecondsPerTank();
    }

    @Override
    public @Nullable List<Vec3> getThrusters(@NotNull IJetpack.Context context) {
        return Stream.of(-0.35, 0.35)
                .map(offset -> new Vec3(offset, 0.7, -0.5))
                .toList();
    }

    @Override
    public boolean isHovering(@NotNull IJetpack.Context context) {
        return IFlightApi.Companion.getINSTANCE().isActive(hoverType(context), FlightKey.TOGGLE_HOVER, context.getEntity());
    }

    @Override
    public boolean isThrusting(@NotNull IJetpack.Context context) {
        var entity = context.getEntity();
        if (entity.getVehicle() != null) return false;
        if (!IFlightApi.Companion.getINSTANCE().isActive(
                context.getJetpack().activeType(context),
                FlightKey.TOGGLE_ACTIVE,
                entity
        )
        ) return false;
        if (context.getPose() == FlyingPose.SUPERMAN && entity.getDeltaMovement().length() > 0.1) return true;
        if (isHovering(context) && !entity.onGround()) return true;
        return FlightKey.UP.isPressed(entity);
    }

    @Override
    public @NotNull ParticleOptions createParticles() {
        if (Configs.INSTANCE.getCLIENT().getSpawnSnowParticles())
            return ParticleTypes.SNOWFLAKE;
        else
            return new AirParticleData(0F, 0.01F);
    }
}
