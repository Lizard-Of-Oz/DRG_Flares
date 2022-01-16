package me.lizardofoz.drgflares.util;

import lombok.Getter;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.entity.FlareEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DRGFlarePlayerAspect
{
    private static final Map<PlayerEntity, DRGFlarePlayerAspect> dataMap = new HashMap<>();

    public static final DRGFlarePlayerAspect clientLocal = new DRGFlarePlayerAspect();
    public static final DRGFlarePlayerAspect unknownOrigin = new DRGFlarePlayerAspect();

    public static void add(PlayerEntity player)
    {
        dataMap.put(player, new DRGFlarePlayerAspect());
    }

    public static void remove(PlayerEntity player)
    {
        dataMap.remove(player);
    }

    public static DRGFlarePlayerAspect get(PlayerEntity player)
    {
        return dataMap.get(player);
    }

    public static List<DRGFlarePlayerAspect> getValues()
    {
        return new ArrayList<>(dataMap.values());
    }

    public static void tickAll()
    {
        for (DRGFlarePlayerAspect value : dataMap.values())
            value.tick();
        unknownOrigin.tick();
    }

    //Yes I know, separating the static part like this is kinda ugly, but I don't think this mod is big enough to justify using Kotlin

    @Getter private int flaresLeft;
    @Getter private int flareRegenStatus;

    public int flareEntityCount;
    public int oldestFlareLifetime;
    public FlareEntity oldestFlare;

    public DRGFlarePlayerAspect()
    {
        reset();
    }

    public void reset()
    {
        flaresLeft = ServerSettings.CURRENT.regeneratingFlaresMaxCharges.value;
        flareRegenStatus = 0;
    }

    public void tick()
    {
        if (flaresLeft < ServerSettings.CURRENT.regeneratingFlaresMaxCharges.value)
        {
            if (flareRegenStatus < ServerSettings.CURRENT.regeneratingFlaresRechargeTime.value * 20)
                flareRegenStatus++;
            else
            {
                flareRegenStatus = 0;
                flaresLeft++;
            }
        }
    }
    public void reduceFlareCount(PlayerEntity player)
    {
        if (!DRGFlaresUtil.hasUnlimitedRegeneratingFlares(player) && ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
            flaresLeft = Math.max(0, flaresLeft - 1);
    }

    public boolean checkFlareToss(PlayerEntity player)
    {
        return flaresLeft > 0 || DRGFlaresUtil.hasUnlimitedRegeneratingFlares(player);
    }
}