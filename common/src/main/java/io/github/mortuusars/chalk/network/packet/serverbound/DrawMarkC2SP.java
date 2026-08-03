package io.github.mortuusars.chalk.network.packet.serverbound;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.block.OldChalkMarkBlock;
import io.github.mortuusars.chalk.world.item.MarkDrawable;
import io.github.mortuusars.chalk.network.packet.Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public record DrawMarkC2SP(int color, CompoundTag blockStateNBT, BlockPos markPos, InteractionHand drawingHand) implements Packet {
    public static final CustomPacketPayload.Type<DrawMarkC2SP> TYPE = new CustomPacketPayload.Type<>(Chalk.resource("draw_mark"));
    public static final StreamCodec<FriendlyByteBuf, DrawMarkC2SP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, DrawMarkC2SP::color,
            ByteBufCodecs.COMPOUND_TAG, DrawMarkC2SP::blockStateNBT,
            BlockPos.STREAM_CODEC, DrawMarkC2SP::markPos,
            ByteBufCodecs.idMapper(ByIdMap.continuous(Enum::ordinal, InteractionHand.values(), ByIdMap.OutOfBoundsStrategy.WRAP), Enum::ordinal), DrawMarkC2SP::drawingHand,
            DrawMarkC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ItemStack itemInHand = player.getItemInHand(drawingHand());
        if (!(itemInHand.getItem() instanceof MarkDrawable drawable)) {
            Chalk.LOGGER.error("{} is not a drawing tool.", itemInHand);
            return true;
        }

        Level level = player.level();
        BlockState existingState = level.getBlockState(markPos());

        if (!(existingState.isAir() || existingState.getBlock() instanceof OldChalkMarkBlock)) {
            Chalk.LOGGER.error("Cannot draw at '{}': block is '{}'.", markPos(), existingState);
            return true;
        }

        BlockState blockState = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), blockStateNBT());

        if (!(blockState.getBlock() instanceof OldChalkMarkBlock)) {
            Chalk.LOGGER.error("Player {} tried to set invalid block through DrawMarkC2SP. State: {}.", player.getScoreboardName(), blockState);
            return true;
        }

//        return drawable.drawMark(player, drawingHand, markPos(), blockState, color(), drawingHand());
        return false;
    }
}