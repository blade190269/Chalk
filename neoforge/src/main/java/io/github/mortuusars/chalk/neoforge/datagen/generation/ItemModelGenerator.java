package io.github.mortuusars.chalk.neoforge.datagen.generation;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.data.ChalkColors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), Chalk.ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        Chalk.Items.CHALKS.forEach((color, item) -> {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item.get());
            singleTexture(id.getPath(), modLoc("item/chalk"), "layer0",
                  modLoc("item/" + id.getPath()));
        });

        for (DyeColor color : ChalkColors.COLORS.keySet()) {
            getBuilder("chalk_box_" + color)
                    .parent(getExistingFile(mcLoc("item/generated")))
                    .texture("layer0", "item/chalk_box")
                    .texture("layer1", "item/" + "chalk_box_" + color + "_chalk");
        }
    }
}
