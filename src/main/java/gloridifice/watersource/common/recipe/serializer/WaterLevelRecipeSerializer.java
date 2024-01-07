package gloridifice.watersource.common.recipe.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import gloridifice.watersource.common.recipe.WaterLevelAndEffectRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WaterLevelRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<WaterLevelAndEffectRecipe> {


    public WaterLevelRecipeSerializer() {
    }

    public WaterLevelAndEffectRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Fluid fluid = null;
        CompoundTag compoundTag = null;
        List<MobEffectInstance> effectInstances = new ArrayList();
        String group = GsonHelper.getAsString(json, "group", "");
        Ingredient ingredient = Ingredient.EMPTY;
        if (GsonHelper.isValidNode(json, "ingredient")) {
            JsonElement jsonelement = GsonHelper.isArrayNode(json, "ingredient") ? GsonHelper.getAsJsonArray(json, "ingredient") : GsonHelper.getAsJsonObject(json, "ingredient");
            ingredient = Ingredient.fromJson((JsonElement)jsonelement);
        }

        if (GsonHelper.isArrayNode(json, "mob_effects")) {
            JsonArray effectsJsonArray = GsonHelper.getAsJsonArray(json, "mob_effects");
            Iterator var11 = effectsJsonArray.iterator();

            while(var11.hasNext()) {
                JsonElement effect = (JsonElement)var11.next();
                JsonObject mobEffectJsonObj = effect.getAsJsonObject();
                int duration = GsonHelper.getAsInt(mobEffectJsonObj, "duration");
                int amplifier = GsonHelper.getAsInt(mobEffectJsonObj, "amplifier");
                String name = GsonHelper.getAsString(mobEffectJsonObj, "name");
                if (duration > 0 && amplifier >= 0) {
                    MobEffect mobEffect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(name));
                    if (mobEffect != null) {
                        effectInstances.add(new MobEffectInstance(mobEffect, duration, amplifier));
                    }
                }
            }
        }

        if (GsonHelper.isValidNode(json, "nbt")) {
            JsonObject nbt = GsonHelper.getAsJsonObject(json, "nbt");

            try {
                compoundTag = NbtUtils.snbtToStructure(nbt.toString());
            } catch (CommandSyntaxException var18) {
                System.out.println("" + recipeId + ": no nbt.");
            }
        }

        if (GsonHelper.isValidNode(json, "fluid")) {
            String fluidName = GsonHelper.getAsString(json, "fluid", "");
            fluid = (Fluid)ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
        }

        int waterLevel = GsonHelper.getAsInt(json, "water_level", 0);
        int waterSaturationLevel = GsonHelper.getAsInt(json, "water_saturation_level", 0);
        return new WaterLevelAndEffectRecipe(recipeId, group, ingredient, waterLevel, waterSaturationLevel, effectInstances, fluid, compoundTag);
    }

    public WaterLevelAndEffectRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf packetBuffer) {
        List<MobEffectInstance> mobEffectInstances = new ArrayList();
        String group = packetBuffer.readUtf();
        Ingredient ingredient = Ingredient.fromNetwork(packetBuffer);
        int waterLevel = packetBuffer.readInt();
        int waterSaturationLevel = packetBuffer.readInt();
        String fluidId = packetBuffer.readUtf();
        Fluid fluid = null;
        if (!fluidId.isEmpty()) {
            fluid = (Fluid)ForgeRegistries.FLUIDS.getValue(ResourceLocation.tryParse(fluidId));
        }

        CompoundTag compoundTag = packetBuffer.readNbt();
        int count = packetBuffer.readInt();

        for(int i = 0; i < count; ++i) {
            String mobEffectName = packetBuffer.readUtf();
            int duration = packetBuffer.readInt();
            int amplifier = packetBuffer.readInt();
            MobEffect mobEffect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(mobEffectName));
            mobEffectInstances.add(new MobEffectInstance(mobEffect, duration, amplifier));
        }

        return new WaterLevelAndEffectRecipe(recipeId, group, ingredient, waterLevel, waterSaturationLevel, mobEffectInstances, fluid, compoundTag);
    }

    public void toNetwork(FriendlyByteBuf buffer, WaterLevelAndEffectRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        recipe.getIngredient().toNetwork(buffer);
        buffer.writeInt(recipe.getWaterLevel());
        buffer.writeInt(recipe.getWaterSaturationLevel());
        buffer.writeUtf(recipe.getFluid() == null ? "" : recipe.getFluid().getRegistryName().toString());
        buffer.writeNbt(recipe.getCompoundTag());
        buffer.writeInt(recipe.getMobEffectInstances().size());
        Iterator var3 = recipe.getMobEffectInstances().iterator();

        while(var3.hasNext()) {
            MobEffectInstance mobEffectInstance = (MobEffectInstance)var3.next();
            buffer.writeUtf(mobEffectInstance.getEffect().getRegistryName().toString());
            buffer.writeInt(mobEffectInstance.getDuration());
            buffer.writeInt(mobEffectInstance.getAmplifier());
        }

    }
}
