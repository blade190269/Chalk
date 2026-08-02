package io.github.mortuusars.chalk.network.packet.serverbound;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.item.ChalkBoxItem;
import io.github.mortuusars.chalk.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record OpenCreativeChalkBoxC2SP(int chalkBoxSlotIndex) implements Packet {
    public static final CustomPacketPayload.Type<OpenCreativeChalkBoxC2SP> TYPE = new CustomPacketPayload.Type<>(Chalk.resource("open_creative_chalk_box"));
    public static final StreamCodec<FriendlyByteBuf, OpenCreativeChalkBoxC2SP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, OpenCreativeChalkBoxC2SP::chalkBoxSlotIndex,
            OpenCreativeChalkBoxC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (!player.isCreative()) {
            Chalk.LOGGER.error("Cannot open Chalk Box. Player is not in creative mode.");
            return false;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            Chalk.LOGGER.error("Cannot open Chalk Box. Player is not ServerPlayer.");
            return false;
        }

        int slotId = chalkBoxSlotIndex();

        ItemStack itemStack = player.getInventory().getItem(slotId);
        if (itemStack.getItem() instanceof ChalkBoxItem chalkBoxItem) {
//            chalkBoxItem.openGUI(serverPlayer, itemStack);
        }
        else {
            Chalk.LOGGER.error("Cannot open Chalk Box. Item in slot '{}' is not a ChalkBoxItem but '{}'", slotId, itemStack);
        }

        return true;
    }
}
