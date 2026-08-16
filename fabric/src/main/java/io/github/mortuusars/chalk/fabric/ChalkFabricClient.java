package io.github.mortuusars.chalk.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.ChalkClient;
import io.github.mortuusars.chalk.client.gui.screens.ChalkBoxScreen;
import io.github.mortuusars.chalk.client.render.MarkBlockColor;
import io.github.mortuusars.chalk.network.fabric.FabricS2CPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class ChalkFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ChalkClient.init();

        ConfigScreenFactoryRegistry.INSTANCE.register(Chalk.ID, ConfigurationScreen::new);

        ColorProviderRegistry.BLOCK.register(new MarkBlockColor(), Chalk.Blocks.MARK.get());

        MenuScreens.register(Chalk.MenuTypes.CHALK_BOX.get(), ChalkBoxScreen::new);

        FabricS2CPacketHandler.register();
    }
}
