package io.github.mortuusars.chalk.neoforge.datagen.generation;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Chalk.ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator dataGenerator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Server
        ModBlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(dataGenerator, lookupProvider, existingFileHelper);
        dataGenerator.addProvider(event.includeServer(), blockTagsProvider);
        dataGenerator.addProvider(event.includeServer(), new ModItemTagsProvider(dataGenerator, lookupProvider, blockTagsProvider, existingFileHelper));
        dataGenerator.addProvider(event.includeServer(), new Recipes(dataGenerator, lookupProvider));
        dataGenerator.addProvider(event.includeServer(),
                (DataProvider.Factory<AdvancementProvider>) output ->
                        new AdvancementProvider(output, lookupProvider, existingFileHelper, List.of(new Advancements())));
        dataGenerator.addProvider(event.includeServer(), (DataProvider.Factory<LootTableProvider>) output ->
                new LootTableProvider(output, Collections.emptySet(),
                        List.of(new LootTableProvider.SubProviderEntry(LootTables.ChestLootTables::new, LootContextParamSets.CHEST)),
                        lookupProvider));

        // Client
        dataGenerator.addProvider(event.includeClient(), new BlockStateGenerator(dataGenerator, existingFileHelper));
        dataGenerator.addProvider(event.includeClient(), new ItemModelGenerator(dataGenerator, existingFileHelper));
    }
}
