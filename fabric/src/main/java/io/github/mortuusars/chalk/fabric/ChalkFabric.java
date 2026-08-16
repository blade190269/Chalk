package io.github.mortuusars.chalk.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.event.CommonEvents;
import io.github.mortuusars.chalk.world.item.OldChalkItem;
import io.github.mortuusars.chalk.network.fabric.FabricC2SPackets;
import io.github.mortuusars.chalk.network.fabric.FabricS2CPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.neoforged.fml.config.ModConfig;

import java.util.function.Supplier;

public class ChalkFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Chalk.init();

        NeoForgeConfigRegistry.INSTANCE.register(Chalk.ID, ModConfig.Type.SERVER, Config.Server.SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(Chalk.ID, ModConfig.Type.COMMON, Config.Common.SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(Chalk.ID, ModConfig.Type.CLIENT, Config.Client.SPEC);
        NeoForgeModConfigEvents.loading(Chalk.ID).register(config -> {
            if (config.getType() == ModConfig.Type.SERVER) {
                Config.Server.loading();
            }
        });
        NeoForgeModConfigEvents.reloading(Chalk.ID).register(config -> {
            if (config.getType() == ModConfig.Type.SERVER) {
                Config.Server.reloading();
            }
        });

        CommonEvents.commonSetup();

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(event -> {
            for (Supplier<OldChalkItem> item : Chalk.Items.CHALKS.values()) {
                event.accept(item.get());
            }
            event.accept(Chalk.Items.CHALK_BOX.get());
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            PlatformImpl.server = server;
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            PlatformImpl.server = null;
        });

        LootTableEvents.MODIFY.register(ChalkFabric::modifyLoot);

        FabricC2SPackets.register();
        FabricS2CPackets.register();
    }

    private static void modifyLoot(ResourceKey<LootTable> tableKey, LootTable.Builder builder,
                                   LootTableSource source, HolderLookup.Provider provider) {
        if (!Config.Common.LOOT.get()) {
            return;
        }

        if (BuiltInLootTables.ABANDONED_MINESHAFT.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                  .add(NestedLootTable.lootTableReference(Chalk.LootTables.ABANDONED_MINESHAFT_CHALKS))
                  .build());
        }
        if (BuiltInLootTables.DESERT_PYRAMID.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                  .add(NestedLootTable.lootTableReference(Chalk.LootTables.DESERT_PYRAMID_CHALKS))
                  .build());
        }
        if (BuiltInLootTables.VILLAGE_CARTOGRAPHER.equals(tableKey)
              || BuiltInLootTables.VILLAGE_MASON.equals(tableKey)
              || BuiltInLootTables.VILLAGE_PLAINS_HOUSE.equals(tableKey)
              || BuiltInLootTables.VILLAGE_SAVANNA_HOUSE.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                  .add(NestedLootTable.lootTableReference(Chalk.LootTables.VILLAGE_CHALKS))
                  .build());
        }
        if (BuiltInLootTables.SIMPLE_DUNGEON.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                  .add(NestedLootTable.lootTableReference(Chalk.LootTables.SIMPLE_DUNGEON_CHALKS))
                  .build());
        }
        if (BuiltInLootTables.SPAWN_BONUS_CHEST.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                  .add(NestedLootTable.lootTableReference(Chalk.LootTables.VILLAGE_CHALKS))
                  .build());
        }
    }
}
