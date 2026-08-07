package io.github.mortuusars.chalk.world.chalk;

import io.github.mortuusars.chalk.utils.Codecs;
import io.github.mortuusars.chalk.utils.GridCell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public record MarkDrawingContext(InteractionHand hand, Vec3 clickLocation, BlockPos markPos, Direction markFacing) {
    public static final StreamCodec<FriendlyByteBuf, MarkDrawingContext> STREAM_CODEC = StreamCodec.composite(
          Codecs.INTERACTION_HAND, MarkDrawingContext::hand,
          Codecs.VEC3, MarkDrawingContext::clickLocation,
          BlockPos.STREAM_CODEC, MarkDrawingContext::markPos,
          Direction.STREAM_CODEC, MarkDrawingContext::markFacing,
          MarkDrawingContext::new
    );

    public GridCell clickedCell() {
        return GridCell.fromClickLocation(clickLocation, markFacing);
    }

    public BlockPos surfacePos() {
        return markPos.relative(markFacing.getOpposite());
    }

    // --

    // --

    //    public NewMarkDrawingContext(Player player, @NotNull BlockHitResult hitResult, InteractionHand drawingHand) {
//        this.markFacing = hitResult.getDirection();
//        this.markBlockPos = hitResult.getBlockPos().relative(markFacing);
//        this.drawingHand = drawingHand;
//        this.clickedCell = GridCell.fromClickLocation(hitResult.getLocation(), hitResult.getDirection());
//        this.existingMarkBlockEntity = level.getBlockEntity(getMarkBlockPos()) instanceof MarkBlockEntity existingBlockEntity
//              ? Optional.of(existingBlockEntity)
//              : Optional.empty();
//        this.existingMark = existingMarkBlockEntity.map(be -> be.getMarks().get(markFacing.get3DDataValue()));
//    }

//    public static void storeContext(MarkDrawingContext context) {
//        storedContext = context;
//    }
//
//    public static @Nullable MarkDrawingContext getStoredContext() {
//        return storedContext;
//    }
//
//    public static void clearStoredContext() {
//        storedContext = null;
//    }

    // --

//    public Player getPlayer() {
//        return player;
//    }
//
//    public Level getLevel() {
//        return level;
//    }
//
//    public BlockHitResult getHitResult() {
//        return hitResult;
//    }
//
//    public Direction getMarkFacing() {
//        return markFacing;
//    }
//
//    public BlockPos getMarkBlockPos() {
//        return markBlockPos;
//    }
//
//    public InteractionHand getHand() {
//        return drawingHand;
//    }
//
//    public GridCell getClickedCell() {
//        return clickedCell;
//    }
//
//    public Optional<MarkBlockEntity> getExistingMarkBlockEntity() {
//        return existingMarkBlockEntity;
//    }
//
//    public Optional<Mark> getExistingMark() {
//        return existingMark;
//    }

    // --

//    public boolean canDraw() {
//        BlockPos pos = hitResult.getBlockPos();
//        Direction face = getMarkFacing();
//        BlockState surfaceBlockState = level.getBlockState(pos);
//        BlockState markPosState = level.getBlockState(pos.relative(face));
//        return (markPosState.isAir() || markPosState.getBlock() instanceof MarkBlock)
//              && Block.isFaceFull(surfaceBlockState.getCollisionShape(level, pos), face)
//              && !surfaceBlockState.is(Chalk.Tags.Blocks.CHALK_CANNOT_DRAW_ON);
//    }
//
//    public boolean shouldMarkReplaceAnother(Mark newMark, Mark oldMark) {
//        if (!newMark.symbol().equals(oldMark.symbol())) return true;
//        if (newMark.orientation() != oldMark.orientation()) return true;
//        if (newMark.color() != oldMark.color()) return true;
//        return newMark.glowing() && !oldMark.glowing();
//    }
//
//    // --
//
//    public Mark createRegularMark(int color, boolean glowing) {
//        Holder<MarkSymbol> symbol = getClickedCell() == GridCell.CENTER
//              ? MarkSymbol.getOrThrow(level.registryAccess(), MarkSymbol.CENTER)
//              : MarkSymbol.getOrThrow(level.registryAccess(), MarkSymbol.ARROW);
//        return createMark(color, symbol, glowing);
//    }
//
//    public Mark createMark(int color, Holder<MarkSymbol> symbol, boolean glowing) {
//        Direction face = getMarkFacing();
//
//        MarkSymbol.OrientationBehavior orientationBehavior = symbol.value().orientationBehavior();
//
//        SymbolOrientation orientation;
//
//        if (orientationBehavior == MarkSymbol.OrientationBehavior.FULL)
//            orientation = SymbolOrientation.fromCell(clickedCell);
//        else if (orientationBehavior == MarkSymbol.OrientationBehavior.CARDINAL)
//            orientation = SymbolOrientation.fromClickLocationCardinal(hitResult.getLocation(), face);
//        else if (orientationBehavior == MarkSymbol.OrientationBehavior.UP_DOWN_CARDINAL && (face == Direction.UP || face == Direction.DOWN))
//            orientation = SymbolOrientation.fromRotation(player.getDirection().getOpposite().get2DDataValue() * 90);
//        else
//            orientation = SymbolOrientation.CENTER;
//
//        return new Mark(symbol, color, orientation, glowing);
//    }

    // --

//    public void openSymbolSelectionScreen() {
//        if (level.isClientSide()) {
//            storeContext(this);
//            return;
//        }
//
//        if (player instanceof ServerPlayer serverPlayer) {
//            List<OldMarkSymbol> unlockedSymbols = SymbolUnlocking.getUnlockedSymbols(serverPlayer);
//
//            if (!unlockedSymbols.isEmpty())
//                Packets.sendToClient(new SelectSymbolS2CP(unlockedSymbols), serverPlayer);
//            else
//                player.displayClientMessage(Component.translatable("gui.chalk.no_symbols_unlocked").withStyle(ChatFormatting.RED), true);
//        }
//    }
}
