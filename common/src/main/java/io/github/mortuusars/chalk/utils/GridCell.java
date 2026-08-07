package io.github.mortuusars.chalk.utils;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public enum GridCell implements StringRepresentable {
    TOP_LEFT("top_left"),
    TOP("top"),
    TOP_RIGHT("top_right"),
    LEFT("left"),
    CENTER("center"),
    RIGHT("right"),
    BOTTOM_LEFT("bottom_left"),
    BOTTOM("bottom"),
    BOTTOM_RIGHT("bottom_right");

    private static final GridCell[] VALUES = values();

    public static final Codec<GridCell> CODEC = StringRepresentable.fromEnum(GridCell::values);
    public static final StreamCodec<ByteBuf, GridCell> STREAM_CODEC = ByteBufCodecs.idMapper(
          ByIdMap.continuous(GridCell::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP), GridCell::ordinal);

    private final String name;

    GridCell(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    /**
     * Returns the cell of a 3x3 grid depending on where the block was clicked.
     */
    public static GridCell fromClickLocation(Vec3 clickLocation, Direction face) {
        Point2d coords = PositionUtils.getClickedBlockSpaceCoords(clickLocation, face);
        int x = Mth.clamp((int) (coords.x() * 3), 0, 2);
        int y = Mth.clamp((int) (coords.y() * 3), 0, 2);
        return VALUES[y * 3 + x];
    }
}
