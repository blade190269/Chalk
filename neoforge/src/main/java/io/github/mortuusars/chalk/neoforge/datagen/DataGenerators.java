package io.github.mortuusars.chalk.neoforge.datagen;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.neoforge.datagen.generation.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Chalk.ID)
public class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Server
        ModBlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(generator, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(generator, lookupProvider, blockTagsProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new Recipes(generator, lookupProvider));
        generator.addProvider(event.includeServer(), new AdvancementProvider(output, lookupProvider, existingFileHelper, List.of(new Advancements())));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Collections.emptySet(), List.of(
              new LootTableProvider.SubProviderEntry(LootTables.ChestLootTables::new, LootContextParamSets.CHEST)), lookupProvider));

        DatapackBuiltinEntriesProvider datapackRegistries = new BuiltInDatapackEntries(output, lookupProvider);
        generator.addProvider(event.includeServer(), datapackRegistries);

        // Client
        generator.addProvider(event.includeClient(), new BlockStateGenerator(generator, existingFileHelper));
        generator.addProvider(event.includeClient(), new ItemModelGenerator(generator, existingFileHelper));
    }
}
