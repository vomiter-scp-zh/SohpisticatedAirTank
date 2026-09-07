package com.vomiter.sophisticatedairtank.common;

import com.vomiter.sophisticatedairtank.common.block.PressurizedBackpackConversion;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class BackpackForgeEvent {
    public static final ArrayDeque<DelayedRunnable> delayedTasks = new ArrayDeque<>();
    public record DelayedRunnable(long gameTick, Level level, Runnable runnable){}

    public static void init(){
        var forgeBus = NeoForge.EVENT_BUS;
        forgeBus.addListener(BackpackForgeEvent::onLevelTick);
        forgeBus.addListener(BackpackForgeEvent::onServerStartEvent);
        forgeBus.addListener(PressurizedBackpackConversion::onNeighborNotify);
        forgeBus.addListener(PressurizedBackpackConversion::onPlaced);
    }

    public static void onServerStartEvent(ServerStartingEvent event){
        delayedTasks.clear();
    }

    public static void onLevelTick(LevelTickEvent.Post event){
        if(event.getLevel().isClientSide) return;
        for (int i = 0; i < BackpackForgeEvent.delayedTasks.size(); i++) {
            var task = BackpackForgeEvent.delayedTasks.pollFirst();
            if(task == null) break;
            if (event.getLevel().getGameTime() > task.gameTick() && event.getLevel().dimension().location().equals(task.level().dimension().location())) {
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
