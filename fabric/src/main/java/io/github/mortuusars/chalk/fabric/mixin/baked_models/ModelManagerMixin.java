package io.github.mortuusars.chalk.fabric.mixin.baked_models;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.fabric.client.FabricMarkBakedModel;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
    @WrapOperation(method = "loadModels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;getBakedTopLevelModels()Ljava/util/Map;"))
    private Map<ModelResourceLocation, BakedModel> onLoadModels(ModelBakery instance, Operation<Map<ModelResourceLocation, BakedModel>> original) {
        Map<ModelResourceLocation, BakedModel> models = original.call(instance);

        for (BlockState blockState : Chalk.Blocks.MARK.get().getStateDefinition().getPossibleStates()) {
            ModelResourceLocation variantMRL = BlockModelShaper.stateToModelLocation(blockState);
            BakedModel existingModel = models.get(variantMRL);

            if (existingModel instanceof FabricMarkBakedModel)
                Chalk.LOGGER.warn("Tried to replace {} model twice", blockState);
            else if (existingModel != null) {
                FabricMarkBakedModel customModel = new FabricMarkBakedModel(existingModel);
                models.put(variantMRL, customModel);
            } else
                Chalk.LOGGER.warn("{} model not found. MarkBakedModel would not be added for this blockstate.", variantMRL);
        }

        return models;
    }
}
