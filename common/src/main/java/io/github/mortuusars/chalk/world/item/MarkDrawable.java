package io.github.mortuusars.chalk.world.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.utils.PositionUtils;
import io.github.mortuusars.chalk.world.block.MarkBlockEntity;
import io.github.mortuusars.chalk.world.block.OldChalkMarkBlock;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import io.github.mortuusars.chalk.world.chalk.Mark;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Optional;

public interface MarkDrawable {
    int getMarkColorValue(ItemStack stack);

    Optional<DyeColor> getMarkColor(ItemStack stack);

    boolean isGlowing(ItemStack stack);

    default MarkDrawingContext createMarkDrawingContext(UseOnContext context) {
        Preconditions.checkArgument(context.getPlayer() != null, "Player should not be null here.");
        return createMarkDrawingContext(context.getPlayer(), context.getClickedPos(), context.getClickLocation(),
              context.getClickedFace(), context.getHand());
    }

    default MarkDrawingContext createMarkDrawingContext(@NotNull Player player, BlockPos clickedPos, Vec3 clickLocation,
                                                        Direction clickedFace, InteractionHand drawingHand) {
        Level level = player.level();
        Direction facing = clickedFace;
        BlockPos surfacePos = clickedPos;

        if (level.getBlockEntity(surfacePos) instanceof MarkBlockEntity markBlockEntity) {
            surfacePos = surfacePos.relative(facing.getOpposite());
        }

//        if (level.getBlockState(surfacePos).getBlock() instanceof OldChalkMarkBlock) {
//            facing = level.getBlockState(surfacePos).getValue(OldChalkMarkBlock.FACING);
//            surfacePos = surfacePos.relative(facing.getOpposite());
//        }

        BlockHitResult hitResult = new BlockHitResult(clickLocation, facing, surfacePos, false);
        return new MarkDrawingContext(player, hitResult, drawingHand);
    }

    default boolean drawMark(MarkDrawingContext context, Mark mark) {
        if (context.getExistingMark().map(oldMark -> !context.shouldMarkReplaceAnother(mark, oldMark)).orElse(false)) {
            return false;
        }

        return drawMark(context.getPlayer(), context.getDrawingHand(), context.getMarkBlockPos(), context.getMarkFacing(), mark);
    }

    default boolean drawMark(Player player, InteractionHand drawingHand, BlockPos markPos, Direction face, Mark mark) {
        ItemStack drawingStack = player.getItemInHand(drawingHand);

        if (!(drawingStack.getItem() instanceof MarkDrawable drawable)
              || !(getExistingOrPlaceNew(player.level(), markPos) instanceof MarkBlockEntity blockEntity)) {
            return false;
        }

        //TODO: calculate mark here

        blockEntity.getMarks().set(face, mark);
        blockEntity.setChanged();
        player.level().sendBlockUpdated(markPos, player.level().getBlockState(markPos), player.level().getBlockState(markPos), Block.UPDATE_ALL);
        drawable.onMarkDrawn(player, drawingHand, markPos, face, mark);
        player.swing(drawingHand);
        return true;
    }

    static @Nullable MarkBlockEntity getExistingOrPlaceNew(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MarkBlockEntity existingBlockEntity) {
            return existingBlockEntity;
        }

        level.setBlock(pos, Chalk.Blocks.MARK.get().defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);

        return level.getBlockEntity(pos) instanceof MarkBlockEntity existingBlockEntity
              ? existingBlockEntity
              : null;
    }

    default void onMarkDrawn(Player player, InteractionHand drawingHand, BlockPos markPos, Direction facing, Mark mark) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack drawingStack = player.getItemInHand(drawingHand);
            if (drawingStack.getItem() instanceof MarkDrawable drawable) {
                drawable.getMarkColor(drawingStack).ifPresent(color -> {
                    BlockPos surfacePos = markPos.relative(facing.getOpposite());
                    MapColor surfaceColor = player.level().getBlockState(surfacePos).getMapColor(player.level(), surfacePos);
                    Chalk.CriteriaTriggers.MARK_DRAWN.get().trigger(serverPlayer, surfaceColor, color); //TODO: add more data to trigger
                });
            }

            float R = (mark.color() & 0x00FF0000) >> 16;
            float G = (mark.color() & 0x0000FF00) >> 8;
            float B = (mark.color() & 0x000000FF);

            Vector3f pos = PositionUtils.blockCenterOffsetToFace(markPos, facing, 0.25f);

            serverPlayer.serverLevel().sendParticles(new DustParticleOptions(new Vector3f(R / 255, G / 255, B / 255), 2f),
                  pos.x(), pos.y(), pos.z(), 1, 0, 0, 0, 0);
            serverPlayer.serverLevel().playSound(null, pos.x(), pos.y(), pos.z(), Chalk.SoundEvents.MARK_DRAW.get(),
                  SoundSource.BLOCKS, 0.7f, serverPlayer.getRandom().nextFloat() * 0.2f + 0.8f);

            if (mark.glowing()) {
                serverPlayer.serverLevel().playSound(null, markPos, Chalk.SoundEvents.GLOWING.get(), SoundSource.BLOCKS, 0.8f, 1f);
                serverPlayer.serverLevel().sendParticles(ParticleTypes.END_ROD,
                      pos.x(), pos.y(), pos.z(), 1, 0, 0, 0, 0);
            }
        }
    }
}
