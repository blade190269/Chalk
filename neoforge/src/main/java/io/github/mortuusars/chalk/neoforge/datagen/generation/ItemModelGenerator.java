package io.github.mortuusars.chalk.neoforge.datagen.generation;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

@SuppressWarnings("removal")
public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), Chalk.ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        Chalk.Items.CHALKS.forEach((color, item) -> {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item.get());
            singleTexture(id.getPath(), modLoc("item/chalk"), "layer0",
                  modLoc("item/deprecated/" + id.getPath()));
        });
    }
}
