package io.github.mortuusars.chalk.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.item.ChalkBoxItem;
import io.github.mortuusars.chalk.menu.ChalkBoxMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class ChalkBoxScreen extends AbstractContainerScreen<ChalkBoxMenu> {
    public static final ResourceLocation TEXTURE = Chalk.resource("textures/gui/container/chalk_box.png");
    private static final int GLOWING_BAR_WIDTH = 72;
    private final int maxGlowingUses;
    private final Player player;

    public ChalkBoxScreen(ChalkBoxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        maxGlowingUses = Config.Common.CHALK_BOX_GLOWING_AMOUNT_PER_ITEM.get();
        this.minecraft = Minecraft.getInstance();
        this.player = Minecraft.getInstance().player;
    }

    @Override
    protected void init() {
        this.imageWidth = 176;
        this.imageHeight = 180;
        this.inventoryLabelY = this.imageHeight - 94;
        super.init();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        for (Slot slot : getMenu().slots) {
            if (!slot.mayPickup(player) && !slot.isActive()) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                graphics.blit(RenderType::guiTextured, TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, 176f, 36f, 20, 20, 256, 256);
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
        graphics.blit(RenderType::guiTextured, TEXTURE, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight, 256, 256);

        renderChalkSlots(graphics, 52, getMenu().isGlowingEnabled() ? 17 : 32);

        if (getMenu().isGlowingEnabled()) {
            // Bar + Slot
            graphics.blit(RenderType::guiTextured, TEXTURE, getGuiLeft() + 52, getGuiTop() + 57, 0, 217, 72, 28, 256, 256);

            Slot slot = getMenu().slots.get(ChalkBoxItem.GLOWINGS_SLOT_INDEX);
            if (slot.getItem().isEmpty()) {
                graphics.blit(RenderType::guiTextured, TEXTURE, getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 176, 18, 18, 18, 256, 256);
            }

            // Fill
            int barSize = (int) Math.ceil((Math.min(getMenu().getGlowAmount(), maxGlowingUses) / (float) maxGlowingUses) * GLOWING_BAR_WIDTH);
            int glowingBarFillLevel = Math.min(GLOWING_BAR_WIDTH, barSize);
            graphics.blit(RenderType::guiTextured, TEXTURE, getGuiLeft() + 52, getGuiTop() + 57, 72, 217, glowingBarFillLevel, 5, 256, 256);
        }
    }

    protected void renderChalkSlots(GuiGraphics graphics, int x, int y) {
        graphics.blit(RenderType::guiTextured, TEXTURE, getGuiLeft() + x, getGuiTop() + y, 0, 180, 72, 36, 256, 256);

        for (int slotIndex = 0; slotIndex < ChalkBoxItem.CHALK_SLOTS; slotIndex++) {
            Slot slot = getMenu().slots.get(slotIndex);
            if (slot.getItem().isEmpty()) {
                graphics.blit(RenderType::guiTextured, TEXTURE, getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 176, 0, 18, 18, 256, 256);
            }
        }
    }
}
