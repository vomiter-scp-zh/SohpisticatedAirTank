package com.vomiter.sophisticatedairtank.registry;

import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import com.vomiter.sophisticatedairtank.common.block.PressurizedBackpackBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, SophisticatedAirTank.MOD_ID);

    public static final DeferredHolder<Block, PressurizedBackpackBlock> PRESSURIZED_BACKPACK = BLOCKS.register(
            "pressurized_backpack", PressurizedBackpackBlock::new);
}
