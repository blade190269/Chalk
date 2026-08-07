package io.github.mortuusars.chalk.world.inventory;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.world.item.ChalkBoxItem;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChalkBoxMenu extends AbstractInHandContainerMenu {
    public ChalkBoxMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(Chalk.MenuTypes.CHALK_BOX.get(), containerId, playerInventory, hand);
    }

    @Override
    protected Container createContainer() {
        List<ItemStack> items = new ArrayList<>(ChalkBoxContents.of(getItemInHand()).copyItems());
        while (items.size() < ChalkBoxContents.SLOTS) {
            items.add(ItemStack.EMPTY);
        }

        return new SimpleContainer(items.stream().limit(ChalkBoxContents.SLOTS).toArray(ItemStack[]::new));
    }

    @Override
    protected void addContainerSlots() {
        int chalkSlotsX = 62;
        int chalkSlotsY = 18;

        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                int index = column + row * 3;
                int x = chalkSlotsX + column * 18;
                int y = chalkSlotsY + row * 18;

                addSlot(new Slot(getContainer(), index, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return ChalkBoxContents.canHold(getContainerSlot(), stack);
                    }
                });
            }
        }

        addSlot(new Slot(getContainer(), ChalkBoxContents.GLOWINGS_SLOT, 134, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ChalkBoxContents.canHold(getContainerSlot(), stack);
            }

            @Override
            public void set(@NotNull ItemStack stack) {
                if (getPlayer().level().isClientSide && this.getItem().isEmpty()
                      && getGlowAmount() <= 0 && stack.is(Chalk.Tags.Items.GLOWINGS)) {
                    Vec3 pos = getPlayer().position();
                    getPlayer().level().playSound(getPlayer(), pos.x, pos.y, pos.z, Chalk.SoundEvents.GLOW_APPLIED.get(), SoundSource.PLAYERS, 1f, 1f);
                    getPlayer().level().playSound(getPlayer(), pos.x, pos.y, pos.z, Chalk.SoundEvents.GLOWING.get(), SoundSource.PLAYERS, 1f, 1f);
                }

                super.set(stack);
            }
        });

//        ---
//
//        IItemHandler itemHandler = new ChalkBoxItemStackHandler(chalkBoxStack) {
//            @Override
//            protected void onContentsChanged(int slot) {
//                super.onContentsChanged(slot);
//                if (player.isCreative()) {
//                    playerInventory.setItem(slot, this.getChalkBoxStack());
//                }
//            }
//        };
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        player.playSound(Chalk.SoundEvents.CHALK_BOX_CLOSE.get(), 0.85f, 0.9f + player.level().getRandom().nextFloat() * 0.2f);

        if (player.isCreative() && !player.level().isClientSide()) {
            player.getInventory().setItem(getUsedSlot(), getItemInHand());
        }

        // Fixes inventory not syncing after closing:
        player.inventoryMenu.resumeRemoteUpdates();
    }

    public boolean isGlowingEnabled() {
        return Config.Server.CHALK_BOX_GLOWING_ENABLED.get();
    }

    public int getGlowAmount() {
        return getUsedItem() instanceof ChalkBoxItem chalkBoxItem
              ? chalkBoxItem.getGlowAmount(getItemInHand())
              : 0;
    }

//    @Override
//    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
//        ItemStack itemstack = ItemStack.EMPTY;
//        Slot slot = this.slots.get(index);
//        if (slot.hasItem()) {
//            ItemStack slotItemStack = slot.getItem();
//            itemstack = slotItemStack.copy();
//            if (index < OldChalkBoxContents.SLOTS) { // From Chalk Box to player inventory.
//                if (!this.moveItemStackTo(slotItemStack, OldChalkBoxContents.SLOTS, this.slots.size(), true))
//                    return ItemStack.EMPTY;
//            } else if (!this.moveItemStackTo(slotItemStack, 0, OldChalkBoxContents.SLOTS, false)) // From player inventory to box.
//                return ItemStack.EMPTY;
//
//
//            if (slotItemStack.isEmpty())
//                slot.set(ItemStack.EMPTY);
//            else
//                slot.setChanged();
//        }
//
//        return itemstack;
//    }

//    @Override
//    public boolean stillValid(@NotNull Player player) {
//        return chalkBoxSlotIndex >= 0 && getChalkBoxStack().getItem().equals(chalkBoxItem);
//    }

    public static ChalkBoxMenu fromNetwork(int containerID, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        return new ChalkBoxMenu(containerID, playerInventory, buffer.readEnum(InteractionHand.class));
    }
}
