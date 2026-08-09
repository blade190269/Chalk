package io.github.mortuusars.chalk.network.handler;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.client.gui.SymbolSelectScreen;
import io.github.mortuusars.chalk.network.packet.clientbound.SelectSymbolAndDrawMarkS2CP;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.chalk.world.item.MarkDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientPacketHandler {
    public static void handleSelectSymbolAndDrawMark(SelectSymbolAndDrawMarkS2CP packet) {
        if (Minecraft.getInstance().player == null
              || Minecraft.getInstance().level == null
              || !(Minecraft.getInstance().player.getItemInHand(packet.context().hand()).getItem() instanceof MarkDrawable)) {
            return;
        }

        validateAndReportConfigSymbolGroups();

        SymbolSelectScreen symbolSelectScreen = new SymbolSelectScreen(packet.availableSymbols(), packet.context());
        Minecraft.getInstance().setScreen(symbolSelectScreen);
    }

    private static void validateAndReportConfigSymbolGroups() {
        if (Minecraft.getInstance().level == null) return;

        try {
            Map<String, List<Holder<MarkSymbol>>> groupsAndSymbols = MarkSymbol.getAllHolders(Minecraft.getInstance().level.registryAccess())
                  .collect(Collectors.groupingBy(symbol -> symbol.value().group()));

            List<? extends String> groupSorting = Config.Client.SYMBOL_SELECTION_GROUP_SORTING.get();

            for (String group : groupSorting) {
                if (!groupsAndSymbols.containsKey(group)) {
                    Chalk.LOGGER.warn("Group '{}', defined in {} config value, is not used by any symbol.",
                          group, Config.Client.SYMBOL_SELECTION_GROUP_SORTING.getPath());
                }
            }
        } catch (Exception e) {
            Chalk.LOGGER.debug("Failed to verify config symbol groups.");
        }
    }
}
