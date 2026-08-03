package io.github.mortuusars.chalk.utils;

import io.github.mortuusars.chalk.core.component.Point2d;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public enum GridCell {
    TOP_LEFT,
    TOP,
    TOP_RIGHT,
    LEFT,
    CENTER,
    RIGHT,
    BOTTOM_LEFT,
    BOTTOM,
    BOTTOM_RIGHT;

    private static final GridCell[] VALUES = values();

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
