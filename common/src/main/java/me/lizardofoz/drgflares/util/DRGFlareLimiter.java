package me.lizardofoz.drgflares.util;

import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.entity.FlareEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import java.util.HashMap;
import java.util.Map;

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
    private static final Map<PlayerEntity, TrackerInstance> playerMap = new HashMap<>();

    private DRGFlareLimiter() { }

    public static void initOrReset()
    {
        playerMap.clear();
        playerMap.put(null, new TrackerInstance());
    }

    public static void onPlayerJoin(PlayerEntity playerEntity)
    {
        playerMap.put(playerEntity, new TrackerInstance());
    }

    public static void onPlayerLeave(PlayerEntity playerEntity)
    {
        playerMap.remove(playerEntity);
    }

    public static void tick()
    {
        if (ServerSettings.CURRENT.flareEntityLimitPerPlayer.value <= 0)
            return;
        for (TrackerInstance tracker : playerMap.values())
        {
            if (tracker.flareEntityCount > ServerSettings.CURRENT.flareEntityLimitPerPlayer.value && tracker.oldestFlare != null)
                tracker.oldestFlare.kill();
            tracker.reset();
        }
    }

    public static void reportFlare(FlareEntity entity)
    {
        if (ServerSettings.CURRENT.flareEntityLimitPerPlayer.value <= 0)
            return;

        TrackerInstance aspect = null;
        Entity owner = entity.getOwner();
        if (owner instanceof PlayerEntity)
            aspect = playerMap.get(owner);
        if (aspect == null)
            aspect = playerMap.get(null);

        aspect.flareEntityCount++;
        if (entity.lifespan > aspect.oldestFlareLifetime)
        {
            aspect.oldestFlareLifetime = entity.lifespan;
            aspect.oldestFlare = entity;
        }
    }

    private static class TrackerInstance
    {
        private int flareEntityCount;
        private int oldestFlareLifetime;
        private FlareEntity oldestFlare;

        private void reset()
        {
            flareEntityCount = 0;
            oldestFlareLifetime = -1;
            oldestFlare = null;
        }
    }
}