package io.github.mortuusars.chalk.neoforge.client;

import com.mojang.math.Transformation;
import io.github.mortuusars.chalk.client.render.MarkBakedModel;
import io.github.mortuusars.chalk.world.block.MarkBlockEntity;
import io.github.mortuusars.chalk.world.chalk.Mark;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NeoForgeMarkBakedModel extends MarkBakedModel {
    ModelProperty<Mark[]> MARKS = new ModelProperty<>();

    protected final ModelState transform = new ModelState() {
        @Override
        public @NotNull Transformation getRotation() {
            return Transformation.identity();
        }
    };

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

    public BakedQuad bakeMarkQuad(Direction direction, @NotNull Mark mark) {
        TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
              .apply(mark.symbol().value().texture());

        // Tint index is set to direction index, to tint each face independently in MarkBlockColor
        BlockElementFace blockPartFace = new BlockElementFace(direction, direction.get3DDataValue(), "",
              new BlockFaceUV(new float[]{0f, 0f, 16f, 16f}, direction == Direction.DOWN ? 180 : 0));

        // Rotate the texture
        int rotationOffset = mark.symbol().value().rotationOffset();
        int rotation = (mark.orientation().getRotation() + (direction == Direction.DOWN ? -rotationOffset : rotationOffset)) % 360;

        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE || direction.getAxis() == Direction.Axis.Y) {
            rotation = 360 - rotation;
        }

        BlockElementRotation blockPartRotation = new BlockElementRotation(ROTATION_ORIGIN, direction.getAxis(), rotation, false);

        Vector3f from = FROM_COORDS.get(direction);
        Vector3f to = TO_COORDS.get(direction);
        BakedQuad bakedQuad = faceBakery.bakeQuad(from, to, blockPartFace, texture, direction, transform, blockPartRotation, !mark.glowing());

        if (mark.glowing()) {
            int[] vertexData = bakedQuad.getVertices();
            int step = vertexData.length / 4;

            // Set lighting to full-bright on all vertices
            vertexData[6] = 0x00F000F0;
            vertexData[6 + step] = 0x00F000F0;
            vertexData[6 + 2 * step] = 0x00F000F0;
            vertexData[6 + 3 * step] = 0x00F000F0;
        }

        return bakedQuad;
    }
}
