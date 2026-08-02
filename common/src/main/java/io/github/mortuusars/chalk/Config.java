package io.github.mortuusars.chalk;

import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.chalk.core.MarkSymbol;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class Config {
    public static class Server {
        public static final ModConfigSpec SPEC;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            SPEC = builder.build();
        }
    }

    public static class Common {
        public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.IntValue GLOWING_MARK_LIGHT_LEVEL;
        public static final ModConfigSpec.BooleanValue CHALK_BOX_GLOWING_ENABLED;
        public static final ModConfigSpec.IntValue CHALK_BOX_GLOWING_AMOUNT_PER_ITEM;
        public static final ModConfigSpec.BooleanValue LOOT;
        public static final Map<MarkSymbol, Pair<ModConfigSpec.BooleanValue, ModConfigSpec.ConfigValue<String>>> SYMBOL_CONFIG;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            GLOWING_MARK_LIGHT_LEVEL = builder.comment("How much light glowing mark produces. Default: 5")
                    .defineInRange("GlowingMarkLightLevel", 5, 0, 15);

            CHALK_BOX_GLOWING_ENABLED = builder.comment("Controls whether glowing should be enabled in Chalk Box.\nIf disabled - you will not be able to draw glowing marks with chalk box.\nDefault: true")
                    .define("ChalkBoxGlowingEnabled", true);

            CHALK_BOX_GLOWING_AMOUNT_PER_ITEM = builder.comment("How many glowing uses one glowing item will give.\nDefault: 8")
                    .defineInRange("ChalkBoxAmountPerGlowingItem", 8, 1, 9999);

            LOOT = builder.comment("If enabled, Chalks (and Chalk Boxes) will generate in Dungeons, Abandoned Mineshafts, Villages (Planes and Savanna), Cartographer village houses\nDefault: true")
                    .define("GenerateChalkInLootChests", true);

            builder.comment("Enable/disable symbols and location of the advancement that will unlock that symbol. (Empty = available from the start)")
                    .push("Symbols");

            Map<MarkSymbol, String> symbolAdvancements = new HashMap<>();
            symbolAdvancements.put(MarkSymbol.CHECKMARK, "");
            symbolAdvancements.put(MarkSymbol.CROSS, "");
            symbolAdvancements.put(MarkSymbol.SKULL, "chalk:adventure/bound_by_bone");
            symbolAdvancements.put(MarkSymbol.HOUSE, "chalk:adventure/home_is_where_the_bed_is");
            symbolAdvancements.put(MarkSymbol.HEART, "minecraft:husbandry/tame_an_animal");
            symbolAdvancements.put(MarkSymbol.PICKAXE, "minecraft:story/iron_tools");

            SYMBOL_CONFIG = new HashMap<>();

            for (var entry : symbolAdvancements.entrySet()) {
                MarkSymbol symbol = entry.getKey();
                String advancement = entry.getValue();

                String symbolName = StringUtils.capitalize(symbol.getSerializedName());
                SYMBOL_CONFIG.put(symbol, Pair.of(
                        builder.define(symbolName + "Enabled", true),
                        builder.define(symbolName + "UnlockAdvancement", advancement)));
            }

            builder.pop();

            SPEC = builder.build();
        }
    }

    public static class Client {
        public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.BooleanValue CHALK_BOX_TOOLTIP_CONTENTS;
        public static final ModConfigSpec.BooleanValue CHALK_BOX_TOOLTIP_DETAILS;
        public static final Map<MarkSymbol, ModConfigSpec.IntValue> SYMBOL_ROTATION_OFFSETS;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            CHALK_BOX_TOOLTIP_CONTENTS = builder
                    .comment("Contents of the Chalk Box will be shown in item's tooltip.")
                    .define("ChalkBoxTooltipContents", true);

            CHALK_BOX_TOOLTIP_DETAILS = builder
                    .comment("Information about using Chalk Box will be shown in the item's tooltip.")
                    .define("ChalkBoxTooltipDetails", true);

            builder.comment("Rotation offsets (in degrees) for each mark.").push("SymbolOffsets");

            SYMBOL_ROTATION_OFFSETS = new HashMap<>();

            for (MarkSymbol symbol : MarkSymbol.values()) {
                String symbolName = StringUtils.capitalize(symbol.getSerializedName());
                int defaultOffset = symbol == MarkSymbol.CROSS || symbol == MarkSymbol.CHECKMARK ? 45 : 0;
                SYMBOL_ROTATION_OFFSETS.put(symbol, builder.defineInRange(symbolName + "RotationOffset",
                        defaultOffset, -360, 360));
            }

            builder.pop();

            SPEC = builder.build();
        }
    }
}
