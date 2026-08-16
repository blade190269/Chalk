package io.github.mortuusars.chalk.integration.jei;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ChalkSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
    INSTANCE;

    @Override
    public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
        return ingredient.get(DataComponents.DYED_COLOR);
    }

    @Override
    public @NotNull String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
        return "";
    }
}
