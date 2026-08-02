package io.github.mortuusars.chalk.network.fabric;

import io.github.mortuusars.chalk.network.packet.Packet;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class PacketsImpl {
    public static void sendToServer(Packet packet) {
        FabricC2SPackets.sendToServer(packet);
    }

    public static void sendToClient(Packet packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, packet);
    }
}
