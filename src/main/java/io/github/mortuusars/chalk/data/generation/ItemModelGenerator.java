package io.github.mortuusars.chalk.data.generation;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.data.ChalkColors;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.DyeColor;
//import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
//import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ItemModelGenerator /*extends ItemModelProvider*/ {
    /*public ItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), Chalk.ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        Chalk.Items.CHALKS.forEach((color, item) ->
                singleTexture(item.getId().getPath(), modLoc("item/chalk"), "layer0",
                        modLoc("item/" + item.getId().getPath())));

        for (DyeColor color : ChalkColors.COLORS.keySet()) {
            getBuilder("chalk_box_" + color)
                    .parent(getExistingFile(mcLoc("item/generated")))
                    .texture("layer0", "item/chalk_box")
                    .texture("layer1", "item/" + "chalk_box_" + color + "_chalk");
        }
    }*/
}
