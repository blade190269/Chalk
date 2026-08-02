package io.github.mortuusars.chalk;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ChalkClient {
    public static void init() {
        ItemModelOverrides.register();
    }

    // --

    public static class ItemModelOverrides {
        public static final ResourceLocation SELECTED_PROPERTY = Chalk.resource("selected_chalk");

        public static void register() {
            ItemProperties.register(Chalk.Items.CHALK_BOX.get(), SELECTED_PROPERTY, ItemModelOverrides::getSelectedChalkColor);
        }

        public static float getSelectedChalkColor(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return Chalk.Items.CHALK_BOX.get().getSelectedChalkColor(stack);
        }
    }
}
