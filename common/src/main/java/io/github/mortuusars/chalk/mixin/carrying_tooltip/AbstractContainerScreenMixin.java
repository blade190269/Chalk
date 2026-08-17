package io.github.mortuusars.chalk.mixin.carrying_tooltip;

import io.github.mortuusars.chalk.world.item.ChalkBoxItem;
import io.github.mortuusars.chalk.world.item.ChalkItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {
    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Shadow public abstract T getMenu();

    @Shadow @Nullable
    public Slot hoveredSlot;

    @Shadow protected abstract List<Component> getTooltipFromContainerItem(ItemStack stack);

    /**
     * Because tooltip is not rendered when carrying an item, we render it manually when carrying a chalk over a chalk box.<br>
     * This helps to choose/see the item we're changing.
     */
    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void onRenderTooltip(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
        if (hoveredSlot != null
              && hoveredSlot.getItem().getItem() instanceof ChalkBoxItem
            && getMenu().getCarried().getItem() instanceof ChalkItem) {
            guiGraphics.renderTooltip(font, getTooltipFromContainerItem(hoveredSlot.getItem()), hoveredSlot.getItem().getTooltipImage(), x, y);
            ci.cancel();
        }
    }
}
