package io.github.mortuusars.chalk.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.item.ChalkItem;
import io.github.mortuusars.chalk.world.item.MarkDrawable;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ChalkBoxContents(List<ItemStack> items, int glowAmount) implements TooltipComponent {
    public static final int SLOTS = 17;
    public static final int CHALK_SLOTS = 16;
    public static final int GLOWINGS_SLOT = SLOTS - 1;

    public static final Codec<ChalkBoxContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.OPTIONAL_CODEC.listOf(0, SLOTS)
                      .optionalFieldOf("items", NonNullList.withSize(SLOTS, ItemStack.EMPTY))
                      .forGetter(ChalkBoxContents::getItemsTrimmed),
                Codec.INT
                      .optionalFieldOf("glowing_uses", 0)
                      .forGetter(ChalkBoxContents::glowAmount))
          .apply(instance, ChalkBoxContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChalkBoxContents> STREAM_CODEC = StreamCodec.composite(
          ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), ChalkBoxContents::items,
          ByteBufCodecs.INT, ChalkBoxContents::glowAmount,
          ChalkBoxContents::new
    );

    public static final ChalkBoxContents EMPTY = new ChalkBoxContents(Collections.emptyList(), 0);

    public ChalkBoxContents(List<ItemStack> items, int glowAmount) {
        this.items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(items.size(), SLOTS); i++) {
            this.items.set(i, items.get(i));
        }
        this.glowAmount = glowAmount;
    }

//    public ChalkBoxContents(Container container) {
//        this.items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
//        for (int i = 0; i < Math.min(container.getContainerSize(), SLOTS); i++) {
//            this.items.set(i, container.getItem(i));
//        }
//    }

    public static ChalkBoxContents of(ItemStack stack) {
        return stack.getOrDefault(Chalk.DataComponents.CHALK_BOX_CONTENTS, EMPTY);
    }

    // --

    public boolean isEmpty() {
        return this == EMPTY || items().isEmpty() || items().stream().allMatch(ItemStack::isEmpty);
    }

    public int size() {
        return items.size();
    }

    /**
     * Items should not be modified. Use {@link ChalkBoxContents#copyItems()} if modification is needed.
     */
    @Override
    public List<ItemStack> items() {
        return items;
    }

    /**
     * Items without trailing empty stacks.
     * <br>
     * Items should not be modified. Use {@link ChalkBoxContents#copyItems()} if modification is needed.
     */
    public List<ItemStack> getItemsTrimmed() {
        List<ItemStack> list = new ArrayList<>(items);
        for (int i = list.size() - 1; i >= 0; i--) {
            if (!list.get(i).isEmpty()) {
                break;
            }
            list.remove(i);
        }
        return list;
    }

    /**
     * Item should not be modified. Copy the stack if modification is needed.
     */
    public @NotNull ItemStack getItem(int index) {
        return index < size() ? items.get(index) : ItemStack.EMPTY;
    }

    /**
     * Returned item can be modified safely.
     */
    public @NotNull ItemStack copyItem(int index) {
        return getItem(index).copy();
    }

    /**
     * Returned items can be modified safely.
     */
    public List<ItemStack> copyItems() {
        return Lists.transform(this.items, ItemStack::copy);
    }

    public int getSelectedChalkIndex() {
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack itemInSlot = items().get(slot);
            if (itemInSlot.getItem() instanceof MarkDrawable) {
                return slot;
            }
        }

        return -1;
    }

    // --

    @SuppressWarnings("deprecation")
    public boolean equals(Object another) {
        if (this == another) return true;
        return another instanceof ChalkBoxContents anotherContents
              && ItemStack.listMatches(this.items, anotherContents.items)
              && glowAmount == anotherContents.glowAmount();
    }

    @SuppressWarnings("deprecation")
    public int hashCode() {
        return ItemStack.hashStackList(this.items) + glowAmount;
    }

    @Override
    public @NotNull String toString() {
        return "ChalkBoxContents{" +
              "items=" + items +
              ", glowAmount=" + glowAmount +
              '}';
    }

    // --

    public static boolean canHold(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOTS) {
            return false;
        } else if (!stack.getItem().canFitInsideContainerItems()) {
            return false;
        } else if (slot == GLOWINGS_SLOT) {
            return stack.is(Chalk.Tags.Items.GLOWINGS);
        } else {
            return stack.getItem() instanceof ChalkItem;
        }
    }
}
