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
}
