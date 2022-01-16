package me.lizardofoz.drgflares.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.mixin.RecipeManagerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DRGFlaresUtil
{
    private static List<Recipe<?>> flareRecipesCache;

    private DRGFlaresUtil() { }

    public static void unlockFlareRecipes(PlayerEntity player)
    {
        if (ServerSettings.CURRENT.flareRecipesInSurvival.value)
            player.unlockRecipes(getFlareRecipes(player.world.getServer()));
    }

    private static List<Recipe<?>> getFlareRecipes(MinecraftServer server)
    {
        if (flareRecipesCache == null)
            flareRecipesCache = server.getRecipeManager().values().stream()
                    .filter(it -> it.getId().getNamespace().equals("drg_flares"))
                    .collect(Collectors.toList());
        return flareRecipesCache;
    }

    public static void setRecipes(RecipeManager recipeManager, Iterable<Recipe<?>> recipes)
    {
        Map<RecipeType<?>, Map<Identifier, Recipe<?>>> map = Maps.newHashMap();
        recipes.forEach((recipe) -> {
            Map<Identifier, Recipe<?>> map2 = map.computeIfAbsent(recipe.getType(), (recipeType) -> Maps.newHashMap());
            Recipe<?> recipe2 = map2.put(recipe.getId(), recipe);
            if (recipe2 != null)
                throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.getId());
        });
        ImmutableMap<RecipeType<?>, Map<Identifier, Recipe<?>>> result = ImmutableMap.copyOf(map);
        ((RecipeManagerAccessor) recipeManager).setRecipes(result);
    }

    public static boolean isOnRemoteServer()
    {
        try
        {
            return !MinecraftClient.getInstance().getNetworkHandler().getConnection().isLocal();
        }
        catch (Throwable ignored)
        {
            return false;
        }
    }

    public static int getVoidDamageLevel(World world)
    {
        return -64;
    }

    public static boolean hasUnlimitedRegeneratingFlares(PlayerEntity player)
    {
        return (player.abilities.creativeMode && ServerSettings.CURRENT.creativeUnlimitedRegeneratingFlares.value) || ServerSettings.CURRENT.unlimitedSurvivalFlares();
    }

    public static boolean isRegenFlareOnCooldown(PlayerEntity player)
    {
        return player.getItemCooldownManager().isCoolingDown(DRGFlareRegistry.getInstance().getFlareItemTypes().get(FlareColor.RED));
    }

    public static FlareColor getFlareColorFromItem(ItemStack stack)
    {
        for (Map.Entry<FlareColor, Item> entry : DRGFlareRegistry.getInstance().getFlareItemTypes().entrySet())
        {
            if (stack.getItem().equals(entry.getValue()))
                return entry.getKey();
        }
        return FlareColor.RED;
    }

    @Environment(EnvType.CLIENT)
    public static void playSoundFromEntityOnClient(Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch)
    {
        MinecraftClient.getInstance().getSoundManager().play(new EntityTrackingSoundInstance(sound, category, volume, pitch, entity));
    }
}