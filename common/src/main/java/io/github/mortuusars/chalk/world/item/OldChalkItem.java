package io.github.mortuusars.chalk.world.item;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Deprecated(since = "2.0.0", forRemoval = true)
public class OldChalkItem extends Item {
    private final DyeColor color;

    public OldChalkItem(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag tooltipFlag) {
        lines.add(Component.translatable("tooltip.chalk.deprecated").withStyle(ChatFormatting.DARK_RED));
        lines.add(Component.translatable("tooltip.chalk.deprecated_use_to_convert").withStyle(ChatFormatting.DARK_RED));
    }

    // --

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack convertedStack = convert(stack);
        player.setItemInHand(hand ,convertedStack);
        player.playSound(Chalk.SoundEvents.MARK_REMOVED.get());
        return InteractionResultHolder.success(convertedStack);
    }

    public @NotNull ItemStack convert(ItemStack stack) {
        ItemStack convertedStack = stack.transmuteCopy(Chalk.Items.CHALK.get());
        if (getColor() != DyeColor.WHITE) {
            DyedItemColor dyedColor = new DyedItemColor(Chalk.Items.CHALK.get().getColorFromDye(convertedStack, getColor()), true);
            convertedStack.set(DataComponents.DYED_COLOR, dyedColor);
        }
        return convertedStack;
    }
}
