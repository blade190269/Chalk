package io.github.mortuusars.chalk.mixin.chalk_dye_colors;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.mortuusars.chalk.world.item.ChalkItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DyedItemColor.class)
public class DyedItemColorMixin {
    @WrapOperation(method = "applyDyes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/DyeColor;getTextureDiffuseColor()I"))
    private static int onApplyDyes(DyeColor instance, Operation<Integer> original, @Local(argsOnly = true) ItemStack stack) {
        if (stack.getItem() instanceof ChalkItem chalkItem) {
            return chalkItem.getColorFromDye(stack, instance);
        }
        return original.call(instance);
    }
}
