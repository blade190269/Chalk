package io.github.mortuusars.chalk.network.packet.serverbound;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.chalk.MarkDrawingContext;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.chalk.world.item.MarkDrawable;
import io.github.mortuusars.chalk.network.packet.Packet;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record DrawMarkC2SP(Holder<MarkSymbol> symbol, MarkDrawingContext drawingContext) implements Packet {
    public static final CustomPacketPayload.Type<DrawMarkC2SP> TYPE = new CustomPacketPayload.Type<>(Chalk.resource("draw_mark"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DrawMarkC2SP> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.holderRegistry(Chalk.Registries.MARK_SYMBOL), DrawMarkC2SP::symbol,
          MarkDrawingContext.STREAM_CODEC, DrawMarkC2SP::drawingContext,
          DrawMarkC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ItemStack itemInHand = player.getItemInHand(drawingContext.hand());
        if (!(itemInHand.getItem() instanceof MarkDrawable drawable)) {
            Chalk.LOGGER.error("{} is not a drawing tool.", itemInHand);
            return true;
        }

        //TODO: check if symbol is unlocked

        drawable.drawMark(player, drawingContext, drawable.createMark(player, drawingContext, itemInHand, symbol));
        return true;
    }
}