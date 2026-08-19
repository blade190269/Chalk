package io.github.mortuusars.chalk.advancements.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ColorPredicate(Optional<Integer> exact,
                             Optional<MinMaxBounds.Ints> red,
                             Optional<MinMaxBounds.Ints> green,
                             Optional<MinMaxBounds.Ints> blue,
                             Optional<MinMaxBounds.Ints> alpha) {
    public static final Codec<ColorPredicate> CODEC = RecordCodecBuilder.create(i -> i.group(
          Codec.INT.optionalFieldOf("exact").forGetter(ColorPredicate::exact),
          MinMaxBounds.Ints.CODEC.optionalFieldOf("red").forGetter(ColorPredicate::red),
          MinMaxBounds.Ints.CODEC.optionalFieldOf("green").forGetter(ColorPredicate::green),
          MinMaxBounds.Ints.CODEC.optionalFieldOf("blue").forGetter(ColorPredicate::blue),
          MinMaxBounds.Ints.CODEC.optionalFieldOf("alpha").forGetter(ColorPredicate::alpha)
    ).apply(i, ColorPredicate::new));

    public boolean matches(int colorARGB) {
        return (exact().isEmpty() || exact().get() == colorARGB)
              && (red().isEmpty() || red.get().matches(FastColor.ARGB32.red(colorARGB)))
              && (green().isEmpty() || green.get().matches(FastColor.ARGB32.green(colorARGB)))
              && (blue().isEmpty() || blue.get().matches(FastColor.ARGB32.blue(colorARGB)))
              && (alpha().isEmpty() || alpha.get().matches(FastColor.ARGB32.alpha(colorARGB)));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable Integer exact;
        @Nullable MinMaxBounds.Ints red;
        @Nullable MinMaxBounds.Ints green;
        @Nullable MinMaxBounds.Ints blue;
        @Nullable MinMaxBounds.Ints alpha;

        public Builder exact(@Nullable Integer exact) {
            this.exact = exact;
            return this;
        }

        public Builder red(@Nullable MinMaxBounds.Ints red) {
            this.red = red;
            return this;
        }

        public Builder green(@Nullable MinMaxBounds.Ints green) {
            this.green = green;
            return this;
        }

        public Builder blue(@Nullable MinMaxBounds.Ints blue) {
            this.blue = blue;
            return this;
        }

        public Builder alpha(@Nullable MinMaxBounds.Ints alpha) {
            this.alpha = alpha;
            return this;
        }

        public ColorPredicate build() {
            return new ColorPredicate(
                  Optional.ofNullable(exact),
                  Optional.ofNullable(red),
                  Optional.ofNullable(green),
                  Optional.ofNullable(blue),
                  Optional.ofNullable(alpha)
            );
        }
    }
}
