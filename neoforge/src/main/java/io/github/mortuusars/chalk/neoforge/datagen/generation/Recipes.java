package io.github.mortuusars.chalk.neoforge.datagen.generation;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class Recipes extends RecipeProvider {
    public Recipes(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(generator.getPackOutput(), lookupProvider);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Chalk.Items.CHALK.get(), 1)
              .unlockedBy("has_calcite", has(Items.CALCITE))
              .requires(Items.CALCITE)
              .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Chalk.Items.CHALK_BOX.get())
                .unlockedBy("has_chalk", has(Chalk.Items.CHALK.get()))
                .unlockedBy("has_paper", has(Items.PAPER))
                .unlockedBy("has_slimeball", has(Tags.Items.SLIME_BALLS))
                .pattern("P P")
                .pattern("PSP")
                .pattern("PPP")
                .define('P', Items.PAPER)
                .define('S', Tags.Items.SLIME_BALLS)
                .save(recipeOutput);
    }
}
