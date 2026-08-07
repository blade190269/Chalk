package io.github.mortuusars.chalk.world.block;

import com.mojang.serialization.MapCodec;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.client.ClientHelper;
import io.github.mortuusars.chalk.utils.ParticleUtils;
import io.github.mortuusars.chalk.utils.PositionUtils;
import io.github.mortuusars.chalk.world.chalk.Mark;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Optional;

public class MarkBlock extends BaseEntityBlock {
    public static final MapCodec<MarkBlock> CODEC = simpleCodec(MarkBlock::new);

    public static final VoxelShape SHAPE_DOWN = Block.box(1.5D, 15.5D, 1.5D, 14.5D, 16D, 14.5D);
    public static final VoxelShape SHAPE_UP = Block.box(1.5D, 0D, 1.5D, 14.5D, 0.5D, 14.5D);
    public static final VoxelShape SHAPE_NORTH = Block.box(1.5D, 1.5D, 15.5D, 14.5D, 14.5D, 16D);
    public static final VoxelShape SHAPE_SOUTH = Block.box(1.5D, 1.5D, 0D, 14.5D, 14.5D, 0.5D);
    public static final VoxelShape SHAPE_WEST = Block.box(15.5D, 1.5D, 1.5D, 16D, 14.5D, 14.5D);
    public static final VoxelShape SHAPE_EAST = Block.box(0D, 1.5D, 1.5D, 0.5D, 14.5D, 14.5D);

    public static final VoxelShape[] SHAPES = new VoxelShape[]{
          SHAPE_DOWN,
          SHAPE_UP,
          SHAPE_NORTH,
          SHAPE_SOUTH,
          SHAPE_WEST,
          SHAPE_EAST
    };

    public MarkBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MarkBlockEntity(pos, state);
    }

    // --

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        //TODO: optimize
        if (level.getBlockEntity(pos) instanceof MarkBlockEntity markBlockEntity) {
            VoxelShape shape = Shapes.empty();
            for (int i = 0; i < 6; i++) {
                if (markBlockEntity.getMarks().get(i) != null) {
                    shape = Shapes.or(shape, SHAPES[i]);
                }
            }
            return shape;
        }
        return Shapes.empty();
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return super.getCloneItemStack(level, pos, state);
    }

    //    @Override
