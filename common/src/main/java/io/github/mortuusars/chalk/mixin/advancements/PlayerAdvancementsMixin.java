package io.github.mortuusars.chalk.mixin.advancements;

import io.github.mortuusars.chalk.event.CommonEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
    @Shadow
    private ServerPlayer player;

    // Using mixin instead of events to have the same code for neoforge and fabric
    @Inject(method = "method_53637", at = @At(value = "RETURN"))
    private void onDisplay(AdvancementHolder advancementHolder, DisplayInfo displayInfo, CallbackInfo ci) {
        CommonEvents.onAdvancementAward(player, advancementHolder);
    }
}
