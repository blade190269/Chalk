package io.github.mortuusars.chalk.neoforge.datagen.generation;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;

public class LootTables {
    public static class ChestLootTables implements LootTableSubProvider {
        private final HolderLookup.Provider provider;

        public ChestLootTables(HolderLookup.Provider provider) {
            this.provider = provider;
        }

        @Override
        public void generate(@NotNull BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
            consumer.accept(Chalk.LootTables.ABANDONED_MINESHAFT_CHALKS,
                    LootTable.lootTable().withPool(
                            LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(2))
                                    .add(chalkBox(List.of(
                                            chalkStack(DyeColor.WHITE, 23), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                                            ItemStack.EMPTY, chalkStack(DyeColor.LIGHT_GRAY, 0), ItemStack.EMPTY, chalkStack(DyeColor.RED, 17),
                                            ItemStack.EMPTY), 0)
                                            .setWeight(2))
                                    .add(chalkBox(List.of(
                                            ItemStack.EMPTY, chalkStack(DyeColor.LIGHT_GRAY, 0), ItemStack.EMPTY, ItemStack.EMPTY,
                                            chalkStack(DyeColor.WHITE, 0), ItemStack.EMPTY, ItemStack.EMPTY, chalkStack(DyeColor.LIGHT_BLUE, 6),
                                            new ItemStack(Items.GLOWSTONE_DUST, 7)), 3)
                                            .setWeight(2))
                                    .add(chalkBox(List.of(
                                            chalkStack(DyeColor.BLUE, 5), ItemStack.EMPTY, chalkStack(DyeColor.PINK, 50), ItemStack.EMPTY,
                                            ItemStack.EMPTY, ItemStack.EMPTY, chalkStack(DyeColor.YELLOW, 0), ItemStack.EMPTY,
                                            new ItemStack(Items.GLOW_INK_SAC)), 5)
                                            .setWeight(2))
                                    .add(chalkBox(List.of(
                                            ItemStack.EMPTY, chalkStack(DyeColor.LIGHT_GRAY, 0), ItemStack.EMPTY, ItemStack.EMPTY,
                                            ItemStack.EMPTY, ItemStack.EMPTY, chalkStack(DyeColor.GREEN, 14), ItemStack.EMPTY,
                                            ItemStack.EMPTY), 0)
                                            .setWeight(2))
                                    .add(chalk(DyeColor.WHITE, 10))
                                    .add(chalk(DyeColor.LIGHT_GRAY, 8))
                                    .add(chalk(DyeColor.BLACK, 4))
                                    .add(EmptyLootItem.emptyItem().setWeight(50))
                    ));

            consumer.accept(Chalk.LootTables.DESERT_PYRAMID_CHALKS,
                    LootTable.lootTable().withPool(
                            LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(2))
                                    .add(chalkBox(List.of(
                                            chalkStack(DyeColor.WHITE, 23), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                                            chalkStack(DyeColor.LIGHT_GRAY, 12), ItemStack.EMPTY, ItemStack.EMPTY, chalkStack(DyeColor.RED, 0),
                                            ItemStack.EMPTY), 0)
                                            .setWeight(2))
                                    .add(chalkBox(List.of(
                                            ItemStack.EMPTY, chalkStack(DyeColor.LIGHT_GRAY, 5), ItemStack.EMPTY, chalkStack(DyeColor.WHITE, 0),
                                            ItemStack.EMPTY, ItemStack.EMPTY, chalkStack(DyeColor.LIGHT_BLUE, 15), ItemStack.EMPTY,
                                            new ItemStack(Items.GLOWSTONE_DUST, 4)), 4)
                                            .setWeight(2))
                                    .add(chalkBox(List.of(
                                            chalkStack(DyeColor.BLUE, 5), ItemStack.EMPTY, chalkStack(DyeColor.ORANGE, 26), ItemStack.EMPTY,
                                            ItemStack.EMPTY, ItemStack.EMPTY, chalkStack(DyeColor.YELLOW, 0), ItemStack.EMPTY,
                                            ItemStack.EMPTY), 0)
                                            .setWeight(2))
                                    .add(chalk(DyeColor.WHITE, 5))
                                    .add(chalk(DyeColor.LIGHT_GRAY, 5))
                                    .add(chalk(DyeColor.GRAY, 5))
                                    .add(chalk(DyeColor.BLACK, 5))
                                    .add(chalk(DyeColor.YELLOW, 5))
                                    .add(chalk(DyeColor.LIGHT_BLUE, 5))
                                    .add(chalk(DyeColor.ORANGE, 5))
                                    .add(EmptyLootItem.emptyItem().setWeight(60))
                    ));

