package io.github.mortuusars.chalk.world.block;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.chalk.Chalk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

/*
Dummy block entity to be able to tick a block and convert the old mark.
 */
@SuppressWarnings("removal")
@Deprecated(since = "2.0.0", forRemoval = true)
public class OldMarkBlockEntity extends BlockEntity {
    public static final Logger LOGGER = LogUtils.getLogger();

    public OldMarkBlockEntity(BlockPos pos, BlockState blockState) {
        super(Chalk.BlockEntityTypes.CHALK_MARK.get(), pos, blockState);
    }
}
