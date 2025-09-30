package io.github.mortuusars.chalk.utils;

import io.github.mortuusars.chalk.data.ChalkColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.DyeColor;
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

    /**
     * Spawns a particle with slight random offset to each.
     */
    public static void spawnParticle(Level level, ParticleOptions particleType, Vector3f position, int count){
        spawnParticle(level, particleType, position, new Vector3f(0f, 0f, 0f), count);
    }

    /**
     * Spawns a color dust particles at the blockPos, close to the specified face.
     */
    public static void spawnColorDustParticles(DyeColor color, Level level, BlockPos pos, Direction face) {
        ParticleUtils.spawnParticle(level, new DustParticleOptions(ChalkColors.fromDyeColor(color), 2f),
                PositionUtils.blockCenterOffsetToFace(pos, face, 0.25f), 1);
    }
}
