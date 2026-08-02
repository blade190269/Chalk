package io.github.mortuusars.chalk.core;

import com.mojang.serialization.Codec;
import io.github.mortuusars.chalk.Chalk;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public enum MarkSymbol implements StringRepresentable {
    CENTER("center", false, OrientationBehavior.FIXED, SymbolOrientation.NORTH),
    ARROW("arrow", false, OrientationBehavior.FULL, SymbolOrientation.NORTH),
    CROSS("cross", true, OrientationBehavior.FIXED, SymbolOrientation.NORTH),
    CHECKMARK("check", true, OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH),
    SKULL("skull", true, OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH),
    HOUSE("house", true, OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH),
    HEART("heart", true, OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH),
    PICKAXE("pickaxe", true, OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH),;

    public static final Codec<MarkSymbol> CODEC = StringRepresentable.fromEnum(MarkSymbol::values);
    public static final IntFunction<MarkSymbol> BY_ID = ByIdMap.continuous(MarkSymbol::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, MarkSymbol> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, MarkSymbol::ordinal);

    private final String name;
    private final ResourceLocation textureLocation;
    private final boolean isSpecial;
    private final OrientationBehavior orientationBehavior;
    private final SymbolOrientation defaultOrientation;

    MarkSymbol(String name, boolean isSpecial, OrientationBehavior orientationBehavior, SymbolOrientation defaultOrientation) {
        this.name = name;
        this.textureLocation = Chalk.resource("block/mark/" + name);
        this.isSpecial = isSpecial;
        this.orientationBehavior = orientationBehavior;
        this.defaultOrientation = defaultOrientation;
    }

    public static List<MarkSymbol> getSpecialSymbols() {
        return Arrays.stream(values()).filter(s -> s.isSpecial).collect(Collectors.toList());
    }

    public static MarkSymbol byNameOrDefault(String name) {
        for (MarkSymbol symbol : values()) {
            if (symbol.name.equals(name))
                return symbol;
        }

        return CENTER;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public boolean isSpecial() {
        return isSpecial;
    }

    public OrientationBehavior getOrientationBehavior() {
        return orientationBehavior;
    }

    public SymbolOrientation getDefaultOrientation() {
        return defaultOrientation;
    }

    public String getTranslationKey() {
        return "gui." + Chalk.ID + ".symbol." + name;
    }

    public enum OrientationBehavior implements StringRepresentable {
        FIXED("fixed"),
        FULL("full"),
        CARDINAL("cardinal"),
        UP_DOWN_CARDINAL("up_down_cardinal");

        private final String name;

        OrientationBehavior(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
