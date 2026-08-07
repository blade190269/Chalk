package io.github.mortuusars.chalk.advancements.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.world.chalk.Mark;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record MarkPredicate(Optional<List<ResourceLocation>> symbols, Optional<MinMaxBounds.Ints> color, Optional<Boolean> glowing) {
    public static final Codec<MarkPredicate> CODEC = RecordCodecBuilder.create(i -> i.group(
          ResourceLocation.CODEC.listOf().optionalFieldOf("symbols").forGetter(MarkPredicate::symbols),
          MinMaxBounds.Ints.CODEC.optionalFieldOf("color").forGetter(MarkPredicate::color),
          Codec.BOOL.optionalFieldOf("glowing").forGetter(MarkPredicate::glowing)
    ).apply(i, MarkPredicate::new));

    public boolean matches(Mark mark) {
        return (color.isEmpty() || color.get().matches(mark.color()))
              && (glowing.isEmpty() || glowing.get() == mark.glowing())
              && (symbols.isEmpty() || mark.symbol().unwrapKey()
                  .map(key -> symbols.get().contains(key.location()))
                  .orElse(false));
    }
}
