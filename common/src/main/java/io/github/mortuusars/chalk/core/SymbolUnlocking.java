package io.github.mortuusars.chalk.core;

import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SymbolUnlocking {
    public static List<MarkSymbol> getUnlockedSymbols(ServerPlayer player) {
        List<MarkSymbol> unlocked = new ArrayList<>();

        for (MarkSymbol symbol : MarkSymbol.getSpecialSymbols()) {
            try {
                Pair<ModConfigSpec.BooleanValue, ModConfigSpec.ConfigValue<String>> symbolConfig = Config.Common.SYMBOL_CONFIG.get(symbol);

                if (!symbolConfig.getFirst().get())
                    continue;

                String advancementLocation = symbolConfig.getSecond().get();

                if (advancementLocation.isEmpty() || hasAdvancement(player, ResourceLocation.parse(advancementLocation)))
                    unlocked.add(symbol);
            }
            catch (Exception e) {
                Chalk.LOGGER.error("Cannot check if the symbol '{}' is unlocked: {}", symbol, e.toString());
            }
        }

        return unlocked;
    }

    private static boolean hasAdvancement(ServerPlayer player, ResourceLocation advancementID) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            Chalk.LOGGER.error("Cannot check advancements: server is null");
            return false;
        }

        @Nullable AdvancementHolder advancementHolder = server.getAdvancements().get(advancementID);
        if (advancementHolder == null) {
            return false;
        }

        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancementHolder);
        return progress.isDone();
    }
}
