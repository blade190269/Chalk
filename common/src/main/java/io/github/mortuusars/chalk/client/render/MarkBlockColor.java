package io.github.mortuusars.chalk.client.render;

import io.github.mortuusars.chalk.world.block.MarkBlockEntity;
import io.github.mortuusars.chalk.world.chalk.Mark;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MarkBlockColor implements BlockColor {
    @Override
    public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos pos, int index) {
        if (blockAndTintGetter != null && blockAndTintGetter.getBlockEntity(pos) instanceof MarkBlockEntity blockEntity) {
            @Nullable Mark mark = blockEntity.getMarks().get(index);
            if (mark != null) {
                return mark.color();
            }
        }

        return 0xFFFFFFFF;
    }
}
