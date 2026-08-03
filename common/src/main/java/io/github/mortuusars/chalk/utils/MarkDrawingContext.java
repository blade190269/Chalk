package io.github.mortuusars.chalk.utils;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.block.MarkBlock;
import io.github.mortuusars.chalk.world.block.MarkBlockEntity;
import io.github.mortuusars.chalk.core.OldMarkSymbol;
import io.github.mortuusars.chalk.world.chalk.symbol.SymbolOrientation;
import io.github.mortuusars.chalk.core.SymbolUnlocking;
import io.github.mortuusars.chalk.network.Packets;
import io.github.mortuusars.chalk.network.packet.clientbound.SelectSymbolS2CP;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MarkDrawingContext {
    @Nullable
    private static MarkDrawingContext storedContext;

    protected final Player player;
    protected final Level level;
    protected final BlockHitResult hitResult;
    protected final Direction markFacing;
    protected final BlockPos markBlockPos;
    protected final InteractionHand drawingHand;
    protected final GridCell clickedCell;
    protected final Optional<MarkBlockEntity> existingMarkBlockEntity;
    protected final Optional<Mark> existingMark;

    public MarkDrawingContext(Player player, @NotNull BlockHitResult hitResult, InteractionHand drawingHand) {
        this.player = player;
        this.level = player.level();
        this.hitResult = hitResult;
        this.markFacing = hitResult.getDirection();
        this.markBlockPos = hitResult.getBlockPos().relative(markFacing);
        this.drawingHand = drawingHand;
        this.clickedCell = GridCell.fromClickLocation(hitResult.getLocation(), hitResult.getDirection());
        this.existingMarkBlockEntity = level.getBlockEntity(getMarkBlockPos()) instanceof MarkBlockEntity existingBlockEntity
              ? Optional.of(existingBlockEntity)
              : Optional.empty();
        this.existingMark = existingMarkBlockEntity.map(be -> be.getMarks().get(markFacing.get3DDataValue()));
    }

    public static void storeContext(MarkDrawingContext context) {
        storedContext = context;
    }

    public static @Nullable MarkDrawingContext getStoredContext() {
        return storedContext;
    }

    public static void clearStoredContext() {
        storedContext = null;
    }

    // --

    public Player getPlayer() {
        return player;
    }

    public Level getLevel() {
        return level;
    }

    public BlockHitResult getHitResult() {
        return hitResult;
    }

    public Direction getMarkFacing() {
        return markFacing;
    }

    public BlockPos getMarkBlockPos() {
        return markBlockPos;
    }

    public InteractionHand getDrawingHand() {
        return drawingHand;
    }

    public GridCell getClickedCell() {
        return clickedCell;
    }

    public Optional<MarkBlockEntity> getExistingMarkBlockEntity() {
        return existingMarkBlockEntity;
    }

    public Optional<Mark> getExistingMark() {
        return existingMark;
    }

    // --

    public boolean canDraw() {
        BlockPos pos = hitResult.getBlockPos();
        Direction face = getMarkFacing();
        BlockState surfaceBlockState = level.getBlockState(pos);
        BlockState markPosState = level.getBlockState(pos.relative(face));
        return (markPosState.isAir() || markPosState.getBlock() instanceof MarkBlock)
              && Block.isFaceFull(surfaceBlockState.getCollisionShape(level, pos), face)
              && !surfaceBlockState.is(Chalk.Tags.Blocks.CHALK_CANNOT_DRAW_ON);
    }

    public boolean shouldMarkReplaceAnother(Mark newMark, Mark oldMark) {
        if (!newMark.symbol().equals(oldMark.symbol())) return true;
        if (newMark.orientation() != oldMark.orientation()) return true;
        if (newMark.color() != oldMark.color()) return true;
        return newMark.glowing() && !oldMark.glowing();
    }

    // --

    public Mark createRegularMark(int color, boolean glowing) {
        Holder<MarkSymbol> symbol = getClickedCell() == GridCell.CENTER
              ? MarkSymbol.getOrThrow(level.registryAccess(), MarkSymbol.CENTER)
              : MarkSymbol.getOrThrow(level.registryAccess(), MarkSymbol.ARROW);
        return createMark(color, symbol, glowing);
    }

    public Mark createMark(int color, Holder<MarkSymbol> symbol, boolean glowing) {
        Direction face = getMarkFacing();

        MarkSymbol.OrientationBehavior orientationBehavior = symbol.value().orientationBehavior();

        SymbolOrientation orientation;

        if (orientationBehavior == MarkSymbol.OrientationBehavior.FULL)
            orientation = SymbolOrientation.fromCell(clickedCell);
        else if (orientationBehavior == MarkSymbol.OrientationBehavior.CARDINAL)
            orientation = SymbolOrientation.fromClickLocationCardinal(hitResult.getLocation(), face);
        else if (orientationBehavior == MarkSymbol.OrientationBehavior.UP_DOWN_CARDINAL && (face == Direction.UP || face == Direction.DOWN))
            orientation = SymbolOrientation.fromRotation(player.getDirection().getOpposite().get2DDataValue() * 90);
        else
            orientation = SymbolOrientation.CENTER;

        return new Mark(symbol, color, orientation, glowing);
    }

    // --

    public void openSymbolSelectionScreen() {
        if (level.isClientSide()) {
            storeContext(this);
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            List<OldMarkSymbol> unlockedSymbols = SymbolUnlocking.getUnlockedSymbols(serverPlayer);

            if (!unlockedSymbols.isEmpty())
                Packets.sendToClient(new SelectSymbolS2CP(unlockedSymbols), serverPlayer);
            else
                player.displayClientMessage(Component.translatable("gui.chalk.no_symbols_unlocked").withStyle(ChatFormatting.RED), true);
        }
    }

}
