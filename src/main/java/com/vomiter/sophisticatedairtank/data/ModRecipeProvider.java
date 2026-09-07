package com.vomiter.sophisticatedairtank.data;

import com.possible_triangle.create_jetpack.Content;
import com.simibubi.create.AllItems;
import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import com.vomiter.sophisticatedairtank.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.concurrent.CompletableFuture;

import static net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.TANK_UPGRADE;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries
    ) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        buildPressurizedAirUpgrade(output);
        buildJetpackUpgrade(output);
    }

    private void buildPressurizedAirUpgrade(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC,
                        ModItems.PRESSURIZED_AIR_UPGRADE.get()
                )
                .requires(AllItems.COPPER_BACKTANK.get())
                .requires(TANK_UPGRADE.get())
                .unlockedBy(
                        "has_back_tank",
                        has(AllItems.COPPER_BACKTANK.get())
                )
                .save(
                        output,
                        SophisticatedAirTank.modLoc(
                                "pressurized_air_upgrade"
                        )
                );
    }

    private void buildJetpackUpgrade(RecipeOutput output) {
        Item jetpack = Content.INSTANCE.getJETPACK_ITEM().get();

        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC,
                        ModItems.JET_PACK_UPGRADE.get()
                )
                .requires(jetpack)
                .requires(TANK_UPGRADE.get())
                .unlockedBy(
                        "has_jetpack",
                        has(jetpack)
                )
                .save(
                        output.withConditions(
                                new ModLoadedCondition("create_jetpack")
                        ),
                        SophisticatedAirTank.modLoc(
                                "jet_pack_upgrade"
                        )
                );
    }
}