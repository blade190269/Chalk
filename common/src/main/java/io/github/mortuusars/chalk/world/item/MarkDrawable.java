package io.github.mortuusars.chalk.world.item;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.advancements.AdvancementUtils;
import io.github.mortuusars.chalk.network.Packets;
import io.github.mortuusars.chalk.network.packet.clientbound.SelectSymbolAndDrawMarkS2CP;
import io.github.mortuusars.chalk.utils.GridCell;
import io.github.mortuusars.chalk.utils.PositionUtils;
import io.github.mortuusars.chalk.world.block.DrawnMark;
import io.github.mortuusars.chalk.world.block.MarkBlock;
import io.github.mortuusars.chalk.world.block.MarkBlockEntity;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.chalk.world.chalk.MarkDrawingContext;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.chalk.world.chalk.symbol.SymbolOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public interface MarkDrawable {
    int getMarkDrawingColor(ItemStack stack);

    default boolean shouldDrawGlowingMark(ItemStack stack) {
        return false;
    }

    default MarkDrawingContext createMarkDrawingContext(Player player, InteractionHand hand, Vec3 clickedLocation,
                                                        BlockPos clickedPos, Direction clickedFace) {
        BlockPos markPos = clickedPos;
        Direction markFacing = clickedFace;

        if (player.level().getBlockState(clickedPos).getBlock() instanceof MarkBlock
              && MarkBlock.getClickedMark(player.level(), clickedLocation) instanceof DrawnMark existingMark) {
            markFacing = existingMark.facing();
        } else {
            markPos = clickedPos.relative(clickedFace);
        }

        return new MarkDrawingContext(hand, clickedLocation, markPos, markFacing);
    }

    default Mark createRegularMark(Player player, MarkDrawingContext context, ItemStack stack) {
        Holder<MarkSymbol> symbol = context.clickedCell() == GridCell.CENTER
              ? MarkSymbol.getOrThrow(player.level().registryAccess(), MarkSymbol.DOT)
              : MarkSymbol.getOrThrow(player.level().registryAccess(), MarkSymbol.ARROW);
        return createMark(player, context, stack, symbol);
    }

    default Mark createMark(Player player, MarkDrawingContext context, ItemStack stack, Holder<MarkSymbol> symbol) {
        Direction facing = context.markFacing();
        GridCell clickedCell = context.clickedCell();

        MarkSymbol.OrientationBehavior orientationBehavior = symbol.value().orientationBehavior();

        SymbolOrientation orientation = switch (orientationBehavior) {
            case FULL -> SymbolOrientation.fromCell(clickedCell);
            case CARDINAL -> SymbolOrientation.fromClickLocationCardinal(context.clickLocation(), facing);
            case UP_DOWN_CARDINAL -> {
                if (facing != Direction.UP && facing != Direction.DOWN) {
                    yield SymbolOrientation.CENTER;
                }

                Direction direction = player.getDirection();
                if (facing == Direction.UP) {
                    direction = direction.getOpposite();
                }

                yield SymbolOrientation.fromRotation(direction.get2DDataValue() * 90);
            }
            case FIXED -> SymbolOrientation.CENTER;
        };

        return new Mark(symbol, getMarkDrawingColor(stack), orientation, shouldDrawGlowingMark(stack));
    }

    default void selectSymbolAndDraw(Player player, MarkDrawingContext context) {
        if (player instanceof ServerPlayer serverPlayer) {
            List<Holder<MarkSymbol>> availableSymbols = MarkSymbol.getAllHolders(serverPlayer.registryAccess())
                  .filter(holder -> isSymbolAvailable(serverPlayer, context, holder))
                  .toList();

            Packets.sendToClient(new SelectSymbolAndDrawMarkS2CP(availableSymbols, context), serverPlayer);
        }
    }

    default boolean isSymbolAvailable(ServerPlayer player, MarkDrawingContext context, Holder<MarkSymbol> symbol) {
        return player.isCreative()
              || !Config.Server.SYMBOL_UNLOCKING.get()
              || symbol.value().requiredAdvancement()
              .map(advancement -> AdvancementUtils.hasAdvancement(player, advancement))
              .orElse(true);
    }

    default boolean canDrawMark(Player player, MarkDrawingContext context) {
        BlockState markPosState = player.level().getBlockState(context.markPos());
        if (!markPosState.isAir() && !(markPosState.getBlock() instanceof MarkBlock)) {
            return false;
        }

        BlockPos surfacePos = context.surfacePos();
        BlockState surfaceBlockState = player.level().getBlockState(surfacePos);

        return Block.isFaceFull(surfaceBlockState.getCollisionShape(player.level(), surfacePos), context.markFacing())
              && !surfaceBlockState.is(Chalk.Tags.Blocks.CHALK_CANNOT_DRAW_ON);
    }

    default @Nullable Mark getExistingMark(Level level, MarkDrawingContext context) {
        return level.getBlockEntity(context.markPos()) instanceof MarkBlockEntity blockEntity
              ? blockEntity.getMarks().get(context.markFacing())
              : null;
    }

    default boolean shouldMarkReplaceAnother(Mark oldMark, Mark newMark) {
        if (!newMark.symbol().equals(oldMark.symbol())) return true;
        if (newMark.orientation() != oldMark.orientation()) return true;
        if (newMark.color() != oldMark.color()) return true;
        return newMark.glowing() && !oldMark.glowing();
    }

    default boolean drawMark(Player player, MarkDrawingContext context, Mark mark) {
        if (getExistingMark(player.level(), context) instanceof Mark existingMark
              && !shouldMarkReplaceAnother(existingMark, mark)) {
            return false;
        }

        if (placeMark(player.level(), context.markPos(), context.markFacing(), mark) instanceof Mark drawnMark) {
            onMarkDrawn(player, context, drawnMark);
            player.swing(context.hand());
            return true;
        }

        return false;
    }

    default @Nullable Mark placeMark(Level level, BlockPos markPos, Direction markFacing, Mark mark) {
        if (!(MarkBlock.getExistingOrPlaceNew(level, markPos) instanceof MarkBlockEntity blockEntity)) {
            return null;
        }

        @Nullable Mark existingMark = blockEntity.getMarks().get(markFacing);
        if (existingMark != null) {
            mark = calculateNewMark(level, existingMark, mark);
        }

        blockEntity.getMarks().set(markFacing, mark);
        blockEntity.marksChanged();
        return mark;
    }

    default Mark calculateNewMark(Level level, Mark oldMark, Mark newMark) {
        if (oldMark.isSameMarkDifferentColors(newMark)) {
            return newMark
                  .lerpColor(oldMark.color(), 0.5f)
                  .withGlowing(oldMark.glowing() || newMark.glowing());
        }
        return newMark;
    }

    default void onMarkDrawn(Player player, MarkDrawingContext context, Mark mark) {
        if (player instanceof ServerPlayer serverPlayer) {
            float R = (mark.color() & 0x00FF0000) >> 16;
            float G = (mark.color() & 0x0000FF00) >> 8;
            float B = (mark.color() & 0x000000FF);

            Vector3f pos = PositionUtils.blockCenterOffsetToFace(context.markPos(), context.markFacing(), 0.25f);

            serverPlayer.serverLevel().sendParticles(new DustParticleOptions(new Vector3f(R / 255, G / 255, B / 255), 2f),
                  pos.x(), pos.y(), pos.z(), 1, 0, 0, 0, 0);
            serverPlayer.serverLevel().playSound(null, pos.x(), pos.y(), pos.z(), Chalk.SoundEvents.MARK_DRAW.get(),
                  SoundSource.BLOCKS, 0.7f, serverPlayer.getRandom().nextFloat() * 0.2f + 0.8f);

            if (mark.glowing()) {
                serverPlayer.serverLevel().playSound(null, context.markPos(),
                      Chalk.SoundEvents.GLOWING.get(), SoundSource.BLOCKS, 0.8f, 1f);
                serverPlayer.serverLevel().sendParticles(ParticleTypes.END_ROD, pos.x(), pos.y(), pos.z(), 1, 0, 0, 0, 0);
            }
        }
    }


//    default MarkDrawingContext createMarkDrawingContext(UseOnContext context) {
//        Preconditions.checkArgument(context.getPlayer() != null, "Player should not be null here.");
//        return createMarkDrawingContext(context.getPlayer(), context.getClickedPos(), context.getClickLocation(),
//              context.getClickedFace(), context.getHand());
//    }
//
//    default MarkDrawingContext createMarkDrawingContext(@NotNull Player player, BlockPos clickedPos, Vec3 clickLocation,
//                                                        Direction clickedFace, InteractionHand drawingHand) {
//        Level level = player.level();
//        Direction facing = clickedFace;
//        BlockPos surfacePos = clickedPos;
//
//        if (level.getBlockState(surfacePos).getBlock() instanceof MarkBlock
//              && MarkBlock.getClickedMark(level, clickLocation) instanceof DrawnMark existingMark) {
//            facing = existingMark.facing();
//        }
//
//        if (level.getBlockEntity(surfacePos) instanceof MarkBlockEntity) {
//            surfacePos = surfacePos.relative(facing.getOpposite());
//        }
//
//        BlockHitResult hitResult = new BlockHitResult(clickLocation, facing, surfacePos, false);
//        return new MarkDrawingContext(player, hitResult, drawingHand);
//    }
//
//    default boolean drawMark(MarkDrawingContext context, Mark mark) {
//        if (context.getExistingMark().map(oldMark -> !context.shouldMarkReplaceAnother(mark, oldMark)).orElse(false)) {
//            return false;
//        }
//
//        return drawMark(context.getPlayer(), context.getDrawingHand(), context.getMarkBlockPos(), context.getMarkFacing(), mark);
//    }
//
//    default boolean drawMark(Player player, InteractionHand drawingHand, BlockPos markPos, Direction face, Mark mark) {
//        ItemStack drawingStack = player.getItemInHand(drawingHand);
//
//        if (!(drawingStack.getItem() instanceof MarkDrawable drawable)
//              || !(getExistingBlockOrPlaceNew(player.level(), markPos) instanceof MarkBlockEntity blockEntity)) {
//            return false;
//        }
//
//        @Nullable Mark existingMark = blockEntity.getMarks().get(face);
//        if (existingMark != null && existingMark.isSameMarkDifferentColors(mark)) {
//            mark = mark
//                  .lerpColor(existingMark.color(), 0.5f)
//                  .withGlowing(existingMark.glowing() || mark.glowing());
//        }
//
//        blockEntity.getMarks().set(face, mark);
//        blockEntity.marksChanged();
//        drawable.onMarkDrawn(player, drawingHand, markPos, face, mark);
//        player.swing(drawingHand);
//        return true;
//    }
//
//
//
//    default void onMarkDrawn(Player player, NewMarkDrawingContext context, Mark mark) {
//        if (player instanceof ServerPlayer serverPlayer) {
//            float R = (mark.color() & 0x00FF0000) >> 16;
//            float G = (mark.color() & 0x0000FF00) >> 8;
//            float B = (mark.color() & 0x000000FF);
//
//            Vector3f pos = PositionUtils.blockCenterOffsetToFace(context.markPos(), context.markFacing(), 0.25f);
//
//            serverPlayer.serverLevel().sendParticles(new DustParticleOptions(new Vector3f(R / 255, G / 255, B / 255), 2f),
//                  pos.x(), pos.y(), pos.z(), 1, 0, 0, 0, 0);
//            serverPlayer.serverLevel().playSound(null, pos.x(), pos.y(), pos.z(), Chalk.SoundEvents.MARK_DRAW.get(),
//                  SoundSource.BLOCKS, 0.7f, serverPlayer.getRandom().nextFloat() * 0.2f + 0.8f);
//
//            if (mark.glowing()) {
//                serverPlayer.serverLevel().playSound(null, context.markPos(), Chalk.SoundEvents.GLOWING.get(), SoundSource.BLOCKS, 0.8f, 1f);
//                serverPlayer.serverLevel().sendParticles(ParticleTypes.END_ROD,
//                      pos.x(), pos.y(), pos.z(), 1, 0, 0, 0, 0);
//            }
//        }
//    }
}
