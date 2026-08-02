package io.github.mortuusars.chalk.advancements.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DyeColorPredicate(@NotNull List<DyeColor> colors) {
    public static final Codec<DyeColorPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DyeColor.CODEC.listOf().fieldOf("colors").forGetter(DyeColorPredicate::colors))
            .apply(instance, DyeColorPredicate::new));

    public boolean matches(DyeColor color) {
        if (colors.isEmpty()) {
            return true;
        }

        return colors.stream().anyMatch(c -> c.equals(color));
    }
}
