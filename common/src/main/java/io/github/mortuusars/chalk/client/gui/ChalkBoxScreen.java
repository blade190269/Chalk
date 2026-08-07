package io.github.mortuusars.chalk.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.world.inventory.ChalkBoxMenu;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
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
        maxGlowingUses = Config.Server.CHALK_BOX_GLOWING_AMOUNT_PER_ITEM.get();
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

        renderChalkSlots(graphics, 52, getMenu().isGlowingEnabled() ? 17 : 32);

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

    protected void renderChalkSlots(GuiGraphics graphics, int x, int y) {
        graphics.blit(TEXTURE, leftPos + x, topPos + y, 0, 180, 72, 36);

        for (int slotIndex = 0; slotIndex < ChalkBoxContents.CHALK_SLOTS; slotIndex++) {
            Slot slot = getMenu().slots.get(slotIndex);
            if (slot.getItem().isEmpty()) {
                graphics.blit(TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, 176, 0, 18, 18);
            }
        }
    }
}
