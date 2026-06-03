package io.github.mortuusars.chalk.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.core.IChalkDrawingTool;
import io.github.mortuusars.chalk.item.ChalkBoxItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ChalkBoxContents(List<ItemStack> items, int glowAmount) implements TooltipComponent {
    public static final ChalkBoxContents EMPTY = new ChalkBoxContents(NonNullList.withSize(ChalkBoxItem.SLOTS, ItemStack.EMPTY), 0);

    public static final Codec<ChalkBoxContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ItemStack.OPTIONAL_CODEC.listOf()
                            .optionalFieldOf("items", NonNullList.withSize(ChalkBoxItem.SLOTS, ItemStack.EMPTY))
                            .forGetter(ChalkBoxContents::items),
                    Codec.INT
                            .optionalFieldOf("glowing_uses", 0)
                            .forGetter(ChalkBoxContents::glowAmount))
            .apply(instance, ChalkBoxContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChalkBoxContents> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), ChalkBoxContents::items,
            ByteBufCodecs.INT, ChalkBoxContents::glowAmount,
            ChalkBoxContents::new
    );

    public ChalkBoxContents {
        if (items.size() < ChalkBoxItem.SLOTS) {
            items = new ArrayList<>(items);
            while (items.size() < ChalkBoxItem.SLOTS) {
                items.add(ItemStack.EMPTY);
            }
        } else if (items.size() > ChalkBoxItem.SLOTS) {
            items = new ArrayList<>(items);
            while (items.size() > ChalkBoxItem.SLOTS) {
                items.remove(ChalkBoxItem.SLOTS);
            }
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ChalkBoxContents that = (ChalkBoxContents) obj;

        if (glowAmount != that.glowAmount()) return false;

        if (items.size() != that.items.size()) return false;

        for (int i = 0; i < items.size(); i++) {
            if (!ItemStack.isSameItemSameComponents(items.get(i), that.items.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = glowAmount;
        for (ItemStack stack : items) {
            result = 31 * result + Objects.hashCode(ItemStack.hashItemAndComponents(stack));
        }
        return result;
    }

    public int getSelectedChalkIndex() {
        for (int slot = 0; slot < ChalkBoxItem.SLOTS; slot++) {
            ItemStack itemInSlot = items().get(slot);
            if (itemInSlot.getItem() instanceof IChalkDrawingTool) {
                return slot;
            }
        }

        return -1;
    }

    public Mutable toMutable() {
        return new Mutable(this);
    }

    @Override
    public String toString() {
        return "ChalkBoxContents{" +
                "items=" + items +
                ", glowingUses=" + glowAmount +
                '}';
    }

    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty) && glowAmount() <= 0;
    }

    public static class Mutable {
        private List<ItemStack> items;
        private int glowingUses;

        public Mutable(ChalkBoxContents contents) {
            this.items = new ArrayList<>(contents.items());
            this.glowingUses = contents.glowAmount();
        }

        public List<ItemStack> getItems() {
            return items;
        }

        public ChalkBoxContents.Mutable setItems(@NotNull List<ItemStack> items) {
            this.items = items;
            return this;
        }

        public ChalkBoxContents.Mutable setItem(ItemStack stack, int slot) {
            this.items.set(slot, stack);
            return this;
        }

        public int getGlowingUses() {
            return glowingUses;
        }

        public ChalkBoxContents.Mutable setGlowingUses(int uses) {
            this.glowingUses = uses;
            return this;
        }

        public ChalkBoxContents.Mutable clear() {
            this.items.clear();
            this.glowingUses = 0;
            return this;
        }

        public ChalkBoxContents toImmutable() {
            return new ChalkBoxContents(List.copyOf(this.items), this.glowingUses);
        }
    }
}
