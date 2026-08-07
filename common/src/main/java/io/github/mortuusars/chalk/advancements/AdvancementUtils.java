package io.github.mortuusars.chalk.advancements;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class AdvancementUtils {
    public static boolean hasAdvancement(ServerPlayer player, ResourceLocation advancement) {
        @Nullable AdvancementHolder advancementHolder = player.serverLevel().getServer().getAdvancements().get(advancement);
        if (advancementHolder == null) {
            return false;
        }

        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancementHolder);
        return progress.isDone();
    }
}
