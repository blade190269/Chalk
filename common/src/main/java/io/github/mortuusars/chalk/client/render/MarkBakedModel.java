package io.github.mortuusars.chalk.client.render;

import com.mojang.math.Transformation;
import io.github.mortuusars.chalk.world.chalk.Mark;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;

/**
 * Baked model is used to programmatically create proper Chalk Mark block model.
 * Based on particular blockstate properties it chooses proper block orientation, texture, texture orientation, glowing.
 */
public class MarkBakedModel implements BakedModel {
    public static final Vector3f ROTATION_ORIGIN = new Vector3f(0.5f, 0.5f, 0.5f);
    public static final FaceBakery BAKERY = new FaceBakery();
    public static final ModelState TRANSFORM = new ModelState() {
        @Override
        public @NotNull Transformation getRotation() {
            return Transformation.identity();
        }
    };

    public static final HashMap<Direction, Vector3f> FROM_COORDS;
    public static final HashMap<Direction, Vector3f> TO_COORDS;

    private final BakedModel baseModel;

    public MarkBakedModel(BakedModel baseModel){
        this.baseModel = baseModel;
    }

    static {
        FROM_COORDS = new HashMap<>();
        FROM_COORDS.put(Direction.DOWN, new Vector3f(0, 15.95f, 0));
        FROM_COORDS.put(Direction.UP, new Vector3f(0, 0, 0));
        FROM_COORDS.put(Direction.NORTH, new Vector3f(0, 0, 15.95f));
        FROM_COORDS.put(Direction.SOUTH, new Vector3f(0, 0, 0));
        FROM_COORDS.put(Direction.WEST, new Vector3f(15.95f, 0, 0));
        FROM_COORDS.put(Direction.EAST, new Vector3f(0, 0, 0));

        TO_COORDS = new HashMap<>();
        TO_COORDS.put(Direction.DOWN, new Vector3f(16, 16, 16));
        TO_COORDS.put(Direction.UP, new Vector3f(16, 0.05f, 16));
        TO_COORDS.put(Direction.NORTH, new Vector3f(16, 16, 16));
        TO_COORDS.put(Direction.SOUTH, new Vector3f(16, 16, 0.05f));
        TO_COORDS.put(Direction.WEST, new Vector3f(16, 16, 16));
        TO_COORDS.put(Direction.EAST, new Vector3f(0.05f, 16, 16));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return baseModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return baseModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return baseModel.isCustomRenderer();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return baseModel.getParticleIcon();
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return baseModel.getOverrides();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return List.of();
    }

    // --

    public BakedQuad bakeMarkQuad(Direction direction, @NotNull Mark mark) {
        TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
              .apply(mark.symbol().value().texture());

        int tintIndex = direction.get3DDataValue(); // Allow MarkBlockColor to tint each face independently
        BlockElementFace blockPartFace = new BlockElementFace(direction, tintIndex, "",
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
        BakedQuad bakedQuad = BAKERY.bakeQuad(from, to, blockPartFace, texture, direction, TRANSFORM, blockPartRotation, !mark.glowing());

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
