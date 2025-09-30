package io.github.mortuusars.chalk.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.block.ChalkMarkBlock;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.core.IChalkDrawingTool;
import io.github.mortuusars.chalk.core.Mark;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.item.component.ChalkBoxContents;
import io.github.mortuusars.chalk.menu.ChalkBoxMenu;
import io.github.mortuusars.chalk.network.Packets;
import io.github.mortuusars.chalk.network.packet.OpenCreativeChalkBoxC2SP;
import io.github.mortuusars.chalk.data.ChalkColors;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ChalkBoxItem extends Item implements IChalkDrawingTool {
    public static final int SLOTS = 9;
    public static final int CHALK_SLOTS = 8;
    public static final int GLOWINGS_SLOT_INDEX = 8;

    public static final ResourceLocation SELECTED_PROPERTY = Chalk.resource("selected");

    public ChalkBoxItem(Properties properties) {
        super(properties.setNoCombineRepair());
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        return Config.Client.CHALK_BOX_TOOLTIP_CONTENTS.get() && !getContents(stack).isEmpty()
                ? Optional.of(getContents(stack)) : Optional.empty();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        if (!Config.Client.CHALK_BOX_TOOLTIP_DETAILS.get()) {
            return;
        }

        if (!Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("gui.chalk.tooltip.hold_for_details"));
        } else {
            if (Minecraft.getInstance().player != null
                    && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen
                    && screen.getSlotUnderMouse() != null
                    && screen.getSlotUnderMouse().container instanceof Inventory) {
                tooltipComponents.add(Component.translatable("item.chalk.chalk_box.tooltip.open"));
            }

            tooltipComponents.add(Component.translatable("item.chalk.chalk_box.tooltip.insert"));
            tooltipComponents.add(Component.translatable("item.chalk.chalk_box.tooltip.change_selected"));
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack otherStack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess slotAccess) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }

        // Open
        if (otherStack.isEmpty() && slot.container instanceof Inventory) {
            if (player.isCreative()) {
                Packets.sendToServer(new OpenCreativeChalkBoxC2SP(slot.getContainerSlot()));
            } else if (player instanceof ServerPlayer serverPlayer) {
                openGUI(serverPlayer, stack);
            }
            return true;
        }

        // Insert chalk into box:
        if (otherStack.getItem() instanceof ChalkItem) {
            for (int i = 0; i < CHALK_SLOTS; i++) {
                if (getItemInSlot(stack, i).isEmpty()) {
                    setItemInSlot(stack, i, otherStack.copy());
                    player.playSound(Chalk.SoundEvents.CHALK_BOX_CHANGE.get(),
                            0.9f, 0.9f + player.level().random.nextFloat() * 0.2f);
                    otherStack.setCount(0);
                    return true;
                }
            }
        }
        else if (Config.Common.CHALK_BOX_GLOWING_ENABLED.get()
                && otherStack.is(Chalk.Tags.Items.GLOWINGS)) {
            ItemStack existingItem = getItemInSlot(stack, GLOWINGS_SLOT_INDEX);
            int glowAmountBefore = getGlowAmount(stack);

            if (existingItem.isEmpty()) {
                setItemInSlot(stack, GLOWINGS_SLOT_INDEX, otherStack.copy());
                otherStack.setCount(0);
            } else if (existingItem.getCount() >= existingItem.getMaxStackSize() || !ItemStack.isSameItemSameComponents(existingItem, otherStack)) {
                return true;
            } else {
                int insertedAmount = Math.min(otherStack.getCount(), existingItem.getMaxStackSize() - existingItem.getCount());
                if (insertedAmount <= 0) {
                    return true;
                }

                existingItem.setCount(existingItem.getCount() + insertedAmount);
                otherStack.split(insertedAmount);
                setItemInSlot(stack, GLOWINGS_SLOT_INDEX, existingItem);
            }

            player.playSound(Chalk.SoundEvents.CHALK_BOX_CHANGE.get(),
                    0.9f, 0.9f + player.level().random.nextFloat() * 0.2f);

            if (glowAmountBefore < getGlowAmount(stack)) {
                player.playSound(Chalk.SoundEvents.GLOW_APPLIED.get(), 1f, 1f);
                player.playSound(Chalk.SoundEvents.GLOWING.get(), 1f, 1f);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) {
        return false;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        ItemStack chalkBoxStack = context.getItemInHand();
        if (!chalkBoxStack.is(this))
            return InteractionResult.FAIL;

        Player player = context.getPlayer();
        if (player == null)
            return InteractionResult.FAIL;

        if (context.getHand() == InteractionHand.OFF_HAND && (player.getMainHandItem().getItem() instanceof ChalkItem || player.getMainHandItem().is(this)))
            return InteractionResult.FAIL; // Skip drawing from offhand if chalks in both hands.

        ItemStack selectedChalk = getSelectedChalk(chalkBoxStack);

        if (selectedChalk.isEmpty()) {
            if (player instanceof ServerPlayer serverPlayer) {
                openGUI(serverPlayer, chalkBoxStack);
            }
            return InteractionResult.SUCCESS;
        }

        MarkDrawingContext drawingContext = createDrawingContext(context);

        if (!drawingContext.canDraw() || !(selectedChalk.getItem() instanceof IChalkDrawingTool chalkDrawingTool))
            return InteractionResult.FAIL;

        if (player.isSecondaryUseActive()) {
            drawingContext.openSymbolSelectionScreen();
            return InteractionResult.CONSUME;
        }

        Mark mark = drawingContext.createRegularMark(chalkDrawingTool.getMarkColorValue(selectedChalk), isGlowing(chalkBoxStack));
        if (drawMark(drawingContext, mark)) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack usedStack = player.getItemInHand(usedHand);

        if (!usedStack.is(this)) {
            return InteractionResult.PASS;
        }

        if (player.isSecondaryUseActive()) {
            if (rotateSelectedChalk(usedStack)) {
                level.playSound(player, player.position().x, player.position().y, player.position().z, Chalk.SoundEvents.CHALK_BOX_CHANGE.get(), SoundSource.PLAYERS,
                        0.9f, 0.9f + level.random.nextFloat() * 0.2f);
            } else {
                return InteractionResult.FAIL;
            }
        } else if (player instanceof ServerPlayer serverPlayer) {
            openGUI(serverPlayer, usedStack);
        }

        return InteractionResult.SUCCESS;
    }

    public void openGUI(ServerPlayer player, ItemStack chalkBoxStack) {
        if (!(chalkBoxStack.getItem() instanceof ChalkBoxItem)) {
            Chalk.LOGGER.error("Cannot open Chalk Box menu: {} is not a ChalkBoxItem.", chalkBoxStack);
            return;
        }

        int chalkBoxSlotIndex = player.getInventory().findSlotMatchingItem(chalkBoxStack);
        if (chalkBoxSlotIndex < 0) {
            Chalk.LOGGER.error("Cannot open Chalk Box menu: {} is not found in player's inventory.", chalkBoxStack);
            return;
        }

        Component title = chalkBoxStack.has(DataComponents.CUSTOM_NAME)
                ? chalkBoxStack.getHoverName()
                : Component.translatable("container.chalk.chalk_box");

        SimpleMenuProvider menuProvider = new SimpleMenuProvider((containerID, playerInventory, playerEntity) ->
                new ChalkBoxMenu(containerID, playerInventory, chalkBoxSlotIndex), title);

        player.openMenu(menuProvider, buffer -> buffer.writeVarInt(chalkBoxSlotIndex));

        player.level().playSound(null, player.position().x, player.position().y, player.position().z,
                Chalk.SoundEvents.CHALK_BOX_OPEN.get(), SoundSource.PLAYERS,
                0.9f, 0.9f + player.level().random.nextFloat() * 0.2f);
    }

    @Override
    public Mark getMark(ItemStack chalkBoxStack, MarkDrawingContext drawingContext, MarkSymbol symbol) {
        ItemStack selectedChalk = getSelectedChalk(chalkBoxStack);

        DyeColor color = selectedChalk.getItem() instanceof IChalkDrawingTool chalkItem
                ? chalkItem.getMarkColor(selectedChalk).orElse(DyeColor.WHITE)
                : DyeColor.WHITE;

        return drawingContext.createMark(ChalkColors.fromDyeColor(color), symbol, isGlowing(chalkBoxStack));
    }

    @Override
    public void onMarkDrawn(Player player, InteractionHand hand, BlockPos markBlockPos, BlockState markBlockState) {
        if (player.isCreative())
            return;

        ItemStack chalkBoxStack = player.getItemInHand(hand);

        int selectedChalkIndex = getContents(chalkBoxStack).getSelectedChalkIndex();

        Preconditions.checkState(selectedChalkIndex >= 0, "Chalk Box has no selected drawing tool. {}", chalkBoxStack);

        ItemStack selectedChalk = getItemInSlot(chalkBoxStack, selectedChalkIndex);

        selectedChalk.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

        setItemInSlot(chalkBoxStack, selectedChalkIndex, selectedChalk.isEmpty() ? ItemStack.EMPTY : selectedChalk);

        if (markBlockState.getValue(ChalkMarkBlock.GLOWING))
            consumeGlow(chalkBoxStack);
    }

    public @NotNull ChalkBoxContents getContents(ItemStack stack) {
        @Nullable ChalkBoxContents contents = stack.get(Chalk.DataComponents.CHALK_BOX_CONTENTS);
        return contents != null ? contents : ChalkBoxContents.EMPTY;
    }

    public ItemStack getItemInSlot(ItemStack stack, int slot) {
        Preconditions.checkElementIndex(slot, SLOTS, "Slot " + slot + " is invalid. Chalk Box has " + SLOTS + " slots.");
        @Nullable ChalkBoxContents contents = stack.get(Chalk.DataComponents.CHALK_BOX_CONTENTS);
        return contents != null ? contents.items().get(slot) : ItemStack.EMPTY;
    }

    public ItemStack getSelectedChalk(ItemStack stack) {
        for (int i = 0; i < CHALK_SLOTS; i++) {
            ItemStack itemInSlot = getItemInSlot(stack, i);
            if (!itemInSlot.isEmpty()) {
                return itemInSlot;
            }
        }

        return ItemStack.EMPTY;
    }

    public void setItemInSlot(ItemStack chalkBoxStack, int slot, ItemStack stack) {
        Preconditions.checkElementIndex(slot, SLOTS, "Slot " + slot + " is invalid. Chalk Box has " + SLOTS + " slots.");
        if (!stack.isEmpty()) {
            Preconditions.checkArgument(stack.getItem() instanceof ChalkItem || slot == GLOWINGS_SLOT_INDEX,
                    "%s cannot be inserted into slot '%s'. Only ChalkItem can be inserted into slots 0-%s.", stack, slot, CHALK_SLOTS - 1);
            Preconditions.checkArgument(stack.is(Chalk.Tags.Items.GLOWINGS) || slot != GLOWINGS_SLOT_INDEX,
                    "%s cannot be inserted into slot '%s'. Only #chalk:glowings can be inserted into slot {}", stack, slot, GLOWINGS_SLOT_INDEX);
        }

        ChalkBoxContents contents = chalkBoxStack.has(Chalk.DataComponents.CHALK_BOX_CONTENTS)
                ? chalkBoxStack.get(Chalk.DataComponents.CHALK_BOX_CONTENTS) : ChalkBoxContents.EMPTY;

        Preconditions.checkState(contents != null);

        ChalkBoxContents.Mutable mutableContents = contents.toMutable();
        mutableContents.setItem(stack, slot);

        chalkBoxStack.set(Chalk.DataComponents.CHALK_BOX_CONTENTS, mutableContents.toImmutable());

        if (slot == GLOWINGS_SLOT_INDEX) {
            updateGlow(chalkBoxStack);
        }
    }

    /**
     * Rotates chalks inside until first slot is changed to chalk with other color.
     */
    private boolean rotateSelectedChalk(ItemStack chalkBoxStack) {
        Preconditions.checkArgument(chalkBoxStack.getItem() instanceof ChalkBoxItem, "Item was not a Chalk Box.");

        ChalkBoxContents contents = getContents(chalkBoxStack);

        int selectedChalkIndex = getContents(chalkBoxStack).getSelectedChalkIndex();

        ArrayList<ItemStack> items = new ArrayList<>(contents.items().stream().limit(CHALK_SLOTS).toList());
        int chalks = ((int) items.stream()
                .filter(stack -> stack.getItem() instanceof IChalkDrawingTool)
                .count());

        if (selectedChalkIndex < 0 || chalks < 2) {
            return false;
        }

        int rotateAmount = 0;
        for (int i = selectedChalkIndex + 1; i < CHALK_SLOTS; i++) {
            if (contents.items().get(i).getItem() instanceof IChalkDrawingTool) {
                rotateAmount = i;
                break;
            }
        }

        if (rotateAmount == 0)
            return false;

        Collections.rotate(items, -rotateAmount);

        items.add(contents.items().get(GLOWINGS_SLOT_INDEX));

        chalkBoxStack.set(Chalk.DataComponents.CHALK_BOX_CONTENTS, contents.toMutable().setItems(items).toImmutable());
        return true;
    }

    /**
     * Used by ItemOverrides to determine what chalk to display with the item texture.
     */
    public float getSelectedChalkColor(ItemStack stack) {
        ChalkBoxContents contents = getContents(stack);
        for (ItemStack item : contents.items()) {
            if (item.getItem() instanceof ChalkItem chalkItem) {
                return chalkItem.getColor().getId() + 1;
            }
        }

        return 0f;
    }

    @Override
    public Optional<DyeColor> getMarkColor(ItemStack chalkBoxStack) {
        ItemStack selectedChalk = getSelectedChalk(chalkBoxStack);
        return selectedChalk.getItem() instanceof IChalkDrawingTool drawingTool ? drawingTool.getMarkColor(selectedChalk) : Optional.empty();
    }

    @Override
    public int getMarkColorValue(ItemStack chalkBoxStack) {
        return getMarkColor(chalkBoxStack)
                .map(ChalkColors::fromDyeColor)
                .orElse(0xFFFFFFFF);
    }

    public boolean isItemValid(ItemStack chalkBoxStack, int slot, @NotNull ItemStack stack) {
        if (slot < 0 || slot >= SLOTS) {
            return false;
        } else if (slot == GLOWINGS_SLOT_INDEX) {
            return stack.is(Chalk.Tags.Items.GLOWINGS);
        } else {
            return stack.getItem() instanceof ChalkItem;
        }
    }

    @Override
    public boolean isGlowing(ItemStack chalkBoxStack) {
        return Config.Common.CHALK_BOX_GLOWING_ENABLED.get() && getContents(chalkBoxStack).glowAmount() > 0;
    }

    public int getGlowAmount(ItemStack chalkBoxStack) {
        return Config.Common.CHALK_BOX_GLOWING_ENABLED.get() ? getContents(chalkBoxStack).glowAmount() : 0;
    }

    public void consumeGlow(ItemStack chalkBoxStack) {
        setGlowAmount(chalkBoxStack, getGlowAmount(chalkBoxStack) - 1);
    }

    public void setGlowAmount(ItemStack chalkBoxStack, int glow) {
        ChalkBoxContents.Mutable mutableContents = getContents(chalkBoxStack).toMutable();
        mutableContents.setGlowingUses(Math.max(0, glow));
        chalkBoxStack.set(Chalk.DataComponents.CHALK_BOX_CONTENTS, mutableContents.toImmutable());

        updateGlow(chalkBoxStack);
    }

    protected void updateGlow(ItemStack chalkBoxStack) {
        if (getGlowAmount(chalkBoxStack) > 0) {
            return;
        }

        ItemStack glowingItemStack = getItemInSlot(chalkBoxStack, GLOWINGS_SLOT_INDEX);
        if (!glowingItemStack.isEmpty()) {
            setGlowAmount(chalkBoxStack, Config.Common.CHALK_BOX_GLOWING_AMOUNT_PER_ITEM.get());
            glowingItemStack.shrink(1);
            setItemInSlot(chalkBoxStack, GLOWINGS_SLOT_INDEX, glowingItemStack);
        }
    }
}
