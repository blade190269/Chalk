package io.github.mortuusars.chalk.data;

import net.minecraft.world.item.DyeColor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChalkColors {
    public static final List<DyeColor> ORDERED_DYE_COLORS = List.of(
          DyeColor.WHITE,
          DyeColor.LIGHT_GRAY,
          DyeColor.GRAY,
          DyeColor.BLACK,
          DyeColor.BROWN,
          DyeColor.RED,
          DyeColor.ORANGE,
          DyeColor.YELLOW,
          DyeColor.LIME,
          DyeColor.GREEN,
          DyeColor.CYAN,
          DyeColor.LIGHT_BLUE,
          DyeColor.BLUE,
          DyeColor.PURPLE,
          DyeColor.MAGENTA,
          DyeColor.PINK
    );

    public static final Map<DyeColor, Integer> COLORS = new LinkedHashMap<>() {{ // LinkedHasMap keeps order
        put(DyeColor.WHITE, 0xFFFFFF);
        put(DyeColor.LIGHT_GRAY, 0xADADA8);
        put(DyeColor.GRAY, 0x606466);
        put(DyeColor.BLACK, 0x252525);
        put(DyeColor.BROWN, 0x8A522A);
        put(DyeColor.RED, 0xEB4A39);
        put(DyeColor.ORANGE, 0xFF8034);
        put(DyeColor.YELLOW, 0xFFD929);
        put(DyeColor.LIME, 0x9AE437);
        put(DyeColor.GREEN, 0x51A80B);
        put(DyeColor.CYAN, 0x1DCAC0);
        put(DyeColor.LIGHT_BLUE, 0x82DBF8);
        put(DyeColor.BLUE, 0x3B50D2);
        put(DyeColor.PURPLE, 0xA74CD2);
        put(DyeColor.MAGENTA, 0xED60E2);
        put(DyeColor.PINK, 0xEE658E);
    }};

    public static int fromDyeColor(DyeColor color) {
        return COLORS.get(color);
    }
}
