package io.github.mortuusars.chalk.world.chalk.symbol;

import com.mojang.serialization.Codec;
import io.github.mortuusars.chalk.core.component.Point2d;
import io.github.mortuusars.chalk.utils.GridCell;
import io.github.mortuusars.chalk.utils.PositionUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public enum SymbolOrientation implements StringRepresentable {
    CENTER("center", 0),
    NORTH("north", 0),
    NORTHEAST("northeast", 45),
    EAST("east", 90),
    SOUTHEAST("southeast", 135),
    SOUTH("south", 180),
    SOUTHWEST("southwest", 225),
    WEST("west", 270),
    NORTHWEST("northwest", 315);

    public static final Codec<SymbolOrientation> CODEC = StringRepresentable.fromEnum(SymbolOrientation::values);
    public static final StreamCodec<ByteBuf, SymbolOrientation> STREAM_CODEC = ByteBufCodecs.idMapper(
          ByIdMap.continuous(SymbolOrientation::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP), SymbolOrientation::ordinal);

    private final String name;
    private final int rotation;

    SymbolOrientation(String name, int rotation) {
        this.name = name;
        this.rotation = rotation;
    }

    public static SymbolOrientation fromCell(GridCell cell) {
        return switch (cell) {
            case TOP_LEFT -> NORTHWEST;
            case TOP -> NORTH;
            case TOP_RIGHT -> NORTHEAST;
            case LEFT -> WEST;
            case CENTER -> CENTER;
            case RIGHT -> EAST;
            case BOTTOM_LEFT -> SOUTHWEST;
            case BOTTOM -> SOUTH;
            case BOTTOM_RIGHT -> SOUTHEAST;
        };
    }

    public String getName() {
        return name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return getName();
    }

    public int getRotation() {
        return rotation;
    }

    public SymbolOrientation rotate(Rotation rotation) {
        int step = switch (rotation) {
            case CLOCKWISE_90 -> 2;
            case CLOCKWISE_180 -> 4;
            case COUNTERCLOCKWISE_90 -> 6;
            default -> 0;
        };

        return values()[((this.getRotation() / 45 + step) % 8) + 1];
    }

    public static SymbolOrientation fromRotation(int degrees) {
        for (SymbolOrientation orientation : values()) {
            if (orientation == CENTER) continue;

            if (orientation.getRotation() == degrees)
                return orientation;
        }

        return CENTER;
    }

    /**
     * Gets the cardinal direction (NORTH, EAST, SOUTH, WEST) from the click location.
     */
    public static SymbolOrientation fromClickLocationCardinal(Vec3 clickLocation, Direction face) {
        Point2d coords = PositionUtils.getClickedBlockSpaceCoords(clickLocation, face);

        final double x = 0.5d - coords.x();
        final double y = 0.5d - coords.y();

        final double radians = Math.atan2(y, x);
        double degrees = radians * (180 / Math.PI);
        degrees = (degrees + 270) % 360; // Adjust so the 0 is NORTH.

        int region = (int)((degrees + 45) % 360) / 90;
        return fromRotation(region * 90);
    }
}
