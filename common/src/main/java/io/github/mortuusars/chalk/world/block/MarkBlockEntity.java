package io.github.mortuusars.chalk.world.block;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.chalk.symbol.SymbolOrientation;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.chalk.world.chalk.MarkSet;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class MarkBlockEntity extends BlockEntity {
    public static final Logger LOGGER = LogUtils.getLogger();

    protected MarkSet marks = new MarkSet();

    protected MarkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public MarkBlockEntity(BlockPos pos, BlockState blockState) {
        super(Chalk.BlockEntityTypes.CHALK_MARK.get(), pos, blockState);
    }

    public MarkSet getMarks() {
        return marks;
    }

    // --

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        MarkSet.CODEC
              .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), marks)
              .resultOrPartial(LOGGER::error)
              .ifPresent(result -> tag.put("Marks", result));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("Marks")) {
            MarkSet.CODEC.decode(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("Marks"))
                  .resultOrPartial(LOGGER::error)
                  .ifPresent(result -> marks = result.getFirst());
        }
    }
}
