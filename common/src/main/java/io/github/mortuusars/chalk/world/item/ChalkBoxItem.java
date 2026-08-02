package io.github.mortuusars.chalk.world.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Platform;
import io.github.mortuusars.chalk.world.block.ChalkMarkBlock;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.core.ChalkMarkDrawable;
import io.github.mortuusars.chalk.core.Mark;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.world.inventory.ChalkBoxMenu;
import io.github.mortuusars.chalk.data.ChalkColors;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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

public class ChalkBoxItem extends Item implements ChalkMarkDrawable {
    public ChalkBoxItem(Properties properties) {
        super(properties);
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
                    && screen.hoveredSlot != null
                    && screen.hoveredSlot.container instanceof Inventory) {
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

        if (otherStack.isEmpty()) {
            //TODO: extract selected?
            return false;
        }

        // Open
//        if (otherStack.isEmpty() && slot.container instanceof Inventory) {
//            if (player.isCreative()) {
//                Packets.sendToServer(new OpenCreativeChalkBoxC2SP(slot.getContainerSlot()));
//            } else if (player instanceof ServerPlayer serverPlayer) {
//                openGUI(serverPlayer, stack);
//            }
//            return true;
//        }

        // Insert chalk into box:
        if (otherStack.getItem() instanceof ChalkItem) {
            for (int i = 0; i < ChalkBoxContents.CHALK_SLOTS; i++) {
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
            ItemStack existingItem = getItemInSlot(stack, ChalkBoxContents.GLOWINGS_SLOT);
            int glowAmountBefore = getGlowAmount(stack);

            if (existingItem.isEmpty()) {
                setItemInSlot(stack, ChalkBoxContents.GLOWINGS_SLOT, otherStack.copy());
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
                setItemInSlot(stack, ChalkBoxContents.GLOWINGS_SLOT, existingItem);
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
    public boolean isEnchantable(@NotNull ItemStack stack) {
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
                open(serverPlayer, context.getHand());
            }
            return InteractionResult.SUCCESS;
        }

        MarkDrawingContext drawingContext = createDrawingContext(context);

        if (!drawingContext.canDraw() || !(selectedChalk.getItem() instanceof ChalkMarkDrawable chalkDrawingTool))
            return InteractionResult.FAIL;

        if (player.isSecondaryUseActive()) {
            drawingContext.openSymbolSelectionScreen();
            return InteractionResult.CONSUME;
        }

        Mark mark = drawingContext.createRegularMark(chalkDrawingTool.getMarkColorValue(selectedChalk), isGlowing(chalkBoxStack));
        if (drawMark(drawingContext, mark)) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        return InteractionResult.FAIL;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!stack.is(this)) {
            return InteractionResultHolder.pass(stack);
        }

        if (player.isSecondaryUseActive()) {
            if (rotateSelectedChalk(stack)) {
                level.playSound(player, player.position().x, player.position().y, player.position().z, Chalk.SoundEvents.CHALK_BOX_CHANGE.get(), SoundSource.PLAYERS,
                        0.9f, 0.9f + level.random.nextFloat() * 0.2f);
            } else {
                return InteractionResultHolder.fail(stack);
            }
        } else if (player instanceof ServerPlayer serverPlayer) {
            SimpleMenuProvider menuProvider = new SimpleMenuProvider(
                  (id, inventory, pl) -> new ChalkBoxMenu(id, inventory, hand), stack.getHoverName());
            Platform.openMenu(serverPlayer, menuProvider, buffer -> buffer.writeEnum(hand));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public void open(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!(stack.getItem() instanceof ChalkBoxItem)) {
            Chalk.LOGGER.error("Cannot open Chalk Box menu: {} is not a ChalkBoxItem.", stack);
            return;
        }

        Component title = stack.has(DataComponents.CUSTOM_NAME)
              ? stack.getHoverName()
              : Component.translatable("container.chalk.chalk_box");

        SimpleMenuProvider menuProvider = new SimpleMenuProvider((containerID, playerInventory, playerEntity) ->
              new ChalkBoxMenu(containerID, playerInventory, hand), title);

        Platform.openMenu(player, menuProvider, buffer -> buffer.writeEnum(hand));

        player.level().playSound(null, player.position().x, player.position().y, player.position().z,
              Chalk.SoundEvents.CHALK_BOX_OPEN.get(), SoundSource.PLAYERS,
              0.9f, 0.9f + player.level().random.nextFloat() * 0.2f);
    }

//    public void openGUI(ServerPlayer player, ItemStack chalkBoxStack) {
//        if (!(chalkBoxStack.getItem() instanceof ChalkBoxItem)) {
//            Chalk.LOGGER.error("Cannot open Chalk Box menu: {} is not a ChalkBoxItem.", chalkBoxStack);
//            return;
//        }
//
//        int chalkBoxSlotIndex = player.getInventory().findSlotMatchingItem(chalkBoxStack);
//        if (chalkBoxSlotIndex < 0) {
//            Chalk.LOGGER.error("Cannot open Chalk Box menu: {} is not found in player's inventory.", chalkBoxStack);
//            return;
//        }
//
//        Component title = chalkBoxStack.has(DataComponents.CUSTOM_NAME)
//                ? chalkBoxStack.getHoverName()
//                : Component.translatable("container.chalk.chalk_box");
//
//        SimpleMenuProvider menuProvider = new SimpleMenuProvider((containerID, playerInventory, playerEntity) ->
//                new ChalkBoxMenu(containerID, playerInventory, chalkBoxSlotIndex), title);
//
//        Platform.openMenu(player, menuProvider, buffer -> buffer.writeVarInt(chalkBoxSlotIndex));
//
//        player.level().playSound(null, player.position().x, player.position().y, player.position().z,
//                Chalk.SoundEvents.CHALK_BOX_OPEN.get(), SoundSource.PLAYERS,
//                0.9f, 0.9f + player.level().random.nextFloat() * 0.2f);
//    }

    @Override
    public Mark getMark(ItemStack chalkBoxStack, MarkDrawingContext drawingContext, MarkSymbol symbol) {
        ItemStack selectedChalk = getSelectedChalk(chalkBoxStack);

        DyeColor color = selectedChalk.getItem() instanceof ChalkMarkDrawable chalkItem
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
        Preconditions.checkElementIndex(slot, ChalkBoxContents.SLOTS, "Slot " + slot + " is invalid. Chalk Box has " + ChalkBoxContents.SLOTS + " slots.");
        @Nullable ChalkBoxContents contents = stack.get(Chalk.DataComponents.CHALK_BOX_CONTENTS);
        return contents != null ? contents.items().get(slot) : ItemStack.EMPTY;
    }

    public ItemStack getSelectedChalk(ItemStack stack) {
        for (int i = 0; i < ChalkBoxContents.CHALK_SLOTS; i++) {
            ItemStack itemInSlot = getItemInSlot(stack, i);
            if (!itemInSlot.isEmpty()) {
                return itemInSlot;
            }
        }

        return ItemStack.EMPTY;
    }

    public void setItemInSlot(ItemStack chalkBoxStack, int slot, ItemStack stack) {
        Preconditions.checkElementIndex(slot, ChalkBoxContents.SLOTS, "Slot " + slot + " is invalid. Chalk Box has " + ChalkBoxContents.SLOTS + " slots.");
        if (!stack.isEmpty()) {
            Preconditions.checkArgument(stack.getItem() instanceof ChalkItem || slot == ChalkBoxContents.GLOWINGS_SLOT,
                    "%s cannot be inserted into slot '%s'. Only ChalkItem can be inserted into slots 0-%s.", stack, slot, ChalkBoxContents.CHALK_SLOTS - 1);
            Preconditions.checkArgument(stack.is(Chalk.Tags.Items.GLOWINGS) || slot != ChalkBoxContents.GLOWINGS_SLOT,
                    "%s cannot be inserted into slot '%s'. Only #chalk:glowings can be inserted into slot {}", stack, slot, ChalkBoxContents.GLOWINGS_SLOT);
        }

        ChalkBoxContents contents = chalkBoxStack.has(Chalk.DataComponents.CHALK_BOX_CONTENTS)
                ? chalkBoxStack.get(Chalk.DataComponents.CHALK_BOX_CONTENTS) : ChalkBoxContents.EMPTY;

        Preconditions.checkState(contents != null);

//        ChalkBoxContents.Mutable mutableContents = contents.toMutable();
//        mutableContents.setItem(stack, slot);
//
//        chalkBoxStack.set(Chalk.DataComponents.CHALK_BOX_CONTENTS, mutableContents.toImmutable());
//
//        if (slot == ChalkBoxContents.GLOWINGS_SLOT) {
//            updateGlow(chalkBoxStack);
//        }
    }

    /**
     * Rotates chalks inside until first slot is changed to chalk with other color.
     */
    private boolean rotateSelectedChalk(ItemStack chalkBoxStack) {
        Preconditions.checkArgument(chalkBoxStack.getItem() instanceof ChalkBoxItem, "Item was not a Chalk Box.");

        ChalkBoxContents contents = getContents(chalkBoxStack);

        int selectedChalkIndex = getContents(chalkBoxStack).getSelectedChalkIndex();

        ArrayList<ItemStack> items = new ArrayList<>(contents.items().stream().limit(ChalkBoxContents.CHALK_SLOTS).toList());
        int chalks = ((int) items.stream()
                .filter(stack -> stack.getItem() instanceof ChalkMarkDrawable)
                .count());

        if (selectedChalkIndex < 0 || chalks < 2) {
            return false;
        }

        int rotateAmount = 0;
        for (int i = selectedChalkIndex + 1; i < ChalkBoxContents.CHALK_SLOTS; i++) {
            if (contents.items().get(i).getItem() instanceof ChalkMarkDrawable) {
                rotateAmount = i;
                break;
            }
        }

        if (rotateAmount == 0)
            return false;

        Collections.rotate(items, -rotateAmount);

        items.add(contents.items().get(ChalkBoxContents.GLOWINGS_SLOT));

//        chalkBoxStack.set(Chalk.DataComponents.CHALK_BOX_CONTENTS, contents.toMutable().setItems(items).toImmutable());
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
        return selectedChalk.getItem() instanceof ChalkMarkDrawable drawingTool ? drawingTool.getMarkColor(selectedChalk) : Optional.empty();
    }

    @Override
    public int getMarkColorValue(ItemStack chalkBoxStack) {
        return getMarkColor(chalkBoxStack)
                .map(ChalkColors::fromDyeColor)
                .orElse(0xFFFFFFFF);
    }

    public boolean isItemValid(ItemStack chalkBoxStack, int slot, @NotNull ItemStack stack) {
        if (slot < 0 || slot >= ChalkBoxContents.SLOTS) {
            return false;
        } else if (slot == ChalkBoxContents.GLOWINGS_SLOT) {
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
//        ChalkBoxContents.Mutable mutableContents = getContents(chalkBoxStack).toMutable();
//        mutableContents.setGlowingUses(Math.max(0, glow));
//        chalkBoxStack.set(Chalk.DataComponents.CHALK_BOX_CONTENTS, mutableContents.toImmutable());

        updateGlow(chalkBoxStack);
    }

    protected void updateGlow(ItemStack chalkBoxStack) {
        if (getGlowAmount(chalkBoxStack) > 0) {
            return;
        }

        ItemStack glowingItemStack = getItemInSlot(chalkBoxStack, ChalkBoxContents.GLOWINGS_SLOT);
        if (!glowingItemStack.isEmpty()) {
            setGlowAmount(chalkBoxStack, Config.Common.CHALK_BOX_GLOWING_AMOUNT_PER_ITEM.get());
            glowingItemStack.shrink(1);
            setItemInSlot(chalkBoxStack, ChalkBoxContents.GLOWINGS_SLOT, glowingItemStack);
        }
    }
}
