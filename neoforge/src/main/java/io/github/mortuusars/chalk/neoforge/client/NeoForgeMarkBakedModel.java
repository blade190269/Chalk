package io.github.mortuusars.chalk.neoforge.client;

import io.github.mortuusars.chalk.client.render.MarkBakedModel;
import io.github.mortuusars.chalk.world.block.MarkBlockEntity;
import io.github.mortuusars.chalk.world.chalk.Mark;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NeoForgeMarkBakedModel extends MarkBakedModel {
    ModelProperty<Mark[]> MARKS = new ModelProperty<>();

    public NeoForgeMarkBakedModel(BakedModel baseModel) {
        super(baseModel);
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource random, @NotNull ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.cutout());
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        if (!(level.getBlockEntity(pos) instanceof MarkBlockEntity blockEntity) || blockEntity.getMarks().isEmpty()) {
            return modelData;
        }

        if (blockEntity.getMarks().isEmpty()) {
            return modelData;
        }

        return ModelData.builder().with(MARKS, blockEntity.getMarks().copyArray()).build();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource random,
                                             @NotNull ModelData data, @Nullable RenderType renderType) {
        if (side != null || !(data.get(MARKS) instanceof Mark[] marks) || marks.length == 0) {
            return Collections.emptyList();
        }

        List<BakedQuad> quads = new ArrayList<>();


        for (int i = 0; i < 6; i++) {
            @Nullable Mark mark = marks[i];
            if (mark == null) {
                continue;
            }

            Direction direction = Direction.from3DDataValue(i);
            quads.add(bakeMarkQuad(direction, mark));
        }

        return quads;
    }
}
