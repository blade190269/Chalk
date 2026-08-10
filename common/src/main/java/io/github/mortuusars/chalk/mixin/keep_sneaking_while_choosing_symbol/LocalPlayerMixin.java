package io.github.mortuusars.chalk.mixin.keep_sneaking_while_choosing_symbol;

import io.github.mortuusars.chalk.client.gui.screens.SymbolSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    /**
     * Purpose of this mixin is to keep player sneaking while they are choosing a symbol.
     * Removes jerking the view up/down, and overall makes it look smooth.
     */
    @Inject(method = "isShiftKeyDown", at = @At("HEAD"), cancellable = true)
    private static void isShiftKeyDown(CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().screen instanceof SymbolSelectScreen) {
            cir.setReturnValue(true);
        }
    }
}
