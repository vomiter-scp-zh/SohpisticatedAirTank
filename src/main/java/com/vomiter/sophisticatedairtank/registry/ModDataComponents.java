package com.vomiter.sophisticatedairtank.registry;

import com.mojang.serialization.Codec;
import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.p3pp3rf1y.sophisticatedcore.util.CodecHelper;

import java.util.function.UnaryOperator;

public final class ModDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE,
                    SophisticatedAirTank.MOD_ID
            );

    public static final DataComponentType<Integer> AIR = register(
            "air",
            builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
    );

    public static final DataComponentType<Integer> CAPACITY = register(
            "capacity",
            builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
    );

    public static final DataComponentType<ItemContainerContents> AIR_IO_INVENTORY = register(
            "air_io_inventory",
            builder -> builder
                    .persistent(CodecHelper.LENIENT_ITEM_CONTAINER_CONTENTS_CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .cacheEncoding()
    );

    private static <T> DataComponentType<T> register(
            String name,
            UnaryOperator<DataComponentType.Builder<T>> factory
    ) {
        DataComponentType<T> type =
                factory.apply(DataComponentType.builder()).build();
        COMPONENTS.register(name, () -> type);
        return type;
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }

    private ModDataComponents() {
    }
}