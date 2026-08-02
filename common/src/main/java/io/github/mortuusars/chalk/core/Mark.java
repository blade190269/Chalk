package io.github.mortuusars.chalk.core;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.block.ChalkMarkBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class Mark
{
    public final Direction facing;
    public final int color;
    public final MarkSymbol symbol;
    public final SymbolOrientation orientation;
    public final boolean glowing;

    public Mark(Direction facing, int color, MarkSymbol symbol, SymbolOrientation orientation, boolean glowing) {
        this.facing = facing;
        this.color = color;
        this.symbol = symbol;
        this.orientation = orientation;
        this.glowing = glowing;
    }

    public BlockState createBlockState(ItemStack drawingItem) {
        DyeColor color = DyeColor.WHITE;
        if (drawingItem.getItem() instanceof ChalkMarkDrawable drawingTool) {
            color = drawingTool.getMarkColor(drawingItem).orElse(color);
        }
        return Chalk.Blocks.getMarkBlock(color).defaultBlockState()
                .setValue(ChalkMarkBlock.FACING, facing)
                .setValue(ChalkMarkBlock.SYMBOL, symbol)
                .setValue(ChalkMarkBlock.ORIENTATION, orientation)
                .setValue(ChalkMarkBlock.GLOWING, glowing);
    }
}
