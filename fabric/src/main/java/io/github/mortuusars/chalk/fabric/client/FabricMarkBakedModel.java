package io.github.mortuusars.chalk.fabric.client;

import io.github.mortuusars.chalk.client.render.MarkBakedModel;
import io.github.mortuusars.chalk.world.block.MarkBlockEntity;
import io.github.mortuusars.chalk.world.chalk.MarkSet;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.function.Supplier;

public class FabricMarkBakedModel extends MarkBakedModel {
    private static final Renderer RENDERER = RendererAccess.INSTANCE.getRenderer();
    private static final RenderMaterial STANDARD_MATERIAL = Objects.requireNonNull(RENDERER).materialFinder().shadeMode(ShadeMode.VANILLA).find();

    public FabricMarkBakedModel(BakedModel baseModel) {
        super(baseModel);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        QuadEmitter emitter = context.getEmitter();

        if (blockView.getBlockEntity(pos) instanceof MarkBlockEntity blockEntity) {
            MarkSet marks = blockEntity.getMarks();
            if (!marks.isEmpty()) {
                marks.forEach((direction, mark) -> {
                    emitter.fromVanilla(bakeMarkQuad(direction, mark), STANDARD_MATERIAL, null);
                    emitter.emit();
                });
            }
        }
    }
}