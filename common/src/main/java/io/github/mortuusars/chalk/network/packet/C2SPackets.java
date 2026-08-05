package io.github.mortuusars.chalk.network.packet;

import io.github.mortuusars.chalk.network.packet.serverbound.DestroyMarkC2SP;
import io.github.mortuusars.chalk.network.packet.serverbound.DrawMarkC2SP;
import io.github.mortuusars.chalk.network.packet.serverbound.OpenCreativeChalkBoxC2SP;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public class C2SPackets {
    public static List<CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
        return List.of(
                 new CustomPacketPayload.TypeAndCodec<>(DrawMarkC2SP.TYPE, DrawMarkC2SP.STREAM_CODEC),
                 new CustomPacketPayload.TypeAndCodec<>(DestroyMarkC2SP.TYPE, DestroyMarkC2SP.STREAM_CODEC),
                 new CustomPacketPayload.TypeAndCodec<>(OpenCreativeChalkBoxC2SP.TYPE, OpenCreativeChalkBoxC2SP.STREAM_CODEC)
        );
    }
}