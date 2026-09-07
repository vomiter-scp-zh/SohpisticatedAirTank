package com.vomiter.sophisticatedairtank.common.block;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.particle.AirParticleData;
import com.vomiter.sophisticatedairtank.common.BackpackForgeEvent;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUtils;
import com.vomiter.sophisticatedairtank.registry.ModBlockEntities;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUpgradeWrapper;
import java.util.Optional;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModBlocks;

public class PressurizedBackpackBlockEntity extends KineticBlockEntity {
    private static final String DELEGATE_BACKPACK_TAG = "DelegateBackpack";
    private static final String DELEGATE_STATE_TAG = "DelegateState";
    private static final String DELEGATE_BLOCK_TAG = "Block";
    private static final String FACING_TAG = "Facing";
    private static final String WATERLOGGED_TAG = "Waterlogged";
    private static final String LEFT_TANK_TAG = "LeftTank";
    private static final String RIGHT_TANK_TAG = "RightTank";
    private static final String BATTERY_TAG = "Battery";
    private static final String OPEN_TAG = "Open";
    private static final String TIMER_TAG = "AirTimer";

    private ItemStack delegateBackpack = ItemStack.EMPTY;
    private BlockState delegateState = ModBlocks.BACKPACK.get().defaultBlockState();
    private int airTimer;
    private BackpackWrapper delegateWrapper;

