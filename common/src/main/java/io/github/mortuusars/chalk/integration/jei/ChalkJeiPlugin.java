package io.github.mortuusars.chalk.integration.jei;

import io.github.mortuusars.chalk.Chalk;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class ChalkJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = Chalk.resource("jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(Chalk.Items.CHALK.get(), ChalkSubtypeInterpreter.INSTANCE);
    }
}