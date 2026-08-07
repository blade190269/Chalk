package io.github.mortuusars.chalk.advancements.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.advancements.predicate.DyeColorPredicate;
import io.github.mortuusars.chalk.advancements.predicate.MapColorPredicate;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MarkDrawnTrigger extends SimpleCriterionTrigger<MarkDrawnTrigger.TriggerInstance> {
    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack drawingStack, MapColor surfaceColor, DyeColor color) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, drawingStack, surfaceColor, color));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                  Optional<LocationPredicate> location,
                                  Optional<ItemPredicate> item,
                                  Optional<MapColorPredicate> surfaceColor,
                                  Optional<DyeColorPredicate> markColor) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
              instance.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                          LocationPredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location),
                          ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item),
                          MapColorPredicate.CODEC.optionalFieldOf("surface_color").forGetter(TriggerInstance::surfaceColor),
                          DyeColorPredicate.CODEC.optionalFieldOf("mark_color").forGetter(TriggerInstance::markColor))
                    .apply(instance, TriggerInstance::new));

        public static Criterion<MarkDrawnTrigger.TriggerInstance> structure(Holder<Structure> structureKey) {
            TriggerInstance instance = new TriggerInstance(EntityPredicate.wrap(
                  Optional.empty()),
                  Optional.of(LocationPredicate.Builder.inStructure(structureKey).build()),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty());
            return Chalk.CriteriaTriggers.MARK_DRAWN.get().createCriterion(instance);
        }

        public static Criterion<MarkDrawnTrigger.TriggerInstance> structures(HolderSet<Structure> structures) {
            TriggerInstance instance = new TriggerInstance(EntityPredicate.wrap(
                  Optional.empty()),
                  Optional.of(new LocationPredicate(Optional.empty(),
                        Optional.empty(),
                        Optional.of(structures),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty())),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty());
            return Chalk.CriteriaTriggers.MARK_DRAWN.get().createCriterion(instance);
        }

        public boolean matches(ServerPlayer player, ItemStack item, MapColor surfaceColor, DyeColor markColor) {
            return (location.isEmpty() || location.get().matches(player.serverLevel(), player.getX(), player.getY(), player.getZ()))
                  && (this.item.isEmpty() || this.item.get().test(item))
                  && (this.surfaceColor.isEmpty() || this.surfaceColor.get().matches(surfaceColor))
                  && (this.markColor.isEmpty() || this.markColor.get().matches(markColor));
        }
    }
}

