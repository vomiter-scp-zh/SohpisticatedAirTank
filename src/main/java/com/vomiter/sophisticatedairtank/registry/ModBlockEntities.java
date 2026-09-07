package com.vomiter.sophisticatedairtank.registry;

import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import com.vomiter.sophisticatedairtank.common.block.PressurizedBackpackBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITY_TYPES, SophisticatedAirTank.MOD_ID);

    public static final RegistryObject<BlockEntityType<PressurizedBackpackBlockEntity>> PRESSURIZED_BACKPACK = BLOCK_ENTITIES.register(
            "pressurized_backpack",
            () -> BlockEntityType.Builder.of(PressurizedBackpackBlockEntity::new, ModBlocks.PRESSURIZED_BACKPACK.get()).build(null));

}
