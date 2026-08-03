package io.github.mortuusars.chalk.world.item;

import io.github.mortuusars.chalk.data.ChalkColors;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import io.github.mortuusars.chalk.world.chalk.Mark;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ChalkItem extends Item implements MarkDrawable {
    private final DyeColor color;

    public ChalkItem(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        InteractionHand hand = context.getHand();
        ItemStack itemStack = context.getItemInHand();
        Player player = context.getPlayer();

        if (player == null || !(itemStack.getItem() instanceof ChalkItem)) {
            return InteractionResult.FAIL;
        }

        // When holding chalks in both hands - skip drawing from offhand
        if (hand == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof ChalkItem) {
            return InteractionResult.FAIL;
        }

        MarkDrawingContext drawingContext = createMarkDrawingContext(player, context.getClickedPos(),
              context.getClickLocation(), context.getClickedFace(), hand);

        if (!drawingContext.canDraw()) {
            return InteractionResult.FAIL;
        }

        if (player.isSecondaryUseActive()) {
            drawingContext.openSymbolSelectionScreen();
            return InteractionResult.CONSUME;
        }

        if (drawMark(drawingContext, drawingContext.createRegularMark(ChalkColors.fromDyeColor(color), false))) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

//        if (drawingContext.hasExistingMark()) {
//            return InteractionResult.PASS;
//        }

        return InteractionResult.FAIL;
    }

    @Override
    public void onMarkDrawn(Player player, InteractionHand drawingHand, BlockPos markPos, Direction facing, Mark mark) {
        MarkDrawable.super.onMarkDrawn(player, drawingHand, markPos, facing, mark);

        if (player.isCreative()) {
            return;
        }

        ItemStack stack = player.getItemInHand(drawingHand);
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(drawingHand));
    }

    @Override
    public int getMarkColorValue(ItemStack stack) {
        return ChalkColors.fromDyeColor(getColor());
    }

    @Override
    public Optional<DyeColor> getMarkColor(ItemStack stack) {
        return Optional.of(getColor());
    }

    @Override
    public boolean isGlowing(ItemStack stack) {
        return false;
    }

    public DyeColor getColor() {
        return this.color;
    }
}