//    public @NotNull ItemStack getCloneItemStack(LevelReader pLevel, BlockPos pPos, BlockState pState) {
//        //TODO: Check in survival. Maybe client specific code (to check Minecraft#player)?
//        return new ItemStack(Chalk.Items.getChalk(this.color));
//    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return !(level.getBlockEntity(pos) instanceof MarkBlockEntity markBlockEntity) || !markBlockEntity.getMarks().isEmpty();
    }

    // --

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack usedStack = player.getItemInHand(hand);

        if (usedStack.is(Chalk.Tags.Items.GLOWINGS)
              && level.getBlockEntity(pos) instanceof MarkBlockEntity blockEntity
              && getClickedMark(level, hitResult.getLocation()) instanceof DrawnMark(Direction facing, Mark existingMark)
              && !existingMark.glowing()) {

            if (!player.isCreative()) {
                usedStack.shrink(1);
            }

            level.playSound(null, pos, Chalk.SoundEvents.GLOW_APPLIED.get(), SoundSource.BLOCKS, 1f, 1f);
            level.playSound(null, pos, Chalk.SoundEvents.GLOWING.get(), SoundSource.BLOCKS, 0.8f, 1f);
            ParticleUtils.spawnParticle(level, ParticleTypes.END_ROD, PositionUtils.blockCenterOffsetToFace(pos, facing, 0.3f),
                  new Vector3f(0f, 0.03f, 0f), 2);

            Mark mark = existingMark.withGlowing(true);
            blockEntity.getMarks().set(facing, mark);
            blockEntity.marksChanged();

            if (player instanceof ServerPlayer serverPlayer) {
                BlockPos surfacePos = pos.relative(facing.getOpposite());
                Chalk.CriteriaTriggers.MARK_GLOWING.get().trigger(
                      serverPlayer, mark, pos, level.getBlockState(surfacePos).getMapColor(level, pos));
            }

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static @Nullable DrawnMark getClickedMark(BlockGetter level, Vec3 clickLocation) {
        BlockPos pos = BlockPos.containing(clickLocation);

        if (!(level.getBlockEntity(pos) instanceof MarkBlockEntity blockEntity)) {
            return null;
        }

        Vec3 location = clickLocation.subtract(Vec3.atLowerCornerOf(pos));

        double closestDistance = Double.MAX_VALUE;
        @Nullable Direction closestDirection = null;

        for (int index : blockEntity.getMarks().getIndices()) {
            VoxelShape shape = SHAPES[index];

            Optional<Vec3> closestPoint = shape.closestPointTo(location);
            if (closestPoint.isEmpty()) {
                continue;
            }

            double distance = closestPoint.get().distanceToSqr(location);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestDirection = Direction.from3DDataValue(index);
            }
        }

        return closestDirection != null
              ? new DrawnMark(closestDirection, blockEntity.getMarks().get(closestDirection.get3DDataValue()))
              : null;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        recheckMarks(level, pos);
    }

    public void recheckMarks(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof MarkBlockEntity blockEntity)) {
            return;
        }

        if (blockEntity.getMarks().isEmpty()) {
            level.removeBlock(pos, false);
            return;
        }

        for (int i = 0; i < 6; i++) {
            @Nullable Mark mark = blockEntity.getMarks().get(i);
            if (mark != null) {
                Direction facing = Direction.from3DDataValue(i);
                if (!canMarkSurvive(level, pos, facing)) {
                    removeMarkWithEffects(level, pos, facing);
                }
            }
        }
    }

    public boolean canMarkSurvive(Level level, BlockPos pos, Direction facing) {
        BlockPos surfacePos = pos.relative(facing.getOpposite());
        BlockState surfaceBlockState = level.getBlockState(surfacePos);
        return !surfaceBlockState.is(Chalk.Tags.Blocks.CHALK_CANNOT_DRAW_ON)
              && surfaceBlockState.isFaceSturdy(level, surfacePos, facing);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof MarkBlockEntity markBlockEntity) {
            markBlockEntity.getMarks().forEach((facing, mark) -> {
                if (mark.glowing() && random.nextInt(90) == 0) {
                    ParticleUtils.spawnParticle(level, ParticleTypes.END_ROD, PositionUtils.blockCenterOffsetToFace(pos, facing,
                          0.33f), new Vector3f(0f, facing == Direction.DOWN ? -0.005f : 0.015f, 0f), 1);
                }
            });
        }
    }

    public void removeMarkWithEffects(Level level, BlockPos pos, Direction facing) {
        if (level.getBlockEntity(pos) instanceof MarkBlockEntity blockEntity
              && blockEntity.getMarks().get(facing) instanceof Mark mark) {

            blockEntity.getMarks().remove(facing);
            blockEntity.marksChanged();

            if (level instanceof ServerLevel serverLevel) {
                Vector3f centerOffset = PositionUtils.blockCenterOffsetToFace(pos, facing, 0.25f);
                Vector3f color = new Vector3f(
                      FastColor.ARGB32.red(mark.color()) / 255f,
                      FastColor.ARGB32.green(mark.color()) / 255f,
                      FastColor.ARGB32.blue(mark.color()) / 255f);
                serverLevel.sendParticles(new DustParticleOptions(color, 2f),
                      centerOffset.x(), centerOffset.y(), centerOffset.z(),
                      1, 0.1, 0.1, 0.1, 0.02);

                level.playSound(null, pos, Chalk.SoundEvents.MARK_REMOVED.get(), SoundSource.BLOCKS,
                      0.5f, level.getRandom().nextFloat() * 0.2f + 0.8f);
            }

            if (blockEntity.getMarks().isEmpty()) {
                level.removeBlock(pos, false);
            }
        }
    }

    /**
     * Handles mark destroying. One at a time.<br>
     * This method is not called when player is in creative mode, we handle that separately in {@link io.github.mortuusars.chalk.mixin.MultiPlayerGameModeMixin}.
     */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (player.level().isClientSide()) {
            ClientHelper.attackMarkBlock(pos);
        }
    }

    /**
     * We handle mark destroying in {@link MarkBlock#attack}.<br>
     * Returning 0 here prevents usual block behavior.
     */
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return 0;
    }
}
