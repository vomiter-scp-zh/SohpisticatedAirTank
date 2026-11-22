package com.vomiter.sophisticatedairtank;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;
import com.vomiter.sophisticatedairtank.client.ClientEvents;
import com.vomiter.sophisticatedairtank.common.BackpackForgeEvent;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirIntegration;
import com.vomiter.sophisticatedairtank.common.upgrade.PressurizedAirUpgradeContainer;
import com.vomiter.sophisticatedairtank.data.ModDataGenerator;
import com.vomiter.sophisticatedairtank.network.ModNetwork;
import com.vomiter.sophisticatedairtank.registry.ModBlockEntities;
import com.vomiter.sophisticatedairtank.registry.ModBlocks;
import com.vomiter.sophisticatedairtank.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerRegistry;
import org.slf4j.Logger;

@Mod(SophisticatedAirTank.MOD_ID)
public final class SophisticatedAirTank {
    public static final String MOD_ID = "sophisticatedairtank";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation modLoc(String path){
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public SophisticatedAirTank() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(ModNetwork::onCommonSetup);
        modBus.addListener(this::addCreativeTabContents);
        modBus.addListener(ModDataGenerator::generateData);

        BackpackForgeEvent.init();
        if (FMLEnvironment.dist.isClient()){
            ClientEvents.init(modBus);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BacktankUtil.addBacktankSupplier(PressurizedAirIntegration::findAirSources);
            BlockStressValues.IMPACTS.register(ModBlocks.PRESSURIZED_BACKPACK.get(), () -> 4.0D);
            assert ModItems.PRESSURIZED_AIR_UPGRADE.getId() != null;
            UpgradeContainerRegistry.register(
                    ModItems.PRESSURIZED_AIR_UPGRADE.getId(),
                    PressurizedAirUpgradeContainer.TYPE
            );
            assert ModItems.JET_PACK_UPGRADE.getId() != null;
            UpgradeContainerRegistry.register(
                    ModItems.JET_PACK_UPGRADE.getId(),
                    PressurizedAirUpgradeContainer.TYPE
            );

        });
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.CREATIVE_TAB.getKey()) {
            event.accept(ModItems.PRESSURIZED_AIR_UPGRADE.get());
            if (ModList.get().isLoaded("create_jetpack")){
                event.accept(ModItems.JET_PACK_UPGRADE.get());
            }
        }
    }
}
