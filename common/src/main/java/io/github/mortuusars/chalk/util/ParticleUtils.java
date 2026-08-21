package io.github.mortuusars.chalk.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class ParticleUtils {
    /**
     * Spawns a particle with slight random offset to each. Includes velocity.
     */
    public static void spawnParticle(Level level, ParticleOptions particleType, Vector3f position, Vector3f velocity, int count){
        if (!level.isClientSide() || count < 1)
            return;

        for (int i=0; i < count; i++ ){
            level.addParticle(particleType,
                    position.x() + ((level.getRandom().nextFloat() - 0.5f) * 0.3),
                    position.y() + ((level.getRandom().nextFloat() - 0.5f) * 0.3),
                    position.z() + ((level.getRandom().nextFloat() - 0.5f) * 0.3),
                    velocity.x(),
                    velocity.y(),
                    velocity.z());
        }
    }
}
