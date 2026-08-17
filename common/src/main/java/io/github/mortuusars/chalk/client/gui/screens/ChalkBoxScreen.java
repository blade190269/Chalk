package io.github.mortuusars.chalk.client.gui.screens;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.inventory.ChalkBoxMenu;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChalkBoxScreen extends AbstractInHandContainerScreen<ChalkBoxMenu> {
    public static final ResourceLocation TEXTURE = Chalk.resource("textures/gui/container/chalk_box.png");
    public static final ResourceLocation CHALK_SLOT_PLACEHOLDER_SPRITE = Chalk.resource("chalk_box/chalk_slot_placeholder");
    public static final ResourceLocation GLOWING_SLOT_PLACEHOLDER_SPRITE = Chalk.resource("chalk_box/glowing_slot_placeholder");
    public static final ResourceLocation GLOW_BAR_SPRITE = Chalk.resource("chalk_box/glow_bar");
    public static final ResourceLocation SELECTED_CHALK_SLOT_OVERLAY_SPRITE = Chalk.resource("chalk_box/selected_chalk_slot_overlay");

    public static final int GLOW_BAR_HEIGHT = 18;

    protected final Player player;
    protected int selectedSlot;

    public ChalkBoxScreen(ChalkBoxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE);
        this.player = playerInventory.player;
    }

    @Override
    protected void init() {
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        selectedSlot = getMenu().getSelectedSlot();
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (getMenu().isGlowEnabled()) {
            graphics.blit(TEXTURE, leftPos + 133, topPos + 34, imageWidth, 0, 25, 18);
            renderGlowingBar(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (getMenu().isGlowEnabled()
              && x >= getLeftPos() + 151 && x <= getLeftPos() + 160
              && y >= getTopPos() + 34 && y <= getTopPos() + 51) {
            int glow = getMenu().getGlow();
            ChatFormatting glowAmountColor = glow > 0
                  ? ChatFormatting.GOLD
                  : ChatFormatting.GRAY;
            guiGraphics.renderTooltip(font, Component.translatable("gui.chalk.chalk_box.glow",
                  Component.literal(Integer.toString(glow)).withStyle(glowAmountColor)), x, y);
            return;
        }

        super.renderTooltip(guiGraphics, x, y);
    }

    @Override
    protected @NotNull List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> lines = super.getTooltipFromContainerItem(stack);
        if (hoveredSlot != null && hoveredSlot.index < ChalkBoxContents.CHALK_SLOTS && hoveredSlot.getItem().equals(stack)) {
            lines = new ArrayList<>(lines);
            lines.add(Component.translatable("gui.chalk.chalk_box.alt_select"));
        }

        return lines;
    }

    protected void renderGlowingBar(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int currentGlow = getMenu().getGlow();
        int maxGlow = getMenu().getMaxGlow();
        int barSize = currentGlow == 0 ? 0
              : currentGlow == maxGlow ? 18
              : 2 + Math.round((currentGlow - 1) * 14f / (maxGlow - 2));
        int glowingBarFillLevel = Math.min(GLOW_BAR_HEIGHT, barSize);
        graphics.blitSprite(GLOW_BAR_SPRITE, 5, 18, 0, 18 - glowingBarFillLevel,
              getLeftPos() + 153, getTopPos() + 34 + 18 - glowingBarFillLevel, 5, glowingBarFillLevel);
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        // renderSlotOverlays renders on top of the durability bar, so we render it before the item
        if (slot.index == selectedSlot) {
            guiGraphics.blitSprite(SELECTED_CHALK_SLOT_OVERLAY_SPRITE, slot.x - 2, slot.y - 2, 20, 20);
        }
        super.renderSlot(guiGraphics, slot);
    }

    @Override
    protected void renderSlotOverlays(GuiGraphics guiGraphics, Slot slot) {
        super.renderSlotOverlays(guiGraphics, slot);
        if (slot.index < ChalkBoxContents.CHALK_SLOTS) {
            if (!slot.hasItem()) {
                guiGraphics.blitSprite(CHALK_SLOT_PLACEHOLDER_SPRITE, slot.x - 1, slot.y - 1, 18, 18);
            }
        }
        if (getMenu().isGlowEnabled() && slot.index == ChalkBoxContents.GLOWINGS_SLOT && !slot.hasItem()) {
            guiGraphics.blitSprite(GLOWING_SLOT_PLACEHOLDER_SPRITE, slot.x - 1, slot.y - 1, 18, 18);
        }
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (Screen.hasAltDown() && slotId >= 0 && slotId < ChalkBoxContents.CHALK_SLOTS && slot.hasItem()) {
            getMenu().setSelectedSlot(slotId);
            slotId += 100;
            assert minecraft != null;
            assert minecraft.gameMode != null;
            minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, slotId);
            return;
        }
        super.slotClicked(slot, slotId, mouseButton, type);
    }
}
