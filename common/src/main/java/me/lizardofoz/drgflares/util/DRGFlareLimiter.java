package me.lizardofoz.drgflares.util;

import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.entity.FlareEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

/**
 * Here's how this whole thing works:<br/>
 * During a tick, each flare reports itself to the DRGFlareLimiter, and the oldest flare <s>per player</s> gets remembered.<br/>
 * Upon a new tick, if there are more than N flares, the oldest one (which we've remembered during the report process) gets deleted<br/>
 * This means each tick we can delete 1 flare above the limit.<br/>
 * While it means having several flares generated per tick will cause an overflow eventually,<br/>
 *   one has to go out of their way to make this happen. This is called griefing, and there are many way to do it anyway.<br/><br/>
 *
 * Note: all flares of non-player origin (e.g. a dispenser or a modded thing) go into their own shared pool, as if it would be one more player.
 */
public class DRGFlareLimiter
{
    public static void tick()
    {
        if (ServerSettings.CURRENT.flareEntityLimitPerPlayer.value <= 0)
            return;
        List<DRGFlarePlayerAspect> playerAspects = DRGFlarePlayerAspect.getValues();
        playerAspects.add(DRGFlarePlayerAspect.unknownOrigin);
        for (DRGFlarePlayerAspect playerAspect : playerAspects)
        {
            if (playerAspect.flareEntityCount > ServerSettings.CURRENT.flareEntityLimitPerPlayer.value && playerAspect.oldestFlare != null)
                playerAspect.oldestFlare.kill();
            playerAspect.flareEntityCount = 0;
            playerAspect.oldestFlareLifetime = -1;
            playerAspect.oldestFlare = null;
        }
    }

    public static void reportFlare(FlareEntity entity)
    {
        if (ServerSettings.CURRENT.flareEntityLimitPerPlayer.value <= 0)
            return;
        DRGFlarePlayerAspect aspect = DRGFlarePlayerAspect.unknownOrigin;
        if (entity.getOwner() instanceof PlayerEntity)
        {
            DRGFlarePlayerAspect livingPlayerAspect = DRGFlarePlayerAspect.get((PlayerEntity) entity.getOwner());
            if (livingPlayerAspect != null)
                aspect = livingPlayerAspect;
        }
        aspect.flareEntityCount++;
        if (entity.lifespan > aspect.oldestFlareLifetime)
        {
            aspect.oldestFlareLifetime = entity.lifespan;
            aspect.oldestFlare = entity;
        }
    }
}