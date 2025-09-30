package io.github.mortuusars.chalk.data.generation;

public class ModItemTagsProvider /*extends ItemTagsProvider*/ {
    /*public ModItemTagsProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), lookupProvider, blockTagsProvider.contentsGetter(), Chalk.ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(Chalk.Tags.Items.GLOWINGS)
                .add(Items.GLOW_INK_SAC)
                .add(Items.GLOWSTONE_DUST);

        Chalk.Items.CHALKS.forEach((color, item) -> {
            tag(Chalk.Tags.Items.CHALKS).add(item.get());
            tag(Chalk.Tags.Items.C_CHALKS).add(item.get());
        });

        for (DyeColor color : ChalkColors.COLORS.keySet()){
            tag(color.getTag()).add(Chalk.Items.getChalk(color));
        }
    }*/
}
