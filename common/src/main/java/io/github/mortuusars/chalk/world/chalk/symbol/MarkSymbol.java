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
import java.util.stream.Stream;

import static io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol.OrientationBehavior.*;

public record MarkSymbol(ResourceLocation texture, int rotationOffset, OrientationBehavior orientationBehavior,
                         Optional<ResourceLocation> requiredAdvancement, String group, int groupPriority) {

    public static final String GROUP_PRIMARY = "primary";
    public static final ResourceKey<MarkSymbol> ARROW =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("arrow"));
    public static final ResourceKey<MarkSymbol> DOT =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("dot"));
    public static final ResourceKey<MarkSymbol> BACK =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("back"));

    public static final String GROUP_SYMBOLS = "symbols";
    public static final ResourceKey<MarkSymbol> CHECK =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("check"));
    public static final ResourceKey<MarkSymbol> CROSS =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("cross"));
    public static final ResourceKey<MarkSymbol> HOUSE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("house"));
    public static final ResourceKey<MarkSymbol> HEART =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("heart"));
    public static final ResourceKey<MarkSymbol> SKULL =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("skull"));
    public static final ResourceKey<MarkSymbol> NOTE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("note"));
    public static final ResourceKey<MarkSymbol> SUN =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("sun"));
    public static final ResourceKey<MarkSymbol> MOON =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("moon"));
    public static final ResourceKey<MarkSymbol> STAR =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("star"));

    public static final String GROUP_TOOLS = "tools";
    public static final ResourceKey<MarkSymbol> PICKAXE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("pickaxe"));
    public static final ResourceKey<MarkSymbol> AXE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("axe"));
    public static final ResourceKey<MarkSymbol> SHOVEL =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("shovel"));
    public static final ResourceKey<MarkSymbol> SWORD =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("sword"));
    public static final ResourceKey<MarkSymbol> HOE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("hoe"));

    public static final ResourceKey<MarkSymbol> DEFAULT = DOT;

    // --

    public static final Codec<MarkSymbol> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
          ResourceLocation.CODEC.fieldOf("texture").forGetter(MarkSymbol::texture),
          Codec.intRange(-180, 180).optionalFieldOf("rotation_offset", 0).forGetter(MarkSymbol::rotationOffset),
          OrientationBehavior.CODEC.optionalFieldOf("orientation_behavior", FULL).forGetter(MarkSymbol::orientationBehavior),
          ResourceLocation.CODEC.optionalFieldOf("required_advancement").forGetter(MarkSymbol::requiredAdvancement),
          Codec.STRING.optionalFieldOf("group", "symbols").forGetter(MarkSymbol::group),
          Codec.INT.optionalFieldOf("group_priority", 9999).forGetter(MarkSymbol::groupPriority)
    ).apply(i, MarkSymbol::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarkSymbol> DIRECT_STREAM_CODEC = StreamCodec.composite(
          ResourceLocation.STREAM_CODEC, MarkSymbol::texture,
          ByteBufCodecs.INT, MarkSymbol::rotationOffset,
          OrientationBehavior.STREAM_CODEC, MarkSymbol::orientationBehavior,
          ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), MarkSymbol::requiredAdvancement,
          ByteBufCodecs.STRING_UTF8, MarkSymbol::group,
          ByteBufCodecs.INT, MarkSymbol::groupPriority,
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

    public static Stream<Holder<MarkSymbol>> getAllHolders(HolderLookup.Provider lookup) {
        return lookup.lookupOrThrow(Chalk.Registries.MARK_SYMBOL)
              .listElements()
              .map(ref -> ref);
    }

    // --

    public static void bootstrap(BootstrapContext<MarkSymbol> context) {
        register(context, ARROW, 0, FULL, Optional.empty(), GROUP_PRIMARY, 10);
        register(context, DOT, 0, FIXED, Optional.empty(), GROUP_PRIMARY, 20);
        register(context, BACK, 0, CARDINAL, adv("chalk:adventure/this_way"), GROUP_PRIMARY, 30);
        register(context, CHECK, 45, UP_DOWN_CARDINAL, Optional.empty(), GROUP_PRIMARY, 40);
        register(context, CROSS, 45, FIXED, Optional.empty(), GROUP_PRIMARY, 50);

        register(context, HOUSE, 0, UP_DOWN_CARDINAL, adv("chalk:adventure/home_is_where_the_bed_is"), GROUP_SYMBOLS, 100);
        register(context, HEART, 0, UP_DOWN_CARDINAL, adv("minecraft:husbandry/breed_an_animal"), GROUP_SYMBOLS, 110);
        register(context, SKULL, 0, UP_DOWN_CARDINAL, adv("minecraft:adventure/sniper_duel"), GROUP_SYMBOLS, 120);
        register(context, NOTE, 0, UP_DOWN_CARDINAL, adv("minecraft:adventure/play_jukebox_in_meadows"), GROUP_SYMBOLS, 130);
        register(context, SUN, 0, UP_DOWN_CARDINAL, adv("chalk:adventure/consumed_by_the_light"), GROUP_SYMBOLS, 210);
        register(context, MOON, 0, UP_DOWN_CARDINAL, adv("chalk:adventure/alone_in_the_darkness"), GROUP_SYMBOLS, 220);
        register(context, STAR, 0, UP_DOWN_CARDINAL, adv("chalk:adventure/guiding_star"), GROUP_SYMBOLS, 230);

        register(context, SWORD, 0, UP_DOWN_CARDINAL, adv("minecraft:story/iron_tools"), GROUP_TOOLS, 10);
        register(context, SHOVEL, 0, UP_DOWN_CARDINAL, adv("minecraft:story/iron_tools"), GROUP_TOOLS, 20);
        register(context, PICKAXE, 0, UP_DOWN_CARDINAL, adv("minecraft:story/iron_tools"), GROUP_TOOLS, 30);
        register(context, AXE, 0, UP_DOWN_CARDINAL, adv("minecraft:story/iron_tools"), GROUP_TOOLS, 40);
        register(context, HOE, 0, UP_DOWN_CARDINAL, adv("minecraft:story/iron_tools"), GROUP_TOOLS, 50);
    }

    static Optional<ResourceLocation> adv(String id) {
        return Optional.of(ResourceLocation.parse(id));
    }

    static void register(BootstrapContext<MarkSymbol> context, ResourceKey<MarkSymbol> key,
                         int rotationOffset, MarkSymbol.OrientationBehavior behavior,
                         Optional<ResourceLocation> requiredAdvancement, String group, int groupPriority) {
        ResourceLocation texture = key.location().withPrefix("block/mark/");
        context.register(key, new MarkSymbol(texture, rotationOffset, behavior, requiredAdvancement, group, groupPriority));
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

