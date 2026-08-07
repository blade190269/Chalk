package io.github.mortuusars.chalk.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class Codecs {
    public static final StreamCodec<ByteBuf, Vec3> VEC3 = StreamCodec.of(
          (buf, vec) -> {
              buf.writeDouble(vec.x);
              buf.writeDouble(vec.y);
              buf.writeDouble(vec.z);
          },
          buf -> new Vec3(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
          )
    );

    public static final StreamCodec<ByteBuf, InteractionHand> INTERACTION_HAND = ByteBufCodecs.idMapper(
          ByIdMap.continuous(Enum::ordinal, InteractionHand.values(), ByIdMap.OutOfBoundsStrategy.WRAP), Enum::ordinal);

    public static final StreamCodec<FriendlyByteBuf, BlockHitResult> BLOCK_HIT_RESULT = StreamCodec.composite(
          VEC3, BlockHitResult::getLocation,
          Direction.STREAM_CODEC, BlockHitResult::getDirection,
          BlockPos.STREAM_CODEC, BlockHitResult::getBlockPos,
          ByteBufCodecs.BOOL, BlockHitResult::isInside,
          BlockHitResult::new
    );
}
