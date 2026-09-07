package com.vomiter.sophisticatedairtank.client;

import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUpgradeContainer;
import com.vomiter.sophisticatedairtank.network.ModNetwork;
import com.vomiter.sophisticatedairtank.registry.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeGuiManager;

import java.util.ArrayDeque;
import java.util.Optional;

public final class ClientEvents {

    public static void init(IEventBus modBus){
        modBus.addListener(ClientEvents::onClientSetup);
        modBus.addListener(ClientEvents::registerRenderers);

        var forgeBus = NeoForge.EVENT_BUS;
        ClientEvents.initForge(forgeBus);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.PRESSURIZED_BACKPACK.get(), PressurizedBackpackRenderer::new);
    }

    public static void onClientSetup(FMLClientSetupEvent event){
        event.enqueueWork(() -> {
            UpgradeGuiManager.registerInventoryControl(
                    PressurizedAirUpgradeContainer.TYPE,
                    PressurizedAirInventoryControl::new
            );
            UpgradeGuiManager.registerTab(
                    PressurizedAirUpgradeContainer.TYPE,
                    PressurizedAirUpgradeTab::new
            );
        });

    }

    static void initForge(IEventBus forgeBus){
        forgeBus.addListener(ClientEvents::onLevelTick);
        forgeBus.addListener(ClientEvents::onLogOut);
        forgeBus.addListener(ClientEvents::onLogIn);
        forgeBus.addListener(ClientEvents::onEquipmentChange);
    }

    private static void requireSync(Player player){
        var now = Optional.ofNullable(Minecraft.getInstance().level).map(Level::getGameTime).orElse(0L);
        delayedTasks.add(new DelayedClientRunnable(
                now + 100,
                () -> {
                    PlayerInventoryProvider.get().runOnBackpacks(
                            player,
                            (backpack, handlerName, identifier, slot) -> {
                                IBackpackWrapper wrapper = BackpackWrapper.fromStack(backpack);

                                wrapper.getContentsUuid().ifPresent(uuid -> {
                                    SophisticatedAirTank.LOGGER.info("UUID = {}", uuid);
                                    PacketDistributor.sendToServer(
                                            new ModNetwork.SATSyncRequest(uuid)
                                    );
                                });

                                return true;
                            }
                    );
                }
        ));
    }

    public static void onLogIn(ClientPlayerNetworkEvent.LoggingIn event){
        delayedTasks.clear();
        requireSync(event.getPlayer());
    }

    public static void onEquipmentChange(LivingEquipmentChangeEvent event){
        if(event.getEntity() instanceof Player player) requireSync(player);
    }

    public static void onLogOut(ClientPlayerNetworkEvent.LoggingOut event){
        delayedTasks.clear();
    }

    public static void onLevelTick(ClientTickEvent.Post event){
        if (Minecraft.getInstance().level == null) return;
        for (int i = 0; i < delayedTasks.size(); i++) {
            var task = delayedTasks.pollFirst();
            if(task == null) break;
            if (Minecraft.getInstance().level.getGameTime() > task.gameTick()) {
                task.runnable().run();
            } else {
                delayedTasks.addLast(new DelayedClientRunnable(
                        task.gameTick,
                        task.runnable
                ));
            }
        }
    }


    public static final ArrayDeque<DelayedClientRunnable> delayedTasks = new ArrayDeque<>();
    public record DelayedClientRunnable(long gameTick, Runnable runnable){}

}
