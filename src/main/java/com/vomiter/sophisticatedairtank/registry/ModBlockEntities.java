package com.vomiter.sophisticatedairtank.registry;

import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import com.vomiter.sophisticatedairtank.common.block.PressurizedBackpackBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, SophisticatedAirTank.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PressurizedBackpackBlockEntity>> PRESSURIZED_BACKPACK = BLOCK_ENTITIES.register(
            "pressurized_backpack",
            () -> BlockEntityType.Builder.of(PressurizedBackpackBlockEntity::new, ModBlocks.PRESSURIZED_BACKPACK.get()).build(null));

}
