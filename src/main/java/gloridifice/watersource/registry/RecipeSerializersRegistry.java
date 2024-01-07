package gloridifice.watersource.registry;

import gloridifice.watersource.WaterSource;
import gloridifice.watersource.common.recipe.*;
import gloridifice.watersource.common.recipe.serializer.StrainerFilterRecipeSerializer;
import gloridifice.watersource.common.recipe.serializer.ThirstRecipeSerializer;
import gloridifice.watersource.common.recipe.serializer.WaterFilterRecipeSerializer;
import gloridifice.watersource.common.recipe.serializer.WaterLevelRecipeSerializer;
import gloridifice.watersource.common.recipe.type.StrainerFilterRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeSerializersRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> MOD_RECIPE_SERIALIZERS;
    public static final RegistryObject<StrainerFilterRecipeSerializer> STRAINER_FILTER_RECIPE_SERIALIZER;
    public static final RegistryObject<ThirstRecipeSerializer> THIRST_RECIPE_SERIALIZER;
    public static final RegistryObject<WaterLevelRecipeSerializer> WATER_LEVEL_RECIPE_SERIALIZER;
    public static final RegistryObject<WaterFilterRecipeSerializer> WATER_FILTER_RECIPE_SERIALIZER;

    public RecipeSerializersRegistry() {
    }

    static {
        MOD_RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, WaterSource.MODID);
        STRAINER_FILTER_RECIPE_SERIALIZER = MOD_RECIPE_SERIALIZERS.register("crafting_strainer_filter", StrainerFilterRecipeSerializer::new);
        THIRST_RECIPE_SERIALIZER = MOD_RECIPE_SERIALIZERS.register("thirst", ThirstRecipeSerializer::new);
        WATER_LEVEL_RECIPE_SERIALIZER = MOD_RECIPE_SERIALIZERS.register("water_level", WaterLevelRecipeSerializer::new);
        WATER_FILTER_RECIPE_SERIALIZER = MOD_RECIPE_SERIALIZERS.register("water_filter", () -> {
            return new WaterFilterRecipeSerializer(WaterFilterRecipe::new, Fluids.EMPTY, Fluids.EMPTY);
        });
    }
}
