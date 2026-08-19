package io.github.mortuusars.chalk.util;

public record Point2d(double x, double y) {
    public static final Point2d ZERO = new Point2d(0, 0);
}
