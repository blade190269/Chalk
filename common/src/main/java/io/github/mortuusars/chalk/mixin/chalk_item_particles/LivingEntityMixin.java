package io.github.mortuusars.chalk.mixin.chalk_item_particles;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.mortuusars.chalk.world.item.ChalkItem;
import io.github.mortuusars.mortaar.util.color.Color;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @WrapOperation(method = "spawnItemParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void onSpawnItemParticle(Level instance, ParticleOptions particleData, double x, double y, double z,
                                     double xSpeed, double ySpeed, double zSpeed, Operation<Void> original,
                                     @Local(argsOnly = true) ItemStack stack) {
        if (stack.getItem() instanceof ChalkItem chalkItem) {
            int color = chalkItem.getTintColor(stack, 0);
            particleData = new DustParticleOptions(new Vector3f(Color.redF(color), Color.greenF(color), Color.blueF(color)), 2f);
        }

        original.call(instance, particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }
}
