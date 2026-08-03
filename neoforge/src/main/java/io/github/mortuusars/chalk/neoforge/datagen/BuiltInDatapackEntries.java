package io.github.mortuusars.chalk.neoforge.datagen;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class BuiltInDatapackEntries extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder REGISTRIES = new RegistrySetBuilder()
          .add(Chalk.Registries.MARK_SYMBOL, MarkSymbol::bootstrap);

    public BuiltInDatapackEntries(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, REGISTRIES, Set.of(Chalk.ID));
    }
}