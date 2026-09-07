package com.vomiter.sophisticatedairtank.data;

import com.possible_triangle.create_jetpack.Content;
import com.simibubi.create.AllItems;
import com.vomiter.sophisticatedairtank.SophisticatedAirTank;
import com.vomiter.sophisticatedairtank.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.PRESSURIZED_AIR_UPGRADE.get())
                .requires(AllItems.COPPER_BACKTANK.get())
                .requires(net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.TANK_UPGRADE.get())
                .unlockedBy("has_back_tank", has(AllItems.COPPER_BACKTANK.get()))
                .save(output, SophisticatedAirTank.modLoc(ModItems.PRESSURIZED_AIR_UPGRADE.getId().getPath()));

        ConditionalRecipe.builder()
                .addCondition(new ModLoadedCondition("create_jetpack"))
                .addRecipe(
                    consumer ->  ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.JET_PACK_UPGRADE.get())
                            .requires(Content.INSTANCE.getJETPACK_ITEM())
                            .requires(net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.TANK_UPGRADE.get())
                            .unlockedBy("has_back_tank", has(AllItems.COPPER_BACKTANK.get()))
                            .save(consumer)
                )
                .build(output, ModItems.JET_PACK_UPGRADE.getId());
    }
}
