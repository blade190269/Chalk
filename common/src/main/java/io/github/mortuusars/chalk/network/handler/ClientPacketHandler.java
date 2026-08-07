package io.github.mortuusars.chalk.network.handler;

import io.github.mortuusars.chalk.client.gui.SymbolSelectScreen;
import io.github.mortuusars.chalk.network.packet.clientbound.SelectSymbolAndDrawMarkS2CP;
import io.github.mortuusars.chalk.world.item.MarkDrawable;
import net.minecraft.client.Minecraft;

public class ClientPacketHandler {
    public static void handleSelectSymbolAndDrawMark(SelectSymbolAndDrawMarkS2CP packet) {
        if (Minecraft.getInstance().player == null
              || Minecraft.getInstance().level == null
              || !(Minecraft.getInstance().player.getItemInHand(packet.context().hand()).getItem() instanceof MarkDrawable)) {
            return;
        }

        SymbolSelectScreen symbolSelectScreen = new SymbolSelectScreen(packet.availableSymbols(), packet.context());
        Minecraft.getInstance().setScreen(symbolSelectScreen);
    }
}
