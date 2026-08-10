package io.github.mortuusars.chalk.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.client.gui.Sprites;
import io.github.mortuusars.chalk.world.inventory.ChalkBoxMenu;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class ChalkBoxScreen extends AbstractInHandContainerScreen<ChalkBoxMenu> {
    public static final ResourceLocation TEXTURE = Chalk.resource("textures/gui/container/chalk_box.png");
    public static final ResourceLocation CHALK_SLOT_PLACEHOLDER_SPRITE = Chalk.resource("chalk_box/chalk_slot_placeholder");
    public static final ResourceLocation GLOWING_SLOT_PLACEHOLDER_SPRITE = Chalk.resource("chalk_box/glowing_slot_placeholder");
    public static final ResourceLocation GLOW_BAR_SPRITE = Chalk.resource("chalk_box/glow_bar");
    public static final ResourceLocation SELECTED_CHALK_SLOT_OVERLAY_SPRITE = Chalk.resource("chalk_box/selected_chalk_slot_overlay");

    public static final int GLOWING_BAR_WIDTH = 72;
    protected final int maxGlowingUses;
    protected final Player player;

    public ChalkBoxScreen(ChalkBoxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE);
        this.maxGlowingUses = Config.Server.CHALK_BOX_GLOWING_AMOUNT_PER_ITEM.get();
        this.minecraft = Minecraft.getInstance();
        this.player = playerInventory.player;
    }

    @Override
    protected void init() {
        this.imageWidth = 176;
        this.imageHeight = 150;
        this.inventoryLabelY = this.imageHeight - 94;
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        for (Slot slot : getMenu().slots) {
            if (!slot.mayPickup(player) && !slot.isActive()) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                graphics.blit(TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, 176, 36, 20, 20);
                graphics.renderFakeItem(slot.getItem(), leftPos + slot.x, topPos + slot.y);
                graphics.fill(RenderType.guiGhostRecipeOverlay(), leftPos + slot.x - 1, topPos + slot.y - 1,
                        leftPos + slot.x + 15, topPos + slot.y + 15, 0x40FFFFFF);
                RenderSystem.disableBlend();
            }
        }

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (getMenu().isGlowingEnabled()) {
            renderGlowingBar(graphics, mouseX, mouseY, partialTick);
        }

        if (getMenu().isGlowingEnabled()) {
            // Bar + Slot
            graphics.blit(TEXTURE, leftPos + 52, topPos + 57, 0, 217, 72, 28);

            Slot slot = getMenu().slots.get(ChalkBoxContents.GLOWINGS_SLOT);
            if (slot.getItem().isEmpty()) {
                graphics.blit(TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, 176, 18, 18, 18);
            }

            // Fill
            int barSize = (int) Math.ceil((Math.min(getMenu().getGlowAmount(), maxGlowingUses) / (float) maxGlowingUses) * GLOWING_BAR_WIDTH);
            int glowingBarFillLevel = Math.min(GLOWING_BAR_WIDTH, barSize);
            graphics.blit(TEXTURE, leftPos + 52, topPos + 57, 72, 217, glowingBarFillLevel, 5);
        }
    }

    protected void renderGlowingBar(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {


    }

    @Override
    protected void renderSlotOverlays(GuiGraphics guiGraphics, Slot slot) {
        super.renderSlotOverlays(guiGraphics, slot);
        if (slot.index <= ChalkBoxContents.CHALK_SLOTS) {
            //TODO: selected slot
            guiGraphics.blitSprite(CHALK_SLOT_PLACEHOLDER_SPRITE, getLeftPos() + slot.x - 1, getTopPos() + slot.y - 1, 18, 18);
        }
        if (getMenu().isGlowingEnabled() && slot.index == ChalkBoxContents.GLOWINGS_SLOT) {
            guiGraphics.blitSprite(GLOWING_SLOT_PLACEHOLDER_SPRITE, getLeftPos() + slot.x - 1, getTopPos() + slot.y - 1, 18, 18);
        }
    }
}
