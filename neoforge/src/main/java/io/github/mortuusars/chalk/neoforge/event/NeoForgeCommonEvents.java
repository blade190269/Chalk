package io.github.mortuusars.chalk.neoforge.event;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.data.ChalkColors;
import io.github.mortuusars.chalk.network.neoforge.PacketsImpl;
import io.github.mortuusars.chalk.network.packet.C2SPackets;
import io.github.mortuusars.chalk.network.packet.CommonPackets;
import io.github.mortuusars.chalk.network.packet.Packet;
import io.github.mortuusars.chalk.network.packet.S2CPackets;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.chalk.world.item.OldChalkItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.function.Supplier;

@EventBusSubscriber(modid = Chalk.ID)
public class NeoForgeCommonEvents {
    @SubscribeEvent
    public static void addDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(Chalk.Registries.MARK_SYMBOL, MarkSymbol.DIRECT_CODEC, MarkSymbol.DIRECT_CODEC);
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        // This monstrosity is to avoid having to define packets for forge and fabric separately.
        for (CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> definition : S2CPackets.getDefinitions()) {
            registrar.playToClient((CustomPacketPayload.Type<Packet>) definition.type(),
                  (StreamCodec<FriendlyByteBuf, Packet>) definition.codec(), PacketsImpl::handle);
        }

        for (CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> definition : C2SPackets.getDefinitions()) {
            registrar.playToServer((CustomPacketPayload.Type<Packet>) definition.type(),
                  (StreamCodec<FriendlyByteBuf, Packet>) definition.codec(), PacketsImpl::handle);
        }

        for (CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> definition : CommonPackets.getDefinitions()) {
            registrar.playBidirectional((CustomPacketPayload.Type<Packet>) definition.type(),
                  (StreamCodec<FriendlyByteBuf, Packet>) definition.codec(), PacketsImpl::handle);
        }
    }

    @SubscribeEvent
    private static void onCreativeTabsBuild(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            for (Supplier<OldChalkItem> item : Chalk.Items.CHALKS.values()) {
                event.accept(item.get());
            }

            event.accept(new ItemStack(Chalk.Items.CHALK.get()));

            if (Config.Server.ADD_DYED_CHALKS_TO_TAB.get()) {
                for (DyeColor dyeColor : ChalkColors.ORDERED_DYE_COLORS) {
                    if (dyeColor == DyeColor.WHITE) {
                        continue;
                    }

                    ItemStack stack = new ItemStack(Chalk.Items.CHALK.get());
                    int color = Chalk.Items.CHALK.get().getColorFromDye(stack, dyeColor);
                    stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, true));
                    event.accept(stack);
                }
            }

            event.accept(Chalk.Items.CHALK_BOX.get());
        }
    }

    @SubscribeEvent
    public static void configLoaded(ModConfigEvent.Loading event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER) {
            Config.Server.loading();
        }
    }

    @SubscribeEvent
    public static void configLoaded(ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER) {
            Config.Server.reloading();
        }
    }
}