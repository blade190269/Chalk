package io.github.mortuusars.chalk.world.chalk.symbol;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.Chalk;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.*;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record MarkSymbol(ResourceLocation texture, int rotationOffset, OrientationBehavior orientationBehavior) {
    public static final ResourceKey<MarkSymbol> ARROW =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("arrow"));
    public static final ResourceKey<MarkSymbol> CENTER =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("center"));

    public static final ResourceKey<MarkSymbol> CHECKMARK =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("check"));
    public static final ResourceKey<MarkSymbol> CROSS =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("cross"));
    public static final ResourceKey<MarkSymbol> HOUSE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("house"));
    public static final ResourceKey<MarkSymbol> PICKAXE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("pickaxe"));
    public static final ResourceKey<MarkSymbol> HEART =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("heart"));
    public static final ResourceKey<MarkSymbol> SKULL =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("skull"));

    public static final ResourceKey<MarkSymbol> DEFAULT = CENTER;

    // --

    public static final Codec<MarkSymbol> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
          ResourceLocation.CODEC.fieldOf("texture").forGetter(MarkSymbol::texture),
          Codec.intRange(-180, 180).optionalFieldOf("rotation_offset", 0).forGetter(MarkSymbol::rotationOffset),
          OrientationBehavior.CODEC.optionalFieldOf("orientation_behavior", OrientationBehavior.FULL).forGetter(MarkSymbol::orientationBehavior)
    ).apply(i, MarkSymbol::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarkSymbol> DIRECT_STREAM_CODEC = StreamCodec.composite(
          ResourceLocation.STREAM_CODEC, MarkSymbol::texture,
          ByteBufCodecs.INT, MarkSymbol::rotationOffset,
          OrientationBehavior.STREAM_CODEC, MarkSymbol::orientationBehavior,
          MarkSymbol::new
    );

    public static final Codec<Holder<MarkSymbol>> CODEC =
          RegistryFileCodec.create(Chalk.Registries.MARK_SYMBOL, DIRECT_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<MarkSymbol>> STREAM_CODEC =
          ByteBufCodecs.holder(Chalk.Registries.MARK_SYMBOL, DIRECT_STREAM_CODEC);

    // --

    public static Optional<Holder.Reference<MarkSymbol>> get(RegistryAccess registryAccess, ResourceKey<MarkSymbol> key) {
        return registryAccess.registryOrThrow(Chalk.Registries.MARK_SYMBOL).getHolder(key);
    }

    public static Holder<MarkSymbol> getOrThrow(RegistryAccess registryAccess, ResourceKey<MarkSymbol> key) {
        return registryAccess.registryOrThrow(Chalk.Registries.MARK_SYMBOL).getHolderOrThrow(key);
    }

    public static Holder<MarkSymbol> withFallback(RegistryAccess registryAccess, Optional<Holder<MarkSymbol>> symbol) {
        Registry<MarkSymbol> registry = registryAccess.registryOrThrow(Chalk.Registries.MARK_SYMBOL);
        return symbol
              .or(() -> registry.getHolder(DEFAULT))
              .or(registry::getAny)
              .orElseThrow();
    }

    // --

    public static void bootstrap(BootstrapContext<MarkSymbol> context) {
        register(context, ARROW, 0, OrientationBehavior.FULL);
        register(context, CENTER, 0, OrientationBehavior.FIXED);
        register(context, CHECKMARK, 45, OrientationBehavior.UP_DOWN_CARDINAL);
        register(context, CROSS, 45, OrientationBehavior.FIXED);
        register(context, HOUSE, 0, OrientationBehavior.UP_DOWN_CARDINAL);
        register(context, PICKAXE, 0, OrientationBehavior.UP_DOWN_CARDINAL);
        register(context, HEART, 0, OrientationBehavior.UP_DOWN_CARDINAL);
        register(context, SKULL, 0, OrientationBehavior.UP_DOWN_CARDINAL);
    }

    static void register(BootstrapContext<MarkSymbol> context, ResourceKey<MarkSymbol> key, int rotationOffset, MarkSymbol.OrientationBehavior behavior) {
        ResourceLocation texture = key.location().withPrefix("block/mark/");
        context.register(key, new MarkSymbol(texture, rotationOffset, behavior));
    }

    // --

    public enum OrientationBehavior implements StringRepresentable {
        FIXED("fixed"),
        FULL("full"),
        CARDINAL("cardinal"),
        UP_DOWN_CARDINAL("up_down_cardinal");

        public static final Codec<OrientationBehavior> CODEC = StringRepresentable.fromEnum(OrientationBehavior::values);
        public static final StreamCodec<ByteBuf, OrientationBehavior> STREAM_CODEC = ByteBufCodecs.idMapper(
              ByIdMap.continuous(OrientationBehavior::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP), OrientationBehavior::ordinal);

        private final String name;

        OrientationBehavior(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}

