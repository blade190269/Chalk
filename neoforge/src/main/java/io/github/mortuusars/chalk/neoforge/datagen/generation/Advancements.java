package io.github.mortuusars.chalk.neoforge.datagen.generation;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.advancements.MarkDrawnTrigger;
import io.github.mortuusars.chalk.advancements.ConsecutiveSleepingTrigger;
import io.github.mortuusars.chalk.advancements.predicate.DyeColorPredicate;
import io.github.mortuusars.chalk.advancements.predicate.MapColorPredicate;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
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
                .parent(ResourceLocation.parse("minecraft:adventure/kill_a_mob"))
                .display(Items.SKELETON_SKULL,
                        Component.translatable("advancement.chalk.bound_by_bone.title"),
                        Component.translatable("advancement.chalk.bound_by_bone.description"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("get_skull", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SKELETON_SKULL))
                .save(saver, Chalk.resource("adventure/bound_by_bone"), existingFileHelper);

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


        AdvancementHolder drawInStructure = Advancement.Builder.advancement()
                .parent(ResourceLocation.parse("minecraft:adventure/root"))
                .display(Chalk.Items.getChalk(DyeColor.YELLOW),
                        Component.translatable("advancement.chalk.this_way.title"),
                        Component.translatable("advancement.chalk.this_way.description"),
                        null, AdvancementType.TASK, true, true, false)
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("draw_in_mineshaft", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.MINESHAFT)))
                .addCriterion("draw_in_mineshaft_mesa", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.MINESHAFT_MESA)))
                .addCriterion("draw_in_fortress", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.FORTRESS)))
                .addCriterion("draw_in_stronghold", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.STRONGHOLD)))
                .save(saver, Chalk.resource("adventure/this_way"), existingFileHelper);

        Advancement.Builder.advancement()
                .parent(drawInStructure)
                .display(Chalk.Items.getChalk(DyeColor.LIGHT_GRAY),
                        Component.translatable("advancement.chalk.vandalism.title"),
                        Component.translatable("advancement.chalk.vandalism.description"),
                        null, AdvancementType.TASK, true, true, true)
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("draw_in_village_plains", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.VILLAGE_PLAINS)))
                .addCriterion("draw_in_village_desert", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.VILLAGE_DESERT)))
                .addCriterion("draw_in_village_savanna", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.VILLAGE_SAVANNA)))
                .addCriterion("draw_in_village_snowy", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.VILLAGE_SNOWY)))
                .addCriterion("draw_in_village_taiga", MarkDrawnTrigger.TriggerInstance.structure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.VILLAGE_TAIGA)))
                .rewards(AdvancementRewards.Builder.experience(50))
                .save(saver, Chalk.resource("adventure/vandalism"), existingFileHelper);

        Advancement.Builder.advancement()
                .parent(drawInStructure)
                .display(Chalk.Items.getChalk(DyeColor.BLACK),
                        Component.translatable("advancement.chalk.alone_in_the_darkness.title"),
                        Component.translatable("advancement.chalk.alone_in_the_darkness.description"),
                        null, AdvancementType.TASK, true, true, true)
                .addCriterion("draw_black", Chalk.CriteriaTriggers.MARK_DRAWN.get().createCriterion(new MarkDrawnTrigger.TriggerInstance(EntityPredicate.wrap(Optional.empty()),
                        Optional.of(LocationPredicate.Builder.location()
                                .setLight(new LightPredicate.Builder()
                                        .setComposite(MinMaxBounds.Ints.atMost(7)))
                                .build()),
                        Optional.of(new MapColorPredicate(List.of(MapColor.COLOR_BLACK))),
                        Optional.of(new DyeColorPredicate(List.of(DyeColor.BLACK))))))
                .rewards(AdvancementRewards.Builder.experience(100))
                .save(saver, Chalk.resource("adventure/alone_in_the_darkness"), existingFileHelper);
    }
}