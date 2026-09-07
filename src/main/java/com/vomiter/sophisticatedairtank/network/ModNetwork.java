package com.vomiter.sophisticatedairtank.network;

import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class ModNetwork {
    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;
    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CHANNEL = NetworkRegistry.newSimpleChannel(
                    SophisticatedAirTank.modLoc("main"),
                    () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
            );

            int id = 0;
            CHANNEL.messageBuilder(SATSyncRequest.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(SATSyncRequest::encode)
                    .decoder(SATSyncRequest::decode)
                    .consumerMainThread(SATSyncRequest::handle)
                    .add();
            CHANNEL.messageBuilder(SATSyncResponse.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(SATSyncResponse::encode)
                    .decoder(SATSyncResponse::decode)
                    .consumerMainThread(SATSyncResponse::handle)
                    .add();
        });

    }

    public record SATSyncResponse(UUID uuid, CompoundTag tag){
        public static void encode(SATSyncResponse pkt, FriendlyByteBuf buf){
            buf.writeUUID(pkt.uuid);
            buf.writeNbt(pkt.tag);
        }

        public static SATSyncResponse decode(FriendlyByteBuf buf){
            return new SATSyncResponse(buf.readUUID(), buf.readNbt());
        }

        public static void handle(SATSyncResponse pkt, Supplier<NetworkEvent.Context> ctx) {
            BackpackStorage.get().setBackpackContents(pkt.uuid, pkt.tag);
            ModNetworkClientHandler.handleSATSyncResponse(pkt);
            ctx.get().setPacketHandled(true);

        }
    }

    public record SATSyncRequest(UUID uuid){
        public static void encode(SATSyncRequest pkt, FriendlyByteBuf buf) {
            buf.writeUUID(pkt.uuid);
        }
        public static SATSyncRequest decode(FriendlyByteBuf buf) {
            return new SATSyncRequest(buf.readUUID());
        }
        public static void handle(SATSyncRequest pkt, Supplier<NetworkEvent.Context> ctx) {
            var sourceContents = BackpackStorage.get()
                    .getOrCreateBackpackContents(
                            pkt.uuid
                    );

            CompoundTag responseContents =
                    copyInventoryContents(sourceContents);

            PlayerInventoryProvider.get().runOnBackpacks(Objects.requireNonNull(ctx.get().getSender()), ((backpack, s, s1, i) -> {
                        backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance()).ifPresent(iBackpackWrapper -> {
                            iBackpackWrapper.getContentsUuid().ifPresent(uuid1 -> {
                                if (uuid1.equals(pkt.uuid)){
                                    CHANNEL.send(
                                            PacketDistributor.PLAYER.with(() -> ctx.get().getSender()),
                                            new SATSyncResponse(uuid1, responseContents)
                                    );
                                }
                            });
                        });
                        return false;
                    }));

            ctx.get().setPacketHandled(true);
        }

        private static CompoundTag copyInventoryContents(
                CompoundTag source
        ) {
            CompoundTag result = new CompoundTag();
            result.put(
                    "inventory",
                    source.contains("inventory", Tag.TAG_COMPOUND)
                            ? source.getCompound("inventory").copy()
                            : new CompoundTag()
            );

            result.put(
                    "upgradeInventory",
                    source.contains("upgradeInventory", Tag.TAG_COMPOUND)
                            ? source.getCompound("upgradeInventory").copy()
                            : new CompoundTag()
            );

            return result;
        }
    }
}
