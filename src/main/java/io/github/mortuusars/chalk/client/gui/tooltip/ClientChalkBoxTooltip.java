package io.github.mortuusars.chalk.client.gui.tooltip;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.item.ChalkBoxItem;
import io.github.mortuusars.chalk.item.component.ChalkBoxContents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ClientChalkBoxTooltip implements ClientTooltipComponent {
    private static final ResourceLocation TEXTURE = Chalk.resource("textures/gui/container/chalk_box_tooltip.png");
    private static final ResourceLocation SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_back");
    private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_front");

    private static final int ROWS = 4;
    private static final int COLUMNS = 2;

    private final ChalkBoxContents contents;

    public ClientChalkBoxTooltip(ChalkBoxContents contents) {
        this.contents = contents;
    }

    @Override
    public int getHeight(@NotNull Font font) {
        return this.backgroundHeight() + 2;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return backgroundWidth();
    }

    private int backgroundWidth() {
        return 76;
    }

    private int backgroundHeight() {
        boolean showGlowingStuff = Config.Common.CHALK_BOX_GLOWING_ENABLED.get()
                && (contents.glowAmount() > 0 || !contents.items().get(ChalkBoxItem.GLOWINGS_SLOT_INDEX).isEmpty());
        return showGlowingStuff ? 69 : 42;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, int width, int height, GuiGraphics guiGraphics) {
        guiGraphics.blit(RenderType::guiTextured, TEXTURE, x, y, 0, 0, backgroundWidth(), 41, 128, 128);
        int selectedIndex = contents.getSelectedChalkIndex();
        int index = 0;
        for (int c = 0; c < COLUMNS; c++) {
            for (int r = 0; r < ROWS; r++) {
                int px = x + r * 18 + 2;
                int py = y + c * 18 + 2;

                if (index == selectedIndex) {
                    renderSlotHighlightBack(guiGraphics, px + 1, py + 1);
                }

                renderSlot(px, py, index, guiGraphics, font);

                if (index == selectedIndex) {
                    renderSlotHighlightFront(guiGraphics, px + 1, py + 1);
                }

                index++;
            }
        }

        boolean showGlowingStuff = Config.Common.CHALK_BOX_GLOWING_ENABLED.get()
                && (contents.glowAmount() > 0 || !contents.items().get(ChalkBoxItem.GLOWINGS_SLOT_INDEX).isEmpty());
        if (showGlowingStuff) {
            guiGraphics.blit(RenderType::guiTextured, TEXTURE, x, y + 38, 0, 41, backgroundWidth(), 30, 128, 128);

            renderSlot(x + 29, y + 47, index, guiGraphics, font);

            if (contents.glowAmount() > 0) {
                int maxWidth = 72;
                float fill = contents.glowAmount() / (float)Config.Common.CHALK_BOX_GLOWING_AMOUNT_PER_ITEM.get();
                int fillWidth = Math.min((int)Math.floor(maxWidth * fill), maxWidth);
                guiGraphics.blit(RenderType::guiTextured, TEXTURE, x + 2, y + 40, 0, 71, fillWidth, 5, 128, 128);
            }
        }
    }

    private void renderSlot(int x, int y, int index, GuiGraphics guiGraphics, Font font) {
        ItemStack itemstack = this.contents.items().get(index);
        guiGraphics.renderItem(itemstack, x + 1, y + 1, index);
        guiGraphics.renderItemDecorations(font, itemstack, x + 1, y + 1);
    }

    private void renderSlotHighlightBack(GuiGraphics pGuiGraphics, int x, int y) {
        pGuiGraphics.blitSprite(RenderType::guiTextured, SLOT_HIGHLIGHT_BACK_SPRITE, x - 4, y - 4, 24, 24);
    }

    private void renderSlotHighlightFront(GuiGraphics pGuiGraphics, int x, int y) {
        pGuiGraphics.blitSprite(RenderType::guiTexturedOverlay, SLOT_HIGHLIGHT_FRONT_SPRITE, x - 4, y - 4, 24, 24);
    }
}
