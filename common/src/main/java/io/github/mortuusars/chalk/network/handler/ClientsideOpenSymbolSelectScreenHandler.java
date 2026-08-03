package io.github.mortuusars.chalk.network.handler;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.client.gui.SymbolSelectScreen;
import io.github.mortuusars.chalk.core.OldMarkSymbol;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import org.slf4j.Logger;

import java.util.List;

public class ClientsideOpenSymbolSelectScreenHandler {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static void handle(List<OldMarkSymbol> unlockedSymbols) {
        MarkDrawingContext storedContext = MarkDrawingContext.getStoredContext();
        if (storedContext == null) {
            LOGGER.error("Cannot select a symbol: MarkDrawingContext wasn't stored on the client.");
            return;
        }

        if (Minecraft.getInstance().level != null) {
//        SymbolSelectScreen symbolSelectScreen = new SymbolSelectScreen(unlockedSymbols, storedContext);
            List<Holder<MarkSymbol>> symbols = Minecraft.getInstance().level.registryAccess()
                  .registryOrThrow(Chalk.Registries.MARK_SYMBOL).holders()
                  .map(ref -> (Holder<MarkSymbol>)ref).toList();
            SymbolSelectScreen symbolSelectScreen = new SymbolSelectScreen(symbols, storedContext);
            Minecraft.getInstance().setScreen(symbolSelectScreen);

            MarkDrawingContext.clearStoredContext();
        }
    }
}