    public PressurizedBackpackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRESSURIZED_BACKPACK.get(), pos, state);
    }

    public void setDelegate(ItemStack backpack, BlockState originalState) {
        delegateBackpack = backpack.copy();
        delegateState = originalState;
        delegateWrapper = new BackpackWrapper(delegateBackpack);
        setChanged();
        sendData();
    }

    public ItemStack getDelegateBackpack() {
        return delegateBackpack;
    }

    public BlockState getDelegateState() {
        return delegateState;
    }

    public Optional<IBackpackWrapper> getDelegateWrapper() {
        if (delegateWrapper == null) return Optional.empty();
        return Optional.of(delegateWrapper);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel){
            long current = serverLevel.getGameTime();
            BackpackForgeEvent.delayedTasks.add(new BackpackForgeEvent.DelayedRunnable(
                    current + 100,
                    level,
                    () -> {
                        PressurizedBackpackConversion.tryConvert(serverLevel, getBlockPos());
                    }
            ));

        }

    }

    @Override
    public void tick() {
        super.tick();

        PressurizedAirUpgradeWrapper air = getDelegateWrapper().flatMap(PressurizedAirUtils::find).orElse(null);
        assert level != null;
        if (air == null) {
            if (!level.isClientSide()) {
                restoreDelegate();
            }
            return;
        }
        if (air.isFull()) {
            if (!level.isClientSide()) {
                playFilledEffect();
                restoreDelegate();
            }
            return;
        }
        if (getSpeed() == 0.0F || getBlockState().getValue(PressurizedBackpackBlock.WATERLOGGED)) {
            return;
        }
        if (airTimer > 0) {
            airTimer--;
            return;
        }

        if (level.isClientSide()) {
            if (!air.isFull()) {
                spawnChargingParticle();
            }
            return;
        }
        float absoluteSpeed = Math.abs(getSpeed());
        int increment = Mth.clamp(((int) absoluteSpeed - 100) / 20, 1, 5);
        air.addAir(increment);
        setChanged();
        sendData();
        airTimer = Mth.clamp((int) (128.0F - absoluteSpeed / 5.0F) - 108, 0, 20);
        if (air.isFull()) {
            playFilledEffect();
            restoreDelegate();
        }
    }

    private void spawnChargingParticle() {
        Vec3 center = VecHelper.getCenterOf(worldPosition);
        Vec3 position = VecHelper.offsetRandomly(center, level.random, 0.65F);
        Vec3 motion = center.subtract(position);
        level.addParticle(new AirParticleData(1.0F, 0.05F), position.x, position.y, position.z, motion.x, motion.y, motion.z);
    }

    private void playFilledEffect() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        AllSoundEvents.CONFIRM.playAt(level, worldPosition, 0.4F, 1.0F, true);
        Vec3 baseMotion = new Vec3(0.25D, 0.1D, 0.0D);
        Vec3 center = VecHelper.getCenterOf(worldPosition);
        for (int angle = 0; angle < 360; angle += 10) {
            Vec3 motion = VecHelper.rotate(baseMotion, angle, Direction.Axis.Y);
            Vec3 position = center.add(motion.normalize().scale(0.25D));
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    position.x,
                    position.y,
                    position.z,
                    0,
                    motion.x,
                    motion.y,
                    motion.z,
                    1.0D);
        }
    }

    public InteractionResult restoreAndUse(Player player, InteractionHand hand, BlockHitResult hit) {
        Level currentLevel = level;
        if (currentLevel == null || level.isClientSide() || !restoreDelegate()) {
            return InteractionResult.PASS;
        }
        BlockPos pos = getBlockPos();
        BackpackForgeEvent.delayedTasks.add(
                new BackpackForgeEvent.DelayedRunnable(
                        level.getGameTime() + 2,
                        level,
                        () -> level.getBlockState(pos).use(currentLevel, player, hand, hit)
                )
        );
        return InteractionResult.SUCCESS;
    }

    boolean restoreDelegate() {
        if (level == null || delegateBackpack.isEmpty() || delegateState == null) {
            return false;
        }
        ItemStack backpack = delegateBackpack.copy();
        BlockState originalState = delegateState.setValue(BackpackBlock.OPEN, false);
        PressurizedBackpackConversion.runSuppressed(worldPosition, () -> {
            level.setBlock(worldPosition, originalState, 3);
            BlockEntity restoredBlockEntity = level.getBlockEntity(worldPosition);
            if (restoredBlockEntity instanceof BackpackBlockEntity backpackBlockEntity) {
                backpackBlockEntity.setBackpack(backpack);
                backpackBlockEntity.refreshRenderState();
                backpackBlockEntity.tryToAddToController();
            }
        });
        return level.getBlockState(worldPosition).getBlock() instanceof BackpackBlock;
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if (!delegateBackpack.isEmpty()) {
            tag.put(DELEGATE_BACKPACK_TAG, delegateBackpack.save(new CompoundTag()));
        }
        tag.put(DELEGATE_STATE_TAG, writeDelegateState(delegateState));
        tag.putInt(TIMER_TAG, airTimer);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        delegateBackpack = tag.contains(DELEGATE_BACKPACK_TAG)
                ? ItemStack.of(tag.getCompound(DELEGATE_BACKPACK_TAG))
                : ItemStack.EMPTY;
        delegateState = readDelegateState(tag.getCompound(DELEGATE_STATE_TAG));
        airTimer = tag.getInt(TIMER_TAG);
    }

    private static CompoundTag writeDelegateState(BlockState state) {
        CompoundTag tag = new CompoundTag();
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (id != null) {
            tag.putString(DELEGATE_BLOCK_TAG, id.toString());
        }
        if (state.hasProperty(BackpackBlock.FACING)) {
            tag.putString(FACING_TAG, state.getValue(BackpackBlock.FACING).getSerializedName());
        }
        putBooleanProperty(tag, WATERLOGGED_TAG, state, BlockStateProperties.WATERLOGGED);
        putBooleanProperty(tag, LEFT_TANK_TAG, state, BackpackBlock.LEFT_TANK);
        putBooleanProperty(tag, RIGHT_TANK_TAG, state, BackpackBlock.RIGHT_TANK);
        putBooleanProperty(tag, BATTERY_TAG, state, BackpackBlock.BATTERY);
        putBooleanProperty(tag, OPEN_TAG, state, BackpackBlock.OPEN);
        return tag;
    }

    private static void putBooleanProperty(
            CompoundTag tag,
            String key,
            BlockState state,
            BooleanProperty property) {
        if (state.hasProperty(property)) {
            tag.putBoolean(key, state.getValue(property));
        }
    }

    private static BlockState readDelegateState(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString(DELEGATE_BLOCK_TAG));
        Block block = id == null ? null : ForgeRegistries.BLOCKS.getValue(id);
        BlockState state = block instanceof BackpackBlock
                ? block.defaultBlockState()
                : ModBlocks.BACKPACK.get().defaultBlockState();

        Direction facing = Direction.byName(tag.getString(FACING_TAG));
        if (facing != null && facing.getAxis().isHorizontal() && state.hasProperty(BackpackBlock.FACING)) {
            state = state.setValue(BackpackBlock.FACING, facing);
        }
        state = readBooleanProperty(tag, WATERLOGGED_TAG, state, BlockStateProperties.WATERLOGGED);
        state = readBooleanProperty(tag, LEFT_TANK_TAG, state, BackpackBlock.LEFT_TANK);
        state = readBooleanProperty(tag, RIGHT_TANK_TAG, state, BackpackBlock.RIGHT_TANK);
        state = readBooleanProperty(tag, BATTERY_TAG, state, BackpackBlock.BATTERY);
        return readBooleanProperty(tag, OPEN_TAG, state, BackpackBlock.OPEN);
    }

    private static BlockState readBooleanProperty(
            CompoundTag tag,
            String key,
            BlockState state,
            BooleanProperty property) {
        return state.hasProperty(property) && tag.contains(key)
                ? state.setValue(property, tag.getBoolean(key))
                : state;
    }
}
