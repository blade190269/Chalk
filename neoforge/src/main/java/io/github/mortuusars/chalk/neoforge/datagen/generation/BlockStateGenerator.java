package io.github.mortuusars.chalk.neoforge.datagen.generation;


import io.github.mortuusars.chalk.Chalk;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class BlockStateGenerator extends BlockStateProvider {
    public BlockStateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen.getPackOutput(), Chalk.ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        Chalk.Blocks.MARKS.forEach((color, block) ->
                simpleBlock(block.get(), models().getExistingFile(Chalk.resource("block/chalk_mark"))));
    }
}
