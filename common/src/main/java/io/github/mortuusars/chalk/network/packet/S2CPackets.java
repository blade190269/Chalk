package io.github.mortuusars.chalk.network.packet;

import io.github.mortuusars.chalk.network.packet.clientbound.SelectSymbolAndDrawMarkS2CP;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public class S2CPackets {
    public static List<CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
        return List.of(
                new CustomPacketPayload.TypeAndCodec<>(SelectSymbolAndDrawMarkS2CP.TYPE, SelectSymbolAndDrawMarkS2CP.STREAM_CODEC)
        );
    }
}