package io.github.mortuusars.chalk.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.world.item.ChalkBoxItem;
import io.github.mortuusars.chalk.world.item.ChalkItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @param items Readonly list of the items. Use {@link #copyItems()} if modification is needed.
 * @param glow Remaining glow uses.
 * @param selected Slot selected for drawing. Always points to the slot with item or -1 if Chalk Box is empty.
 */
public record ChalkBoxContents(List<ItemStack> items, int glow, int selected) implements TooltipComponent {
    public static final int SLOTS = 10;
    public static final int CHALK_SLOTS = 9;
    public static final int GLOWINGS_SLOT = SLOTS - 1;

    public static final Codec<ChalkBoxContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.OPTIONAL_CODEC.listOf(0, SLOTS)
                      .optionalFieldOf("items", NonNullList.withSize(SLOTS, ItemStack.EMPTY))
                      .forGetter(ChalkBoxContents::getItemsTrimmed),
                Codec.INT
                      .optionalFieldOf("glow", 0)
                      .forGetter(ChalkBoxContents::glow),
                Codec.INT
                      .optionalFieldOf("selected", -1)
                      .forGetter(ChalkBoxContents::selected))
          .apply(instance, ChalkBoxContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChalkBoxContents> STREAM_CODEC = StreamCodec.composite(
          ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), ChalkBoxContents::items,
          ByteBufCodecs.INT, ChalkBoxContents::glow,
          ByteBufCodecs.INT, ChalkBoxContents::selected,
          ChalkBoxContents::new
    );

    public static final ChalkBoxContents EMPTY = new ChalkBoxContents(Collections.emptyList(), 0, -1);

    public ChalkBoxContents(List<ItemStack> items, int glow, int selected) {
        this.items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(items.size(), SLOTS); i++) {
            this.items.set(i, items.get(i));
        }
        this.glow = Math.max(glow, 0);
        this.selected = correctSelected(selected);
    }

    private int correctSelected(int selected) {
        if (!getItem(selected).isEmpty() && selected < CHALK_SLOTS) {
            return selected;
        }

        if (selected <= -1) {
            for (int i = 0; i < items.size(); i++) {
                if (!getItem(i).isEmpty()) {
                    return i;
                }
            }
            return -1;
        }

        if (selected > CHALK_SLOTS - 1) {
            selected = CHALK_SLOTS - 1;
        }

        for (int index = selected; index < CHALK_SLOTS; index++) {
            if (!getItem(index).isEmpty()) {
                return index;
            }
        }

        for (int index = 0; index < selected; index++) {
            if (!getItem(index).isEmpty()) {
                return index;
            }
        }

        return -1;
    }

    public ChalkBoxContents(List<ItemStack> items, int glow) {
        this(items, glow, 0);
    }

    public static ChalkBoxContents of(ItemStack stack) {
        return stack.getOrDefault(Chalk.DataComponents.CHALK_BOX_CONTENTS, EMPTY);
    }

    public ChalkBoxContents.Mutable mutable() {
        return new Mutable(this);
    }

    // --

    public boolean isEmpty() {
        return this == EMPTY || items().isEmpty() || items().stream().allMatch(ItemStack::isEmpty);
    }

    public int size() {
        return items.size();
    }

    public boolean hasChalks() {
        return items().stream().anyMatch(s -> s.getItem() instanceof ChalkItem);
    }

    public int getChalkCount() {
        int count = 0;
        for (int i = 0; i < CHALK_SLOTS; i++) {
            if (!getItem(i).isEmpty()) count++;
        }
        return count;
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
        return index >= 0 && index < size() ? items.get(index) : ItemStack.EMPTY;
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

    public ItemStack getSelectedChalk() {
        return getItem(selected);
    }

    // --

    public ChalkBoxContents withGlow(int glow) {
        return new ChalkBoxContents(items, glow, selected);
    }

    public ChalkBoxContents withSelected(int selected) {
        return new ChalkBoxContents(items, glow, selected);
    }

    // --

    @SuppressWarnings("deprecation")
    public boolean equals(Object another) {
        if (this == another) return true;
        return another instanceof ChalkBoxContents anotherContents
              && ItemStack.listMatches(this.items, anotherContents.items)
              && glow == anotherContents.glow();
    }

    @SuppressWarnings("deprecation")
    public int hashCode() {
        return ItemStack.hashStackList(this.items) + glow;
    }

    @Override
    public @NotNull String toString() {
        return "ChalkBoxContents{" +
              "items=" + items +
              ", glowAmount=" + glow +
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

    public static class Mutable {
        private List<ItemStack> items;
        private int glow;
        private int selected;

        public Mutable(ChalkBoxContents contents) {
            this.items = new ArrayList<>(contents.copyItems());
            this.glow = contents.glow;
            this.selected = contents.selected;
        }

        public ChalkBoxContents toImmutable() {
            return new ChalkBoxContents(items, glow, selected);
        }

        public ItemStack toImmutable(ItemStack stack) {
            stack.set(Chalk.DataComponents.CHALK_BOX_CONTENTS, new ChalkBoxContents(items, glow, selected));
            return stack;
        }

        public Mutable setItems(Container container) {
            for (int i = 0; i < items.size(); i++) {
                items.set(i, container.getItem(i));
            }
            return this;
        }

        public Mutable setItems(List<ItemStack> items) {
            this.items = items;
            return this;
        }

        public Mutable setItem(int index, ItemStack item) {
            items.set(index, item);
            return this;
        }

        public Mutable consumeGlow() {
            glow -= 1;
            return updateGlow();
        }

        public Mutable updateGlow() {
            if (glow <= 0 && !items.get(GLOWINGS_SLOT).isEmpty()) {
                items.get(GLOWINGS_SLOT).shrink(1);
                glow = Config.Server.CHALK_BOX_GLOWING_AMOUNT_PER_ITEM.get();
            }
            return this;
        }

        public Mutable setSelected(int slot) {
            selected = slot;
            return this;
        }
    }
}
