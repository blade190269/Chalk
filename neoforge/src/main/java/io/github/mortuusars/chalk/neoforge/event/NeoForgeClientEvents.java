package io.github.mortuusars.chalk.neoforge.event;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.ChalkClient;
import io.github.mortuusars.chalk.client.gui.ChalkBoxScreen;
import io.github.mortuusars.chalk.client.gui.tooltip.ClientChalkBoxTooltip;
import io.github.mortuusars.chalk.client.render.ChalkMarkBakedModel;
import io.github.mortuusars.chalk.client.render.ChalkMarkBlockColor;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import java.util.function.Supplier;

@EventBusSubscriber(modid = Chalk.ID, value = Dist.CLIENT)
public class NeoForgeClientEvents {
    @SubscribeEvent
    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(Chalk.Items.CHALK_BOX.get(), ChalkClient.ItemModelOverrides.SELECTED_PROPERTY,
                  (stack, level, entity, damage) -> Chalk.Items.CHALK_BOX.get().getSelectedChalkColor(stack));
        });
    }

    @SubscribeEvent
    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Chalk.MenuTypes.CHALK_BOX.get(), ChalkBoxScreen::new);
    }

    @SubscribeEvent
    private static void modelBake(ModelEvent.ModifyBakingResult event) {
        Chalk.Blocks.MARKS.forEach((color, block) -> {
            for (BlockState blockState : block.get().getStateDefinition().getPossibleStates()) {
                ModelResourceLocation variantMRL = BlockModelShaper.stateToModelLocation(blockState);
                BakedModel existingModel = event.getModels().get(variantMRL);

                if (existingModel instanceof ChalkMarkBakedModel)
                    Chalk.LOGGER.warn("Tried to replace {} model twice", block);
                else if (existingModel != null) {
                    ChalkMarkBakedModel customModel = new ChalkMarkBakedModel(existingModel);
                    event.getModels().put(variantMRL, customModel);
                } else
                    Chalk.LOGGER.warn("{} model not found. ChalkMarkBakedModel would not be added for this blockstate.", variantMRL);
            }
        });
    }

    @SubscribeEvent
    private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(new ChalkMarkBlockColor(), Chalk.Blocks.MARKS.values()
              .stream()
              .map(Supplier::get)
              .toArray(Block[]::new));
    }

    @SubscribeEvent
    private static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ChalkBoxContents.class, ClientChalkBoxTooltip::new);
    }
}