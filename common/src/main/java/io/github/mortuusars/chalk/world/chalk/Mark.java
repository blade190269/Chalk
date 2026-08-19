package io.github.mortuusars.chalk.world.chalk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.chalk.symbol.SymbolOrientation;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.FastColor;

public record Mark(Holder<MarkSymbol> symbol, int color, SymbolOrientation orientation, boolean glowing) {
    public static final Codec<Mark> CODEC = RecordCodecBuilder.create(i -> i.group(
          RegistryFixedCodec.create(Chalk.Registries.MARK_SYMBOL).fieldOf("symbol").forGetter(Mark::symbol),
          Codec.INT.optionalFieldOf("color", 0xFFFFFFFF).forGetter(Mark::color),
          SymbolOrientation.CODEC.optionalFieldOf("orientation", SymbolOrientation.CENTER).forGetter(Mark::orientation),
          Codec.BOOL.optionalFieldOf("glowing", false).forGetter(Mark::glowing)
    ).apply(i, Mark::new));

    public Mark withSymbol(Holder<MarkSymbol> symbol) {
        return new Mark(symbol, color, orientation, glowing);
    }

    public Mark withColor(int color) {
        return new Mark(symbol, color, orientation, glowing);
    }

    public Mark lerpColor(int targetColor, float delta) {
        return withColor(FastColor.ARGB32.lerp(delta, color, targetColor));
    }

    public Mark withOrientation(SymbolOrientation orientation) {
        return new Mark(symbol, color, orientation, glowing);
    }

    public Mark withGlowing(boolean glowing) {
        return new Mark(symbol, color, orientation, glowing);
    }

    public boolean isSameMarkDifferentColors(Mark other) {
        return other.symbol().equals(symbol())
              && other.orientation() == orientation()
              && other.color() != color();
    }
}
