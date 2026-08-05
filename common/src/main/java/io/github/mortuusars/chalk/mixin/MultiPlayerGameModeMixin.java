package io.github.mortuusars.chalk.mixin;

import io.github.mortuusars.chalk.client.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("NameDoesntMatchTargetClass")
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Shadow
    private GameType localPlayerMode;

    @Shadow
    private int destroyDelay;

    /**
     * Block#attack is not called when player is in creative, so we need this to break only one mark at a time.
     */
    @Inject(method = "startDestroyBlock", at = @At(value = "HEAD"), cancellable = true)
    private void onStartDestroyBlock(BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().player != null
              && Minecraft.getInstance().player.isCreative()
              && ClientHelper.handleCreativeStartDestroyBlock(pos, face, localPlayerMode)) {
            destroyDelay = 5;
            cir.setReturnValue(true);
        }
    }
}
