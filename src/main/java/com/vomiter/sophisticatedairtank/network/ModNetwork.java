package com.vomiter.sophisticatedairtank.network;

import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;

import java.util.UUID;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event
                .registrar(SophisticatedAirTank.MOD_ID)
                .versioned(PROTOCOL_VERSION);

        registrar.playToServer(
                SATSyncRequest.TYPE,
                SATSyncRequest.STREAM_CODEC,
                SATSyncRequest::handlePayload
        );

        registrar.playToClient(
                SATSyncResponse.TYPE,
                SATSyncResponse.STREAM_CODEC,
                SATSyncResponse::handlePayload
        );
    }

    /**
     * Client-side 請求同步指定背包。
     */
    public static void requestSync(UUID backpackUuid) {
        PacketDistributor.sendToServer(
                new SATSyncRequest(backpackUuid)
        );
    }

    public record SATSyncResponse(
            UUID uuid,
            CompoundTag contents
    ) implements CustomPacketPayload {

        public static final Type<SATSyncResponse> TYPE =
                new Type<>(
                        SophisticatedAirTank.modLoc("sync_response")
                );

        public static final StreamCodec<ByteBuf, SATSyncResponse>
                STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                SATSyncResponse::uuid,
                ByteBufCodecs.COMPOUND_TAG,
                SATSyncResponse::contents,
                SATSyncResponse::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handlePayload(
                SATSyncResponse payload,
                IPayloadContext context
        ) {
            context.enqueueWork(() -> {
                /*
                 * playToClient 保證這個 handler 只會在 client 收到，
                 * BackpackStorage.get() 此時取得 client storage copy。
                 */
                BackpackStorage.get().setBackpackContents(
                        payload.uuid,
                        payload.contents
                );

                ModNetworkClientHandler.handleSATSyncResponse(payload);
            });
        }
    }

    public record SATSyncRequest(
            UUID uuid
    ) implements CustomPacketPayload {

        public static final Type<SATSyncRequest> TYPE =
                new Type<>(
                        SophisticatedAirTank.modLoc("sync_request")
                );

        public static final StreamCodec<ByteBuf, SATSyncRequest>
                STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                SATSyncRequest::uuid,
                SATSyncRequest::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handlePayload(
                SATSyncRequest payload,
                IPayloadContext context
        ) {
            context.enqueueWork(() -> {
                Player contextPlayer = context.player();

                if (!(contextPlayer instanceof ServerPlayer serverPlayer)) {
                    return;
                }

                /*
                 * 不接受任意 UUID。
                 * 玩家必須實際持有 UUID 相符的 Sophisticated Backpack。
                 */
                boolean hasRequestedBackpack =
                        playerHasBackpack(
                                serverPlayer,
                                payload.uuid
                        );

                if (!hasRequestedBackpack) {
                    SophisticatedAirTank.LOGGER.warn(
                            "Rejected backpack sync request from {} for UUID {}",
                            serverPlayer.getGameProfile().getName(),
                            payload.uuid
                    );
                    return;
                }

                CompoundTag sourceContents =
                        BackpackStorage
                                .get(serverPlayer.serverLevel())
                                .getOrCreateBackpackContents(
                                        payload.uuid
                                );

                CompoundTag responseContents =
                        copyInventoryContents(sourceContents);

                PacketDistributor.sendToPlayer(
                        serverPlayer,
                        new SATSyncResponse(
                                payload.uuid,
                                responseContents
                        )
                );
            });
        }

        private static boolean playerHasBackpack(
                ServerPlayer player,
                UUID requestedUuid
        ) {
            return PlayerInventoryProvider.get().runOnBackpacks(
                    player,
                    (backpack, handlerName, identifier, slot) ->
                            BackpackWrapper
                                    .fromStack(backpack)
                                    .getContentsUuid()
                                    .filter(requestedUuid::equals)
                                    .isPresent()
            );
        }

        private static CompoundTag copyInventoryContents(
                CompoundTag source
        ) {
            CompoundTag result = new CompoundTag();

            copyCompoundIfPresent(
                    source,
                    result,
                    InventoryHandler.INVENTORY_TAG
            );

            copyCompoundIfPresent(
                    source,
                    result,
                    UpgradeHandler.UPGRADE_INVENTORY_TAG
            );

            return result;
        }

        private static void copyCompoundIfPresent(
                CompoundTag source,
                CompoundTag target,
                String key
        ) {
            if (source.contains(key, Tag.TAG_COMPOUND)) {
                target.put(
                        key,
                        source.getCompound(key).copy()
                );
            }
        }
    }
}