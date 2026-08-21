package io.github.mortuusars.chalk.neoforge.datagen.generation;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.advancements.trigger.MarkDrawnTrigger;
import io.github.mortuusars.chalk.advancements.trigger.ConsecutiveSleepingTrigger;
import io.github.mortuusars.chalk.advancements.trigger.MarkGlowingTrigger;
import io.github.mortuusars.chalk.world.item.ChalkItem;
import io.github.mortuusars.mortaar.advancement.predicate.ColorPredicate;
import io.github.mortuusars.mortaar.advancement.predicate.MapColorPredicate;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class Advancements implements AdvancementProvider.AdvancementGenerator {
    @SuppressWarnings("removal")
    @Override
    public void generate(HolderLookup.@NotNull Provider registries, @NotNull Consumer<AdvancementHolder> saver,
                         @NotNull ExistingFileHelper existingFileHelper) {
        Advancement.Builder.advancement()
              .parent(ResourceLocation.parse("minecraft:adventure/sleep_in_bed"))
              .display(Items.YELLOW_BED,
                    Component.translatable("advancement.chalk.home_is_where_the_bed_is.title"),
                    Component.translatable("advancement.chalk.home_is_where_the_bed_is.description"),
                    null, AdvancementType.TASK, true, true, false)
              .addCriterion("sleep_three_times_in_one_place", Chalk.CriteriaTriggers.CONSECUTIVE_SLEEPING.get().createCriterion(
                    new ConsecutiveSleepingTrigger.TriggerInstance(EntityPredicate.wrap(Optional.empty()),
                          Optional.empty(),
                          MinMaxBounds.Ints.atLeast(3),
                          DistancePredicate.absolute(MinMaxBounds.Doubles.atMost(16)))))
              .save(saver, Chalk.resource("adventure/home_is_where_the_bed_is"), existingFileHelper);

        AdvancementHolder thisWay = Advancement.Builder.advancement()
              .parent(ResourceLocation.parse("minecraft:adventure/root"))
              .display(ChalkItem.create(0xFFDB4A, 0),
                    Component.translatable("advancement.chalk.this_way.title"),
                    Component.translatable("advancement.chalk.this_way.description"),
                    null, AdvancementType.TASK, true, true, false)
              .requirements(AdvancementRequirements.Strategy.OR)
              .addCriterion("draw_in_mineshaft", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.MINESHAFT)))
              .addCriterion("draw_in_mineshaft_mesa", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.MINESHAFT_MESA)))
              .addCriterion("draw_in_fortress", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.FORTRESS)))
              .addCriterion("draw_in_stronghold", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.STRONGHOLD)))
              .addCriterion("draw_in_trial_chambers", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.TRIAL_CHAMBERS)))
              .save(saver, Chalk.resource("adventure/this_way"), existingFileHelper);

        AdvancementHolder vandalism = Advancement.Builder.advancement()
              .parent(thisWay)
              .display(ChalkItem.create(0xBDBFBE, 0),
                    Component.translatable("advancement.chalk.vandalism.title"),
                    Component.translatable("advancement.chalk.vandalism.description"),
                    null, AdvancementType.TASK, true, true, true)
              .requirements(AdvancementRequirements.Strategy.OR)
              .addCriterion("draw_in_village", MarkDrawnTrigger.TriggerInstance.structures(registries.lookupOrThrow(Registries.STRUCTURE).get(StructureTags.VILLAGE).get()))
              .rewards(AdvancementRewards.Builder.experience(50))
              .save(saver, Chalk.resource("adventure/vandalism"), existingFileHelper);

        AdvancementHolder guidingStar = Advancement.Builder.advancement()
              .parent(thisWay)
              .display(Items.GLOWSTONE_DUST,
                    Component.translatable("advancement.chalk.guiding_star.title"),
                    Component.translatable("advancement.chalk.guiding_star.description"),
                    null, AdvancementType.TASK, true, true, false)
              .addCriterion("make_glowing", Chalk.CriteriaTriggers.MARK_GLOWING.get().createCriterion(new MarkGlowingTrigger.TriggerInstance(EntityPredicate.wrap(Optional.empty()),
                    Optional.empty(),
                    Optional.of(LocationPredicate.Builder.location()
                          .setLight(new LightPredicate.Builder()
                                .setComposite(MinMaxBounds.Ints.atMost(7)))
                          .build()),
                    Optional.empty())))
              .save(saver, Chalk.resource("adventure/guiding_star"), existingFileHelper);

        AdvancementHolder consumedByTheLight = Advancement.Builder.advancement()
              .parent(guidingStar)
              .display(ChalkItem.create(0xFFFFFF, 0),
                    Component.translatable("advancement.chalk.consumed_by_the_light.title"),
                    Component.translatable("advancement.chalk.consumed_by_the_light.description"),
                    null, AdvancementType.TASK, true, true, false)
              .addCriterion("draw_white", Chalk.CriteriaTriggers.MARK_DRAWN.get().createCriterion(new MarkDrawnTrigger.TriggerInstance(EntityPredicate.wrap(Optional.empty()),
                    Optional.of(LocationPredicate.Builder.location()
                          .setLight(new LightPredicate.Builder()
                                .setComposite(MinMaxBounds.Ints.atLeast(11)))
                          .build()),
                    Optional.empty(),
                    Optional.of(new MapColorPredicate(List.of(MapColor.TERRACOTTA_WHITE, MapColor.SNOW, MapColor.QUARTZ))),
                    Optional.of(ColorPredicate.builder()
                          .red(MinMaxBounds.Ints.atLeast(200))
                          .green(MinMaxBounds.Ints.atLeast(200))
                          .blue(MinMaxBounds.Ints.atLeast(200))
                          .build()))))
              .save(saver, Chalk.resource("adventure/consumed_by_the_light"), existingFileHelper);

        AdvancementHolder aloneInTheDarkness = Advancement.Builder.advancement()
              .parent(consumedByTheLight)
              .display(ChalkItem.create(0x2C2D2E, 0),
                    Component.translatable("advancement.chalk.alone_in_the_darkness.title"),
                    Component.translatable("advancement.chalk.alone_in_the_darkness.description"),
                    null, AdvancementType.TASK, true, true, false)
              .addCriterion("draw_black", Chalk.CriteriaTriggers.MARK_DRAWN.get().createCriterion(new MarkDrawnTrigger.TriggerInstance(EntityPredicate.wrap(Optional.empty()),
                    Optional.of(LocationPredicate.Builder.location()
                          .setLight(new LightPredicate.Builder()
                                .setComposite(MinMaxBounds.Ints.atMost(7)))
                          .build()),
                    Optional.empty(),
                    Optional.of(new MapColorPredicate(List.of(MapColor.COLOR_BLACK))),
                    Optional.of(ColorPredicate.builder()
                          .red(MinMaxBounds.Ints.atMost(50))
                          .green(MinMaxBounds.Ints.atMost(50))
                          .blue(MinMaxBounds.Ints.atMost(50))
                          .build()))))
              .save(saver, Chalk.resource("adventure/alone_in_the_darkness"), existingFileHelper);
    }
}