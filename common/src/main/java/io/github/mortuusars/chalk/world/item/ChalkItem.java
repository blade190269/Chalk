package io.github.mortuusars.chalk.world.item;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.data.ChalkColors;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.chalk.world.chalk.MarkDrawingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class ChalkItem extends Item implements MarkDrawable {
    private final DyeColor color;

    public ChalkItem(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override
    public int getMarkDrawingColor(ItemStack stack) {
        return ChalkColors.fromDyeColor(getColor());
    }

    // --

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        InteractionHand hand = context.getHand();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        // When holding drawable items in both hands - skip drawing from offhand
        //TODO: Test if needed
        if (hand == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof MarkDrawable) {
            return InteractionResult.FAIL;
        }

        MarkDrawingContext drawingContext = createMarkDrawingContext(player, context.getHand(),
              context.getClickLocation(), context.getClickedPos(), context.getClickedFace());

        if (!canDrawMark(player, drawingContext)) {
            return InteractionResult.FAIL;
        }

        if (player.isSecondaryUseActive()) {
            selectSymbolAndDraw(player, drawingContext);
            return InteractionResult.CONSUME;
        }

        if (drawMark(player, drawingContext, createRegularMark(player, drawingContext, stack))) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void onMarkDrawn(Player player, MarkDrawingContext context, Mark mark) {
        MarkDrawable.super.onMarkDrawn(player, context, mark);
        onChalkMarkDrawn(player, context.hand(), player.getItemInHand(context.hand()), context.markPos(), context.markFacing(), mark);
    }

    public void onChalkMarkDrawn(Player player, InteractionHand hand, ItemStack stack, BlockPos markPos, Direction markFacing, Mark mark) {
        if (player instanceof ServerPlayer serverPlayer) {
            BlockPos surfacePos = markPos.relative(markFacing.getOpposite());
            MapColor surfaceColor = player.level().getBlockState(surfacePos).getMapColor(player.level(), surfacePos);
            Chalk.CriteriaTriggers.MARK_DRAWN.get().trigger(serverPlayer, stack, surfaceColor, getColor());
        }

        if (!player.isCreative()) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
    }
}
