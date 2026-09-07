package com.vomiter.sophisticatedairtank.common.block;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.vomiter.sophisticatedairtank.registry.ModBlockEntities;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.everlasting.EverlastingUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;

@ParametersAreNullableByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("deprecation")
public class PressurizedBackpackBlock extends HorizontalKineticBlock
        implements IBE<PressurizedBackpackBlockEntity>, SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public PressurizedBackpackBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(0.8F)
                .sound(SoundType.WOOL)
                .pushReaction(PushReaction.DESTROY)
                .noOcclusion());
        registerDefaultState(defaultBlockState()
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
                .setValue(BackpackBlock.LEFT_TANK, false)
                .setValue(BackpackBlock.RIGHT_TANK, false)
                .setValue(BackpackBlock.BATTERY, false)
                .setValue(BackpackBlock.OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, BackpackBlock.LEFT_TANK, BackpackBlock.RIGHT_TANK, BackpackBlock.BATTERY, BackpackBlock.OPEN);
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        assert context != null;
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        assert state != null;
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos currentPos,
            BlockPos neighborPos) {
        assert state != null;
        if (state.getValue(WATERLOGGED)) {
            assert level != null;
            assert currentPos != null;
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        assert direction != null;
        assert neighborState != null;
        assert level != null;
        assert currentPos != null;
        assert neighborPos != null;
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.UP;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        assert level != null;
        assert pos != null;
        if (level.getBlockEntity(pos) instanceof PressurizedBackpackBlockEntity blockEntity) {
            BlockState delegateState = blockEntity.getDelegateState();
            if (delegateState != null) {
                assert context != null;
                return delegateState.getShape(level, pos, context);
            }
        }
        return Shapes.block();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        assert level != null;
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            return getBlockEntityOptional(level, pos)
                    .map(blockEntity -> blockEntity.restoreAndUse(player, hand, hit))
                    .orElse(InteractionResult.PASS);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide || !(entity instanceof ItemEntity itemEntity)) {
            return;
        }
        getBlockEntityOptional(level, pos).flatMap(PressurizedBackpackBlockEntity::getDelegateWrapper).ifPresent(wrapper -> {
            ItemStack remaining = InventoryHelper.runPickupOnPickupResponseUpgrades(
                    level,
                    wrapper.getUpgradeHandler(),
                    itemEntity.getItem().copy(),
                    false);
            if (remaining.getCount() < itemEntity.getItem().getCount()) {
                itemEntity.setItem(remaining);
            }
        });
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return getBlockEntityOptional(level, pos)
                .flatMap(PressurizedBackpackBlockEntity::getDelegateWrapper)
                .map(wrapper -> InventoryHelper.getAnalogOutputSignal(wrapper.getInventoryForInputOutput()))
                .orElse(0);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        boolean everlasting = getBlockEntityOptional(level, pos)
                .flatMap(PressurizedBackpackBlockEntity::getDelegateWrapper)
                .map(wrapper -> !wrapper.getUpgradeHandler().getTypeWrappers(EverlastingUpgradeItem.TYPE).isEmpty())
                .orElse(false);
        return everlasting ? 3_600_000.0F : super.getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        boolean everlasting = getBlockEntityOptional(level, pos)
                .flatMap(PressurizedBackpackBlockEntity::getDelegateWrapper)
                .map(wrapper -> !wrapper.getUpgradeHandler().getTypeWrappers(EverlastingUpgradeItem.TYPE).isEmpty())
                .orElse(false);
        return !everlasting && super.canEntityDestroy(state, level, pos, entity);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof PressurizedBackpackBlockEntity blockEntity) {
            ItemStack backpack = blockEntity.getDelegateBackpack();
            return backpack.isEmpty() ? List.of() : List.of(backpack.copy());
        }
        return List.of();
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return getBlockEntityOptional(level, pos)
                .map(PressurizedBackpackBlockEntity::getDelegateBackpack)
                .map(ItemStack::copy)
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public Class<PressurizedBackpackBlockEntity> getBlockEntityClass() {
        return PressurizedBackpackBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PressurizedBackpackBlockEntity> getBlockEntityType() {
        return ModBlockEntities.PRESSURIZED_BACKPACK.get();
    }

    public static BlockState copyVisualProperties(BlockState from, BlockState to) {
        if (from.hasProperty(BackpackBlock.FACING) && to.hasProperty(HORIZONTAL_FACING)) {
            to = to.setValue(HORIZONTAL_FACING, from.getValue(BackpackBlock.FACING));
        }
        if (from.hasProperty(BlockStateProperties.WATERLOGGED) && to.hasProperty(WATERLOGGED)) {
            to = to.setValue(WATERLOGGED, from.getValue(BlockStateProperties.WATERLOGGED));
        }
        if (from.hasProperty(BackpackBlock.LEFT_TANK) && to.hasProperty(BackpackBlock.LEFT_TANK)) {
            to = to.setValue(BackpackBlock.LEFT_TANK, from.getValue(BackpackBlock.LEFT_TANK));
        }
        if (from.hasProperty(BackpackBlock.RIGHT_TANK) && to.hasProperty(BackpackBlock.RIGHT_TANK)) {
            to = to.setValue(BackpackBlock.RIGHT_TANK, from.getValue(BackpackBlock.RIGHT_TANK));
        }
        if (from.hasProperty(BackpackBlock.BATTERY) && to.hasProperty(BackpackBlock.BATTERY)) {
            to = to.setValue(BackpackBlock.BATTERY, from.getValue(BackpackBlock.BATTERY));
        }
        if (from.hasProperty(BackpackBlock.OPEN) && to.hasProperty(BackpackBlock.OPEN)) {
            to = to.setValue(BackpackBlock.OPEN, from.getValue(BackpackBlock.OPEN));
        }
        return to;
    }
}
