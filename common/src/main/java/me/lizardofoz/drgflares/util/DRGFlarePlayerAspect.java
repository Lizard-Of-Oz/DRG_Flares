package me.lizardofoz.drgflares.util;

import lombok.Getter;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.entity.FlareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.stat.Stats;
import java.util.HashMap;
import java.util.Map;

public class DRGFlarePlayerAspect
{
    private static final Map<PlayerEntity, DRGFlarePlayerAspect> playerMap = new HashMap<>();

    public static final DRGFlarePlayerAspect clientLocal = new DRGFlarePlayerAspect();

    public static void initOrReset()
    {
        playerMap.clear();
        clientLocal.resetInst();
    }

    public static void onPlayerJoin(PlayerEntity player)
    {
        playerMap.put(player, new DRGFlarePlayerAspect());
    }

    public static void onPlayerLeave(PlayerEntity player)
    {
        playerMap.remove(player);
    }

    public static DRGFlarePlayerAspect get(PlayerEntity player)
    {
        return playerMap.get(player);
    }

    public static void tickAll()
    {
        for (DRGFlarePlayerAspect value : playerMap.values())
            value.tick();
    }

    //Yes I know, separating the static part like this is kinda ugly, but I don't think this mod is big enough to justify using Kotlin

    @Getter private int flaresLeft;
    @Getter private int flareRegenStatus;

    private DRGFlarePlayerAspect()
    {
        resetInst();
    }

    private void resetInst()
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
        else
            flaresLeft = ServerSettings.CURRENT.regeneratingFlaresMaxCharges.value;
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

    public void tryThrowRegeneratingFlare(PlayerEntity player, FlareColor color)
    {
        if (!checkFlareToss(player) || DRGFlaresUtil.isRegenFlareOnCooldown(player) || player.isSpectator())
            return;
        FlareEntity.throwFlare(player, color);
        Map<FlareColor, Item> itemTypes = DRGFlareRegistry.getInstance().getFlareItemTypes();
        player.getItemCooldownManager().set(itemTypes.get(FlareColor.RED), 5);
        player.incrementStat(Stats.USED.getOrCreateStat(itemTypes.get(color)));
        reduceFlareCount(player);
    }
}