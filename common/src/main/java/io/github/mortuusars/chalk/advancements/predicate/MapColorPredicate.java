package io.github.mortuusars.chalk.advancements.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record MapColorPredicate(@NotNull List<MapColor> colors) {
    public static final Codec<MapColorPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.xmap(MapColor::byId, color -> color.id).listOf().fieldOf("colors").forGetter(MapColorPredicate::colors))
            .apply(instance, MapColorPredicate::new));

    public boolean matches(@NotNull MapColor color) {
        if (colors.isEmpty()) {
            return true;
        }

        return colors.stream().anyMatch(c -> c.equals(color));
    }
}
