package io.github.mortuusars.chalk.world.block;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.chalk.MarkSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MarkBlockEntity extends BlockEntity {
    public static final Logger LOGGER = LogUtils.getLogger();

    protected MarkSet marks = new MarkSet();

    protected MarkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public MarkBlockEntity(BlockPos pos, BlockState blockState) {
        super(Chalk.BlockEntityTypes.MARK.get(), pos, blockState);
    }

    public MarkSet getMarks() {
        return marks;
    }

    public void marksChanged() {
        setChanged();
        if (level != null) {
            // Forces client to update
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
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
                  .ifPresent(result -> {
                      marks = result.getFirst();
                      if (level != null && level.isClientSide) {
                          // Updates model on the client
                          level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
                      }
                  });
        }
    }
}
