package com.vomiter.sophisticatedairtank.common;

import com.vomiter.sophisticatedairtank.common.block.PressurizedBackpackConversion;
import com.vomiter.sophisticatedairtank.compat.CuriosCompat;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.ModList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class BackpackForgeEvent {
    public static final ArrayDeque<DelayedRunnable> delayedTasks = new ArrayDeque<>();
    public record DelayedRunnable(long gameTick, Level level, Runnable runnable){}

    public static void init(){
        var forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(BackpackForgeEvent::onLevelTick);
        forgeBus.addListener(BackpackForgeEvent::onServerStartEvent);
        forgeBus.addListener(PressurizedBackpackConversion::onNeighborNotify);
        forgeBus.addListener(PressurizedBackpackConversion::onPlaced);
        if (ModList.get().isLoaded("curios")){
            forgeBus.addListener(CuriosCompat::onCurioChangeEvent);
        }
    }

    public static void onServerStartEvent(ServerStartingEvent event){
        delayedTasks.clear();
    }

    public static void onLevelTick(TickEvent.LevelTickEvent event){
        if(event.level.isClientSide) return;
        for (int i = 0; i < BackpackForgeEvent.delayedTasks.size(); i++) {
            var task = BackpackForgeEvent.delayedTasks.pollFirst();
            if(task == null) break;
            if (event.level.getGameTime() > task.gameTick() && event.level.dimension().location().equals(task.level().dimension().location())) {
                task.runnable().run();
            } else {
                BackpackForgeEvent.delayedTasks.addLast(new DelayedRunnable(
                        task.gameTick,
                        task.level,
                        task.runnable
                ));
            }
        }
    }

}
