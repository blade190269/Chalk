package io.github.mortuusars.chalk.client.gui;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.network.Packets;
import io.github.mortuusars.chalk.network.packet.serverbound.DrawMarkC2SP;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.chalk.world.chalk.MarkDrawingContext;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.chalk.world.item.MarkDrawable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class SymbolSelectScreen extends Screen {
    protected int SYMBOL_SIZE = 32;
    protected int SYMBOL_SPACING = 8;
    protected int SYMBOL_BORDER_THICKNESS = SYMBOL_SIZE / 16;
    protected int GROUP_LABEL_BOTTOM_PADDING = 4;
    protected int DEFAULT_BORDER_COLOR = 0xFF252525;

    protected final Player player;
    protected final Level level;
    protected final long openTimestamp;
    protected final long openTimestampMs = System.currentTimeMillis();

    protected final List<Holder<MarkSymbol>> symbols;
    protected final MarkDrawingContext context;

    protected final int color;
    protected final float r;
    protected final float g;
    protected final float b;
    protected final int hoverBorderColor;
    protected final Direction markFacing;
    protected final BlockState surfaceState;

    protected final List<Mark> marks;
    protected final MarkDrawable drawable;
    protected final ItemStack itemStack;

    protected float openAnimation;
    protected int centerX;
    protected int centerY;

    protected LinkedHashMap<Component, List<List<Mark>>> groupsAndRows = new LinkedHashMap<>();
    protected int contentHeight;
    protected int contentY;

    protected double changePerScroll;
    protected double maxScroll = 999999.0;
    protected double scroll = 0;
    protected double currentScroll = 0;

    @Nullable
    protected Mark hoveredMark;
    protected boolean mouseWasReleased;

    public SymbolSelectScreen(List<Holder<MarkSymbol>> symbols, MarkDrawingContext context) {
        super(Component.empty());
        this.symbols = symbols;
        this.context = context;

        this.minecraft = Minecraft.getInstance();
        this.player = minecraft.player;
        Preconditions.checkArgument(player != null, "Player cannot be null.");
        this.level = player.level();
        this.openTimestamp = level.getGameTime();

        itemStack = player.getItemInHand(context.hand());
        // Screen shouldn't be opened with other items
        drawable = ((MarkDrawable) itemStack.getItem());

        color = drawable.getMarkDrawingColor(itemStack);
        r = (float) (this.color >> 16 & 255) / 255.0F;
        g = (float) (this.color >> 8 & 255) / 255.0F;
        b = (float) (this.color & 255) / 255.0F;
        hoverBorderColor = FastColor.ARGB32.lerp(0.2f, FastColor.ARGB32.opaque(this.color), 0xFFFFFFFF);

        marks = symbols.stream()
              .map(symbol -> drawable.createMark(player, context, itemStack, symbol))
              .toList();
        markFacing = context.markFacing();

        BlockPos surfacePos = this.context.markPos().relative(markFacing.getOpposite());
        surfaceState = minecraft.level != null ? minecraft.level.getBlockState(surfacePos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (!drawable.canDrawMark(player, context)) {
            this.close();
        }
    }

    @Override
    protected void init() {
        centerX = width / 2;
        centerY = height / 2;
        createGroupsAndRows();

        contentHeight = 0;

        for (Map.Entry<Component, List<List<Mark>>> group : groupsAndRows.entrySet()) {
            contentHeight += font.lineHeight + GROUP_LABEL_BOTTOM_PADDING;
            contentHeight += (SYMBOL_SIZE + SYMBOL_SPACING) * group.getValue().size();
        }

        if (contentHeight < height * 0.75f) {
            maxScroll = 0;
            contentY = height / 2 - contentHeight / 2;
        } else {
            maxScroll = Math.max(0, height * 0.3f + contentHeight - height * 0.75f);
            contentY = (int) (height * 0.25f);
        }

        changePerScroll = height / 8.0;
    }

    private void createGroupsAndRows() {
        groupsAndRows.clear();

        Map<String, List<Holder<MarkSymbol>>> groupsAndSymbols = symbols.stream()
              .collect(Collectors.groupingBy(symbol -> symbol.value().group()));

        List<? extends String> groupSorting = Config.Client.SYMBOL_SELECTION_GROUP_SORTING.get();

        List<String> groups = new ArrayList<>();
        groups.addAll(groupSorting.stream()
              .filter(group -> {
                  if (!groupsAndSymbols.containsKey(group)) {
                      Chalk.LOGGER.warn("Group '{}', defined in {} config value, is not used by any symbol and will be skipped.",
                            group, Config.Client.SYMBOL_SELECTION_GROUP_SORTING.getPath());
                      return false;
                  }
                  return true;
              })
              .toList());

        groups.addAll(groupsAndSymbols.keySet().stream()
              .filter(group -> !groups.contains(group))
              .sorted()
              .toList());

        for (String group : groups) {
            groupsAndRows.put(Component.translatable("mark_symbol_group.chalk." + group), createRows(groupsAndSymbols.get(group)));
        }
    }

    protected List<List<Mark>> createRows(List<Holder<MarkSymbol>> symbols) {
        int maxSymbolsInRow = Math.max(1, (int) ((width * 0.7f) / (SYMBOL_SIZE + SYMBOL_SPACING)));

        List<Mark> marks = symbols.stream()
              .sorted(Comparator.comparingInt(symbol -> symbol.value().groupPriority()))
              .map(symbol -> drawable.createMark(player, context, itemStack, symbol))
              .toList();

//        maxSymbolsInRow = 3;

        return Lists.partition(marks, maxSymbolsInRow);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Keep sneaking while choosing a mark to not jerk player's camera:
        //TODO: find a better way to do that. Check keyboard handler or something
        player.setPose(Pose.CROUCHING);

        openAnimation = Mth.clamp((System.currentTimeMillis() - openTimestampMs) / 400f, 0f, 1f);
        openAnimation = 1f - openAnimation;
        openAnimation *= openAnimation * openAnimation;
        openAnimation = 1f - openAnimation;

        handleScreenEdgeScrolling(mouseY, partialTicks);

        super.render(graphics, mouseX, mouseY, partialTicks);

        try {
            renderMarks(graphics, mouseX, mouseY, partialTicks);
        } catch (Exception e) {
            Chalk.LOGGER.error(e);
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 500);
        graphics.fillGradient(0, 0, width, (int) (height * 0.08f),
              FastColor.ARGB32.lerp(openAnimation, 0x007F7F7F, 0x44000000), 0x007F7F7F);
        graphics.fillGradient(0, (int) (height * 0.92f), width, height, 0x007F7F7F, FastColor.ARGB32.lerp(openAnimation, 0x00000000, 0x44000000));
        graphics.pose().popPose();
    }

    protected void handleScreenEdgeScrolling(int mouseY, float partialTicks) {
        float screenScrollArea = Math.max(20, height * 0.1f);

        if (mouseY < screenScrollArea) {
            float strength = Mth.clamp((screenScrollArea - mouseY) / screenScrollArea, 0, 1);
            scroll(-height * (0.15f * strength * partialTicks));
        }

        if (mouseY > height - screenScrollArea) {
            float strength = Mth.clamp((mouseY - (height - screenScrollArea)) / screenScrollArea, 0, 1);
            scroll(height * (0.15f * strength * partialTicks));
        }
    }

    protected void renderMarks(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        hoveredMark = null;

        // Animates scroll smoothly
        if (Math.abs(currentScroll - scroll) > 0.001f) {
            currentScroll = Mth.lerp(Math.min(1, partialTicks), currentScroll, scroll);
        } else {
            currentScroll = scroll;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0, -Mth.frac(currentScroll), 0);

        int y = contentY - (int)currentScroll;

        for (Map.Entry<Component, List<List<Mark>>> group : groupsAndRows.entrySet()) {
            int labelX = width / 2 - font.width(group.getKey()) / 2;
            int textOutlineColor = FastColor.ARGB32.lerp(openAnimation, 0x22000000, DEFAULT_BORDER_COLOR);
            int textColor = FastColor.ARGB32.lerp(openAnimation, 0x22FFFFFF, 0xFFFFFFFF);
            graphics.drawString(font, group.getKey(), labelX - 1, y, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX - 1, y + 1, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX - 1, y - 1, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX + 1, y, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX + 1, y - 1, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX + 1, y + 1, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX, y + 1, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX, y - 1, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX, y, textColor, false);

            y += font.lineHeight + GROUP_LABEL_BOTTOM_PADDING;

            List<List<Mark>> rows = group.getValue();

            for (List<Mark> marks : rows) {
                int count = marks.size();
                int rowWidth = (SYMBOL_SIZE * count) + (SYMBOL_SPACING * (count - 1));
                int rowX = (width / 2) - rowWidth / 2;

                for (int markIndex = 0; markIndex < marks.size(); markIndex++) {
                    Mark mark = marks.get(markIndex);
                    int markX = rowX + markIndex * (SYMBOL_SIZE + SYMBOL_SPACING);

                    boolean isHovering = (mouseX >= markX - SYMBOL_BORDER_THICKNESS && mouseX <= markX + SYMBOL_SIZE + SYMBOL_BORDER_THICKNESS)
                          && (mouseY >= y - SYMBOL_BORDER_THICKNESS - Mth.frac(scroll) && mouseY <= y + SYMBOL_SIZE + SYMBOL_BORDER_THICKNESS - Mth.frac(scroll));

                    if (isHovering) {
                        hoveredMark = mark;
                    }

                    renderMarkButton(graphics, mouseX, mouseY, mark, markX, y, isHovering);
                }

                y += SYMBOL_SIZE + SYMBOL_SPACING;
            }
        }

        graphics.pose().popPose();

        if (hoveredMark != null) {
            renderMarkTooltip(graphics, mouseX, mouseY, partialTicks, hoveredMark);
        }
    }

    public void renderMarkTooltip(GuiGraphics graphics, int x, int y, float partialTicks, Mark mark) {
        mark.symbol().unwrapKey()
              .map(ResourceKey::location)
              .ifPresent(location -> {
                  @SuppressWarnings("deprecation")
                  String namespace = WordUtils.capitalizeFully(location.getNamespace().replace("_", " "));
                  graphics.renderTooltip(font, List.of(
                        Component.translatable(location.toLanguageKey("mark_symbol")).getVisualOrderText(),
                        Component.literal(namespace).withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(true)).getVisualOrderText()), x, y);
              });
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Background
        graphics.fillGradient(0, 0, width, height,
              FastColor.ARGB32.lerp(openAnimation, 0x00000000, 0x40000000),
              FastColor.ARGB32.lerp(openAnimation, 0x00000000, 0x40000000));
    }

    private void renderMarkButton(GuiGraphics graphics, int mouseX, int mouseY, Mark mark, int x, int y, boolean isHovering) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        int borderColor = isHovering ? hoverBorderColor : DEFAULT_BORDER_COLOR;
        borderColor = FastColor.ARGB32.lerp(openAnimation, FastColor.ARGB32.color(0x00, borderColor), borderColor);
        graphics.fill(x - SYMBOL_BORDER_THICKNESS, y - SYMBOL_BORDER_THICKNESS,
              x + SYMBOL_SIZE + SYMBOL_BORDER_THICKNESS, y + SYMBOL_SIZE + SYMBOL_BORDER_THICKNESS, borderColor);

        renderBlockSurface(graphics, mouseX, mouseY, x, y);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(r, g, b, openAnimation);
        RenderSystem.enableBlend();

        poseStack.pushPose();

        poseStack.translate(x + SYMBOL_SIZE / 2f, y + SYMBOL_SIZE / 2f, 0);

        int rotation = mark.symbol().value().orientationBehavior() == MarkSymbol.OrientationBehavior.FULL || mark.symbol().value().orientationBehavior() == MarkSymbol.OrientationBehavior.CARDINAL
              ? mark.orientation().getRotation()
              : 0;

        //TODO: fix
        if (mark.symbol().value().orientationBehavior() == MarkSymbol.OrientationBehavior.FULL) {
            rotation += player.getDirection().get2DDataValue() * 90 + 180;
            rotation %= 360;
        }

        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation + mark.symbol().value().rotationOffset()));
        poseStack.translate(-x - SYMBOL_SIZE / 2f, -y - SYMBOL_SIZE / 2f, 100);
        graphics.blit(mark.symbol().value().texture().withPrefix("textures/").withSuffix(".png"),
              x, y, SYMBOL_SIZE, SYMBOL_SIZE, 0, 0, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        poseStack.popPose();

        // Shadow overlay
        poseStack.pushPose();
        poseStack.translate(0, 0, 100);
        graphics.fillGradient(x, y, x + SYMBOL_SIZE, y + SYMBOL_SIZE,
              FastColor.ARGB32.lerp(openAnimation, 0x33000000, 0x00AAAAAA),
              FastColor.ARGB32.lerp(openAnimation, 0x33000000, 0x2F000000));
        poseStack.popPose();

        poseStack.popPose();
    }

    @SuppressWarnings("DataFlowIssue")
    private void renderBlockSurface(GuiGraphics graphics, int mouseX, int mouseY, int x, int y) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.applyModelViewMatrix();
        Lighting.setupForFlatItems();

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);

        int xRot = 0;
        int yRot = 0;

        if (markFacing == Direction.UP)
            xRot = -90;
        else if (markFacing == Direction.DOWN)
            xRot = 90;

        if (markFacing == Direction.EAST)
            yRot = 270;
        else if (markFacing == Direction.NORTH)
            yRot = 180;
        else if (markFacing == Direction.WEST)
            yRot = 90;

        graphics.pose().translate(SYMBOL_SIZE / 2f, SYMBOL_SIZE / 2f, SYMBOL_SIZE / 2f);
        graphics.pose().mulPose(Axis.XP.rotationDegrees(xRot - 0.1f));
        graphics.pose().mulPose(Axis.YP.rotationDegrees(yRot - 0.1f));
        graphics.pose().translate(-SYMBOL_SIZE / 2f, -SYMBOL_SIZE / 2f, -SYMBOL_SIZE / 2f);

        graphics.pose().translate(0, SYMBOL_SIZE, 0);
        graphics.pose().scale(1.0F, -1.0F, 1.0F);
        graphics.pose().scale(SYMBOL_SIZE, SYMBOL_SIZE, SYMBOL_SIZE);

        MultiBufferSource.BufferSource bufferSource = graphics.bufferSource();

        minecraft.getBlockRenderer().renderSingleBlock(surfaceState, graphics.pose(), bufferSource,
              LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        RenderSystem.applyModelViewMatrix();

        graphics.pose().popPose();
    }

    // -- Input

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            this.close();
            return true;
        }

        int key = keyCode - InputConstants.KEY_0; // Offset
        if (key >= 1 && key <= Math.min(symbols.size(), 9)) {
            tryDrawSymbol(symbols.get(key - 1));
            this.close();
            return true;
        }

        if (keyCode == InputConstants.KEY_ADD || keyCode == InputConstants.KEY_EQUALS) {
            scroll(changePerScroll);
            return true;
        }

        if (keyCode == 333 /*KEY_SUBTRACT*/ || keyCode == InputConstants.KEY_MINUS) {
            scroll(-changePerScroll);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!mouseWasReleased && level.getGameTime() - openTimestamp < 4) {
            mouseWasReleased = true;
            return true;
        }

        if (button == 0 || !mouseWasReleased) {
            if (hoveredMark != null) {
                tryDrawSymbol(hoveredMark.symbol());
            }
        }

        this.close();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            scroll(-(scrollY * changePerScroll));
            return true;
        }
        return false;
    }

    protected void tryDrawSymbol(Holder<MarkSymbol> symbol) {
        Packets.sendToServer(new DrawMarkC2SP(symbol, context));
        player.swing(context.hand());
    }

    public void scroll(double change) {
        scrollTo(scroll + change);
    }

    public void scrollTo(double position) {
        scroll = Mth.clamp(position, 0, maxScroll);
    }

    // --

    public void close() {
        this.onClose();
    }

    @Override
    public void onClose() {
        player.setPose(null);
        super.onClose();
    }
}
