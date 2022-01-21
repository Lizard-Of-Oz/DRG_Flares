package me.lizardofoz.drgflares.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.DRGFlares;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.item.FlareItem;
import me.lizardofoz.drgflares.mixin.RecipeManagerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DRGFlaresUtil
{
    private DRGFlaresUtil() { }

    private static List<Recipe<?>> flareRecipesCache;

    public static void unlockFlareRecipes(PlayerEntity player)
    {
        try
        {
            if (ServerSettings.CURRENT.flareRecipesInSurvival.value)
            {
                if (flareRecipesCache == null)
                    flareRecipesCache = player.world.getServer().getRecipeManager().values().stream()
                            .filter(it -> it.getId().getNamespace().equals("drg_flares"))
                            .collect(Collectors.toList());
                player.unlockRecipes(flareRecipesCache);
            }
        }
        catch (Throwable e)
        {
            DRGFlares.LOGGER.error("Failed to Unlock Flare Recipes for " + player, e);
        }
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
        return (player.getAbilities().creativeMode && ServerSettings.CURRENT.creativeUnlimitedRegeneratingFlares.value) || ServerSettings.CURRENT.unlimitedSurvivalFlares();
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

    public static boolean tryFlare(PlayerEntity player, List<ItemStack> inventorySection)
    {
        for (ItemStack itemStack : inventorySection)
        {
            Item item = itemStack.getItem();
            if (item instanceof FlareItem)
            {
                if (player.getItemCooldownManager().isCoolingDown(item))
                    return true;
                FlareEntity.throwFlare(player, DRGFlaresUtil.getFlareColorFromItem(itemStack));
                if (!player.getAbilities().creativeMode)
                    itemStack.decrement(1);
                player.getItemCooldownManager().set(item, 5);
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                return true;
            }
        }
        return false;
    }

    //We had to move these 2 methods outside, so that a Dedicated Server won't try to load Client-Only classes
    @Environment(EnvType.CLIENT)
    public static void playSoundFromEntityOnClient(Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch)
    {
        MinecraftClient.getInstance().getSoundManager().play(new EntityTrackingSoundInstance(sound, category, volume, pitch, entity));
    }

    @Environment(EnvType.CLIENT)
    public static void addEntityOnClient(World world, Entity entity)
    {
        ((ClientWorld) world).addEntity(entity.getId(), entity);
    }
}