package io.github.mortuusars.chalk.world.block;

import com.mojang.serialization.MapCodec;
import io.github.mortuusars.chalk.utils.ParticleUtils;
import io.github.mortuusars.chalk.utils.PositionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class MarkBlock extends BaseEntityBlock {
    public static final MapCodec<MarkBlock> CODEC = simpleCodec(MarkBlock::new);

    private static final VoxelShape SHAPE_DOWN = Block.box(1.5D, 15.5D, 1.5D, 14.5D, 16D, 14.5D);
    private static final VoxelShape SHAPE_UP = Block.box(1.5D, 0D, 1.5D, 14.5D, 0.5D, 14.5D);
    private static final VoxelShape SHAPE_NORTH = Block.box(1.5D, 1.5D, 15.5D, 14.5D, 14.5D, 16D);
    private static final VoxelShape SHAPE_SOUTH = Block.box(1.5D, 1.5D, 0D, 14.5D, 14.5D, 0.5D);
    private static final VoxelShape SHAPE_WEST = Block.box(15.5D, 1.5D, 1.5D, 16D, 14.5D, 14.5D);
    private static final VoxelShape SHAPE_EAST = Block.box(0D, 1.5D, 1.5D, 0.5D, 14.5D, 14.5D);

    private static final VoxelShape[] shapes = new VoxelShape[] {
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
        if (level.getBlockEntity(pos) instanceof MarkBlockEntity markBlockEntity) {
            VoxelShape shape = Shapes.empty();
            for (int i = 0; i < 6; i++) {
                if (markBlockEntity.getMarks().get(i) != null) {
                    shape = Shapes.or(shape, shapes[i]);
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

    @Override
    public void attack(BlockState blockState, Level level, BlockPos pos, Player player) {
//        removeMarkWithEffects(level, pos);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof MarkBlockEntity markBlockEntity) {
            markBlockEntity.getMarks().forEach((facing, mark) -> {
                if (mark.glowing() && random.nextInt(90) == 0) {
                    ParticleUtils.spawnParticle(level, ParticleTypes.END_ROD, PositionUtils.blockCenterOffsetToFace(pos, facing,
                          0.33f), new Vector3f(0f, 0.015f, 0f), 1);
                }
            });
        }
    }
}
