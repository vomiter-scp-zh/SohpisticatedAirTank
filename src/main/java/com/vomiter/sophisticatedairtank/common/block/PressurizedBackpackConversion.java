package com.vomiter.sophisticatedairtank.common.block;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import com.vomiter.sophisticatedairtank.common.BackpackForgeEvent;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUtils;
import com.vomiter.sophisticatedairtank.registry.ModBlocks;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUpgradeWrapper;

import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;

public final class PressurizedBackpackConversion {
    private static final ThreadLocal<Set<BlockPos>> SUPPRESSED_POSITIONS = ThreadLocal.withInitial(HashSet::new);

    public static void onPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        long current = event.getLevel().getLevelData().getGameTime();
        BackpackForgeEvent.delayedTasks.add(new BackpackForgeEvent.DelayedRunnable(
                current + 10,
                level,
                () -> {
                    tryConvert(level, event.getPos());
                    tryConvert(level, event.getPos().below());
                }
        ));
    }

    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        long current = event.getLevel().getLevelData().getGameTime();
        BackpackForgeEvent.delayedTasks.add(new BackpackForgeEvent.DelayedRunnable(
                current + 10,
                level,
                () -> {
                    tryConvert(level, event.getPos());
                    tryConvert(level, event.getPos().below());
                    tryConvert(level, event.getPos().above());
                }
        ));

    }

    public static void runSuppressed(BlockPos pos, Runnable action) {
        Set<BlockPos> positions = SUPPRESSED_POSITIONS.get();
        BlockPos immutablePos = pos.immutable();
        positions.add(immutablePos);
        try {
            action.run();
        } finally {
            positions.remove(immutablePos);
            if (positions.isEmpty()) {
                SUPPRESSED_POSITIONS.remove();
            }
        }
    }

    static void tryConvert(ServerLevel level, BlockPos pos) {
        if (SUPPRESSED_POSITIONS.get().contains(pos)) {
            return;
        }

        BlockState backpackState = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if ((backpackState.getBlock() instanceof BackpackBlock)
        && (blockEntity instanceof BackpackBlockEntity backpackBlockEntity)){
            if (backpackState.getValue(BackpackBlock.OPEN)) return;

            PressurizedAirUpgradeWrapper air = PressurizedAirUtils.find(backpackBlockEntity.getBackpackWrapper()).orElse(null);
            if (air == null || air.isFull() || !hasVerticalShaftAbove(level, pos)) {
                return;
            }

            ItemStack backpack = backpackBlockEntity.getBackpackWrapper().getBackpack().copy();
            BlockState kineticState = PressurizedBackpackBlock.copyVisualProperties(
                    backpackState,
                    ModBlocks.PRESSURIZED_BACKPACK.get().defaultBlockState());
            level.setBlock(pos, kineticState, 3);
            if (level.getBlockEntity(pos) instanceof PressurizedBackpackBlockEntity pressurizedBlockEntity) {
                pressurizedBlockEntity.setDelegate(backpack, backpackState);
            }
        } else if (blockEntity instanceof PressurizedBackpackBlockEntity backpackBlockEntity) {
            PressurizedAirUpgradeWrapper air = backpackBlockEntity.getDelegateWrapper().flatMap(PressurizedAirUtils::find).orElse(null);

            if (air == null || air.isFull() || !hasVerticalShaftAbove(level, pos)) {
                backpackBlockEntity.restoreDelegate();
            }
        }
    }

    private static boolean hasVerticalShaftAbove(ServerLevel level, BlockPos backpackPos) {
        BlockPos shaftPos = backpackPos.above();
        BlockState shaftState = level.getBlockState(shaftPos);
        if (!(shaftState.getBlock() instanceof IRotate rotate)) {
            return false;
        }
        return rotate.getRotationAxis(shaftState) == Direction.Axis.Y
                && rotate.hasShaftTowards(level, shaftPos, shaftState, Direction.DOWN);
    }
}
