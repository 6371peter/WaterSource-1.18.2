package gloridifice.watersource.common.recipe;

import gloridifice.watersource.registry.RecipeTypesRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ModRecipeManager {
    public ModRecipeManager() {
    }
    public static WaterLevelAndEffectRecipe getWERecipeFromItem(Level level, ItemStack itemStack) {
        List<WaterLevelAndEffectRecipe> recipes = new ArrayList<>();
        Iterator var3 = level.getRecipeManager().getAllRecipesFor((RecipeType)RecipeTypesRegistry.WATER_LEVEL_RECIPE.get()).iterator();

        while (var3.hasNext()) {
            WaterLevelAndEffectRecipe recipe = (WaterLevelAndEffectRecipe)var3.next();
            if (recipe.conform(itemStack)) {
                recipes.add(recipe);
            }
        }
        if (recipes.size() > 0){
            Collections.sort(recipes);
            return recipes.get(0);
        }
        else return null;
    }

    public static ThirstRecipe getThirstRecipeFromItem(Level world, ItemStack itemStack) {
        List<ThirstRecipe> list = new ArrayList<>();
        if (world != null) {
            list.addAll(world.getRecipeManager().getAllRecipesFor(RecipeTypesRegistry.THIRST_RECIPE.get()));
        }

        Iterator var3 = list.iterator();

        ThirstRecipe recipe;
        do {
            if (!var3.hasNext()) {
                return null;
            }
            recipe = (ThirstRecipe)var3.next();
        } while (!recipe.conform(itemStack));

        return recipe;
    }
}