            consumer.accept(Chalk.LootTables.SIMPLE_DUNGEON_CHALKS,
                    LootTable.lootTable().withPool(
                            LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(2))
                                    .add(chalkBox(List.of(
                                            chalkStack(DyeColor.WHITE, 23), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                                            chalkStack(DyeColor.LIGHT_GRAY, 12), ItemStack.EMPTY, ItemStack.EMPTY, chalkStack(DyeColor.RED, 0),
                                            ItemStack.EMPTY), 0)
                                            .setWeight(2))
                                    .add(chalkBox(List.of(
                                            ItemStack.EMPTY, chalkStack(DyeColor.LIGHT_GRAY, 0), ItemStack.EMPTY, chalkStack(DyeColor.WHITE, 7),
                                            ItemStack.EMPTY, ItemStack.EMPTY, chalkStack(DyeColor.LIGHT_BLUE, 15), ItemStack.EMPTY,
                                            new ItemStack(Items.GLOWSTONE_DUST, 7)), 2)
                                            .setWeight(2))
                                    .add(chalkBox(List.of(
                                            chalkStack(DyeColor.BLUE, 5), ItemStack.EMPTY, chalkStack(DyeColor.PINK, 50), ItemStack.EMPTY,
                                            ItemStack.EMPTY, ItemStack.EMPTY, chalkStack(DyeColor.YELLOW, 0), ItemStack.EMPTY,
                                            new ItemStack(Items.GLOW_INK_SAC)), 5)
                                            .setWeight(2))
                                    .add(chalkBox(List.of(
                                            ItemStack.EMPTY, chalkStack(DyeColor.MAGENTA, 6), ItemStack.EMPTY, chalkStack(DyeColor.CYAN, 0),
                                            ItemStack.EMPTY, ItemStack.EMPTY, chalkStack(DyeColor.LIME, 4), ItemStack.EMPTY,
                                            ItemStack.EMPTY), 0)
                                            .setWeight(2))
                                    .add(chalk(DyeColor.WHITE, 5))
                                    .add(chalk(DyeColor.LIGHT_GRAY, 5))
                                    .add(chalk(DyeColor.BLACK, 5))
                                    .add(EmptyLootItem.emptyItem().setWeight(50))
                    ));

            consumer.accept(Chalk.LootTables.VILLAGE_CHALKS,
                    LootTable.lootTable().withPool(
                            LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(2))
                                    .add(chalk(DyeColor.WHITE, 5))
                                    .add(chalk(DyeColor.LIGHT_GRAY, 4))
                                    .add(chalk(DyeColor.GRAY, 3))
                                    .add(chalk(DyeColor.BLACK, 2))
                                    .add(chalk(DyeColor.YELLOW, 5))
                                    .add(chalk(DyeColor.LIGHT_BLUE, 5))
                                    .add(chalk(DyeColor.ORANGE, 5))
                                    .add(EmptyLootItem.emptyItem().setWeight(50))
                    ));
        }

        private LootPoolSingletonContainer.Builder<?> chalk(DyeColor color, int weight) {
            return LootItem.lootTableItem(Chalk.Items.getChalk(color)).setWeight(weight);
        }

        private LootPoolSingletonContainer.Builder<?> chalkBox(List<ItemStack> items, int glowAmount) {
            return LootItem.lootTableItem(Chalk.Items.CHALK_BOX.get())
                    .apply(SetComponentsFunction.setComponent(Chalk.DataComponents.CHALK_BOX_CONTENTS, new ChalkBoxContents(items, glowAmount)));
        }

        private ItemStack chalkStack(DyeColor color, int damage) {
            ItemStack itemStack = new ItemStack(Chalk.Items.getChalk(color));
            if (damage > 0) {
                itemStack.setDamageValue(damage);
            }
            return itemStack;
        }
    }
}
