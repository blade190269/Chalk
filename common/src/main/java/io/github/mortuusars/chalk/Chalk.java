package io.github.mortuusars.chalk;

import io.github.mortuusars.chalk.advancements.trigger.MarkDrawnTrigger;
import io.github.mortuusars.chalk.advancements.trigger.ConsecutiveSleepingTrigger;
import io.github.mortuusars.chalk.advancements.trigger.MarkGlowingTrigger;
import io.github.mortuusars.chalk.world.block.MarkBlockEntity;
import io.github.mortuusars.chalk.world.block.MarkBlock;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.chalk.world.item.ChalkBoxItem;
import io.github.mortuusars.chalk.world.item.ChalkItem;
import io.github.mortuusars.chalk.world.inventory.ChalkBoxMenu;
import io.github.mortuusars.chalk.data.ChalkColors;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Chalk {
    public static final String ID = "chalk";
    public static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        Blocks.init();
        BlockEntityTypes.init();
        Items.init();
        DataComponents.init();
        CriteriaTriggers.init();
        MenuTypes.init();
        SoundEvents.init();
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public static class Blocks {
        public static final Supplier<MarkBlock> MARK = Register.block("mark",
              () -> new MarkBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.DESTROY)
                    .replaceable()
                    .instabreak()
                    .noOcclusion()
                    .noCollission()
                    .noTerrainParticles()
                    .sound(SoundType.NETHER_WART)));

        public static final Map<DyeColor, Supplier<MarkBlock>> MARKS = new LinkedHashMap<>();

//        static {
//            for (DyeColor color : ChalkColors.COLORS.keySet()) {
//                MARKS.put(color, Register.block(color + "_chalk_mark",
//                        () -> new NewChalkMarkBlock(BlockBehaviour.Properties.of()
//                                .mapColor(color)
//                                .pushReaction(PushReaction.DESTROY)
//                                .instabreak()
//                                .noOcclusion()
//                                .noCollission()
//                                .sound(SoundType.NETHER_WART))));
//            }
//        }

        public static MarkBlock getMarkBlock(DyeColor color) {
            return MARK.get();
        }

        static void init() {
        }
    }

    public static class BlockEntityTypes {
        public static final Supplier<BlockEntityType<MarkBlockEntity>> CHALK_MARK = Register.blockEntityType("chalk_mark",
              () -> Register.newBlockEntityType(MarkBlockEntity::new, Blocks.MARK.get()));

        static void init() {
        }
    }

    public static class Items {
        public static Map<DyeColor, Supplier<ChalkItem>> CHALKS = new LinkedHashMap<>();

        public static final Supplier<ChalkBoxItem> CHALK_BOX = Register.item("chalk_box",
              () -> new ChalkBoxItem(new Item.Properties()
                    .stacksTo(1)));

        static {
            for (DyeColor color : ChalkColors.COLORS.keySet()) {
                CHALKS.put(color, Register.item(color + "_chalk", () -> new ChalkItem(color, new Item.Properties()
                      .stacksTo(1)
                      .durability(64))));
            }
        }

        public static ChalkItem getChalk(DyeColor color) {
            return CHALKS.get(color).get();
        }

        static void init() {
        }
    }

    public static class DataComponents {
        public static final DataComponentType<ChalkBoxContents> CHALK_BOX_CONTENTS = Register.dataComponentType("chalk_box_contents",
              builder -> builder.persistent(ChalkBoxContents.CODEC).networkSynchronized(ChalkBoxContents.STREAM_CODEC).cacheEncoding());

        static void init() {
        }
    }

    public static class MenuTypes {
        public static final Supplier<MenuType<ChalkBoxMenu>> CHALK_BOX = Register.menuType("chalk_box", ChalkBoxMenu::fromNetwork);

        static void init() {
        }
    }

    public static class CriteriaTriggers {
        public static final Supplier<ConsecutiveSleepingTrigger> CONSECUTIVE_SLEEPING =
              Register.criterionTrigger("consecutive_sleeping", ConsecutiveSleepingTrigger::new);
        public static final Supplier<MarkDrawnTrigger> MARK_DRAWN =
              Register.criterionTrigger("mark_drawn", MarkDrawnTrigger::new);
        public static final Supplier<MarkGlowingTrigger> MARK_GLOWING =
              Register.criterionTrigger("mark_glowing", MarkGlowingTrigger::new);

        static void init() {
        }
    }

    public static class SoundEvents {
        public static final Supplier<SoundEvent> CHALK_BOX_CHANGE = Register.soundEvent("item.chalk_box_change",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.chalk_box_change")));
        public static final Supplier<SoundEvent> CHALK_BOX_OPEN = Register.soundEvent("item.chalk_box_open",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.chalk_box_open")));
        public static final Supplier<SoundEvent> CHALK_BOX_CLOSE = Register.soundEvent("item.chalk_box_close",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.chalk_box_close")));
        public static final Supplier<SoundEvent> MARK_DRAW = Register.soundEvent("item.chalk_draw",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.chalk_draw")));
        public static final Supplier<SoundEvent> GLOW_APPLIED = Register.soundEvent("item.glow_applied",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.glow_applied")));
        public static final Supplier<SoundEvent> GLOWING = Register.soundEvent("ambient.glowing",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("ambient.glowing")));
        public static final Supplier<SoundEvent> MARK_REMOVED = Register.soundEvent("block.mark_removed",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("block.mark_removed")));

        static void init() {
        }
    }

    public static class Registries {
        public static final ResourceKey<Registry<MarkSymbol>> MARK_SYMBOL = ResourceKey.createRegistryKey(resource("mark_symbol"));
    }

    public static class LootTables {
        public static final ResourceKey<LootTable> ABANDONED_MINESHAFT_CHALKS =
              ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Chalk.resource("chests/abandoned_mineshaft_chalks"));
        public static final ResourceKey<LootTable> DESERT_PYRAMID_CHALKS =
              ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Chalk.resource("chests/desert_pyramid_chalks"));
        public static final ResourceKey<LootTable> SIMPLE_DUNGEON_CHALKS =
              ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Chalk.resource("chests/simple_dungeon_chalks"));
        public static final ResourceKey<LootTable> VILLAGE_CHALKS =
              ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Chalk.resource("chests/village_chalks"));
    }

    public static class Tags {
        public static final class Items {
            public static final TagKey<Item> CHALKS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Chalk.resource("chalks"));
            public static final TagKey<Item> C_CHALKS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "chalks"));
            public static final TagKey<Item> GLOWINGS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Chalk.resource("glowings"));

        }

        public static final class Blocks {
            public static final TagKey<Block> CHALK_MARKS = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Chalk.resource("chalk_marks"));
            public static final TagKey<Block> CHALK_CANNOT_DRAW_ON = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Chalk.resource("chalk_cannot_draw_on"));
        }
    }
}
