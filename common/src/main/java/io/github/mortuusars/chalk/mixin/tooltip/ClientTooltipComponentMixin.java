package io.github.mortuusars.chalk.mixin.tooltip;

import io.github.mortuusars.chalk.client.gui.tooltip.ClientChalkBoxTooltip;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {
    @Inject(method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
          at = @At("HEAD"),
          cancellable = true)
    private static void onCreate(TooltipComponent visualTooltipComponent, CallbackInfoReturnable<ClientTooltipComponent> cir) {
        if (visualTooltipComponent instanceof ChalkBoxContents contents) {
            cir.setReturnValue(new ClientChalkBoxTooltip(contents));
        }
    }
}