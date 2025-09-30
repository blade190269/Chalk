package io.github.mortuusars.chalk;

import com.mojang.serialization.MapCodec;
import io.github.mortuusars.chalk.advancement.MarkDrawnTrigger;
import io.github.mortuusars.chalk.advancement.ConsecutiveSleepingTrigger;
import io.github.mortuusars.chalk.block.ChalkMarkBlock;
import io.github.mortuusars.chalk.client.Client;
import io.github.mortuusars.chalk.item.ChalkBoxItem;
import io.github.mortuusars.chalk.item.ChalkItem;
import io.github.mortuusars.chalk.item.component.ChalkBoxContents;
import io.github.mortuusars.chalk.loot.ChalkAddTableLootModifier;
import io.github.mortuusars.chalk.menu.ChalkBoxMenu;
import io.github.mortuusars.chalk.data.ChalkColors;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.function.Consumer;

@Mod("chalk")
public class Chalk {
    public static final String ID = "chalk";
    public static final Logger LOGGER = LogManager.getLogger();

    public Chalk(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.Common.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.Client.SPEC);

        Blocks.BLOCKS.register(modEventBus);
        Items.ITEMS.register(modEventBus);
        Menus.MENUS.register(modEventBus);
        DataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        LootModifiers.LOOT_MODIFIERS.register(modEventBus);
        SoundEvents.SOUND_EVENTS.register(modEventBus);
        CriteriaTriggers.CRITERIA_TRIGGERS.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            Client.registerConfigScreen(modContainer);
        }
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public static class Blocks {
        private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, ID);
        public static final HashMap<DyeColor, DeferredHolder<Block, ChalkMarkBlock>> MARKS = new HashMap<>();

        static {
            for (DyeColor color : ChalkColors.COLORS.keySet()) {
                MARKS.put(color, BLOCKS.register(color + "_chalk_mark",
                        () -> new ChalkMarkBlock(color, BlockBehaviour.Properties.of()
                                .mapColor(color)
                                .pushReaction(PushReaction.DESTROY)
                                .instabreak()
                                .noOcclusion()
                                .noCollission()
                                .sound(SoundType.NETHER_WART))));
            }
        }

        public static ChalkMarkBlock getMarkBlock(DyeColor color) {
            return MARKS.get(color).get();
        }
    }

    public static class Items {
        private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Chalk.ID);

        public static HashMap<DyeColor, DeferredHolder<Item, ChalkItem>> CHALKS = new HashMap<>();

        public static final DeferredHolder<Item, ChalkBoxItem> CHALK_BOX = ITEMS.register("chalk_box",
                () -> new ChalkBoxItem(new Item.Properties()
                        .stacksTo(1)));

        static {
            for (DyeColor color : ChalkColors.COLORS.keySet()) {
                CHALKS.put(color, ITEMS.register(color + "_chalk", () -> new ChalkItem(color, new Item.Properties()
                        .stacksTo(1)
                        .durability(64))));
            }
        }

        public static ChalkItem getChalk(DyeColor color) {
            return CHALKS.get(color).get();
        }
    }

    public static class LootTables {
        public static final ResourceKey<LootTable> ABANDONED_MINESHAFT_CHALKS =
                ResourceKey.create(Registries.LOOT_TABLE, Chalk.resource("chests/abandoned_mineshaft_chalks"));
        public static final ResourceKey<LootTable> DESERT_PYRAMID_CHALKS =
                ResourceKey.create(Registries.LOOT_TABLE, Chalk.resource("chests/desert_pyramid_chalks"));
        public static final ResourceKey<LootTable> SIMPLE_DUNGEON_CHALKS =
                ResourceKey.create(Registries.LOOT_TABLE, Chalk.resource("chests/simple_dungeon_chalks"));
        public static final ResourceKey<LootTable> VILLAGE_CHALKS =
                ResourceKey.create(Registries.LOOT_TABLE, Chalk.resource("chests/village_chalks"));
    }

    public static class DataComponents {
        private static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Chalk.ID);

        public static final DataComponentType<ChalkBoxContents> CHALK_BOX_CONTENTS = register("chalk_box_contents",
                builder -> builder.persistent(ChalkBoxContents.CODEC).networkSynchronized(ChalkBoxContents.STREAM_CODEC).cacheEncoding());

        private static <T> DataComponentType<T> register(String name, Consumer<DataComponentType.Builder<T>> builderConsumer) {
            var builder = DataComponentType.<T>builder();
            builderConsumer.accept(builder);
            var componentType = builder.build();
            DATA_COMPONENT_TYPES.register(name, () -> componentType);
            return componentType;
        }
    }

    public static class LootModifiers {
        private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
                DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Chalk.ID);

        public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ChalkAddTableLootModifier>> ADD_TABLE =
                LOOT_MODIFIERS.register("add_table", () -> ChalkAddTableLootModifier.CODEC);
    }

    public static class Tags {
        public static final class Items {
            public static final TagKey<Item> CHALKS = ItemTags.create(Chalk.resource("chalks"));
            public static final TagKey<Item> C_CHALKS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "chalks"));
            public static final TagKey<Item> GLOWINGS = ItemTags.create(Chalk.resource("glowings"));
        }

        public static final class Blocks {
            public static final TagKey<Block> CHALK_MARKS = BlockTags.create(Chalk.resource("chalk_marks"));
            public static final TagKey<Block> CHALK_CANNOT_DRAW_ON = BlockTags.create(Chalk.resource("chalk_cannot_draw_on"));
        }
    }

    public static class Menus {
        private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Chalk.ID);

        public static final DeferredHolder<MenuType<?>, MenuType<ChalkBoxMenu>> CHALK_BOX = MENUS.register("chalk_box",
                () -> IMenuTypeExtension.create(ChalkBoxMenu::fromBuffer));
    }

    public static class CriteriaTriggers {
        private static final DeferredRegister<CriterionTrigger<?>> CRITERIA_TRIGGERS =
                DeferredRegister.create(Registries.TRIGGER_TYPE, Chalk.ID);

        public static final DeferredHolder<CriterionTrigger<?>, ConsecutiveSleepingTrigger> CONSECUTIVE_SLEEPING =
                CRITERIA_TRIGGERS.register("consecutive_sleeping", ConsecutiveSleepingTrigger::new);
        public static final DeferredHolder<CriterionTrigger<?>, MarkDrawnTrigger> MARK_DRAWN =
                CRITERIA_TRIGGERS.register("mark_drawn", MarkDrawnTrigger::new);
    }

    public static class SoundEvents {
        private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Chalk.ID);

        public static final DeferredHolder<SoundEvent, SoundEvent> CHALK_BOX_CHANGE = SOUND_EVENTS.register("item.chalk_box_change",
                () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.chalk_box_change")));
        public static final DeferredHolder<SoundEvent, SoundEvent> CHALK_BOX_OPEN = SOUND_EVENTS.register("item.chalk_box_open",
                () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.chalk_box_open")));
        public static final DeferredHolder<SoundEvent, SoundEvent> CHALK_BOX_CLOSE = SOUND_EVENTS.register("item.chalk_box_close",
                () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.chalk_box_close")));
        public static final DeferredHolder<SoundEvent, SoundEvent> MARK_DRAW = SOUND_EVENTS.register("item.chalk_draw",
                () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.chalk_draw")));
        public static final DeferredHolder<SoundEvent, SoundEvent> GLOW_APPLIED = SOUND_EVENTS.register("item.glow_applied",
                () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.glow_applied")));
        public static final DeferredHolder<SoundEvent, SoundEvent> GLOWING = SOUND_EVENTS.register("ambient.glowing",
                () -> SoundEvent.createVariableRangeEvent(Chalk.resource("ambient.glowing")));
        public static final DeferredHolder<SoundEvent, SoundEvent> MARK_REMOVED = SOUND_EVENTS.register("block.mark_removed",
                () -> SoundEvent.createVariableRangeEvent(Chalk.resource("block.mark_removed")));
    }
}
