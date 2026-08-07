package io.github.mortuusars.chalk;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    public static class Server {
        public static final ModConfigSpec SPEC;

        public static final ModConfigSpec.BooleanValue SYMBOL_UNLOCKING;

        // Chalk Box
        public static final ModConfigSpec.BooleanValue CHALK_BOX_GLOWING_ENABLED;
        public static final ModConfigSpec.IntValue CHALK_BOX_GLOWING_AMOUNT_PER_ITEM;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            SYMBOL_UNLOCKING = builder
                  .comment("Controls whether some of the symbols need to be unlocked to be drawable.",
                        "Setting this to 'false' will bypass unlocking feature and make all symbols always available.",
                        "Default: true")
                  .define("symbol_unlocking", true);

            builder.push("chalk_box");

            CHALK_BOX_GLOWING_ENABLED = builder.comment("Controls whether glowing should be enabled in Chalk Box.\nIf disabled - you will not be able to draw glowing marks with chalk box.\nDefault: true")
                  .define("chalk_box_glowing_enabled", true);

            CHALK_BOX_GLOWING_AMOUNT_PER_ITEM = builder.comment("How many glowing uses one glowing item will give.\nDefault: 8")
                  .defineInRange("chalk_box_amount_per_glowing_item", 8, 1, 9999);

            builder.pop();

            SPEC = builder.build();
        }
    }

    public static class Common {
        public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.BooleanValue LOOT;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            // This option needs to be in common config as server config is not loaded when value is requested
            LOOT = builder.comment("If enabled, Chalks (and Chalk Boxes) will generate in Dungeons, Abandoned Mineshafts, " +
                              "Villages (Planes and Savanna), Cartographer village houses",
                        "Default: true")
                    .define("generate_chalk_in_loot_chests", true);

            SPEC = builder.build();
        }
    }

    public static class Client {
        public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> SYMBOL_SELECTION_GROUP_SORTING;
        public static final ModConfigSpec.BooleanValue CHALK_BOX_TOOLTIP_CONTENTS;
        public static final ModConfigSpec.BooleanValue CHALK_BOX_TOOLTIP_DETAILS;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            SYMBOL_SELECTION_GROUP_SORTING = builder
                  .comment("Defines how groups will be sorted in symbol selection overlay.",
                        "Undefined groups will be sorted alphabetically and placed after defined ones.")
                  .define("symbol_selection_groups_sorting", List.of("primary", "symbols", "tools"));

            CHALK_BOX_TOOLTIP_CONTENTS = builder
                    .comment("Contents of the Chalk Box will be shown in item's tooltip.")
                    .define("chalk_box_tooltip_contents", true);

            CHALK_BOX_TOOLTIP_DETAILS = builder
                    .comment("Information about using Chalk Box will be shown in the item's tooltip.")
                    .define("chalk_box_tooltip_details", true);

            SPEC = builder.build();
        }
    }
}
