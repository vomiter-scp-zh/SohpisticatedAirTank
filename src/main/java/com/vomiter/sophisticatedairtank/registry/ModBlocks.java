package com.vomiter.sophisticatedairtank.registry;

import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import com.vomiter.sophisticatedairtank.common.block.PressurizedBackpackBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SophisticatedAirTank.MOD_ID);

    public static final RegistryObject<PressurizedBackpackBlock> PRESSURIZED_BACKPACK = BLOCKS.register(
            "pressurized_backpack", PressurizedBackpackBlock::new);
}
