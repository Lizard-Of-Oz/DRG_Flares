package me.lizardofoz.drgflares.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.List;

public class ServerSettings extends AbstractSettings
{
    public static final ServerSettings LOCAL = new ServerSettings(new File(".", "config/drg_flares_server.json"));
    /**
     * When a player is connected to a remote server, it downloads the server's settings,
     *   but the local config doesn't get overwritten and will be restored upon starting a single-player world.
     */
    public static final ServerSettings CURRENT = new ServerSettings(LOCAL.asJson());

    //Regenerating flares
    public final SettingsEntry.Boolean regeneratingFlaresEnabled =
            new SettingsEntry.Boolean(true, "regenerating_flares_enabled");
    public final SettingsEntry.Integer regeneratingFlaresRechargeTime =
            new SettingsEntry.Integer(4, "regenerating_flare_recharge_time", 0, Integer.MAX_VALUE);
    public final SettingsEntry.Integer regeneratingFlaresMaxCharges =
            new SettingsEntry.Integer(4, "regenerating_flare_max_charges", 0, Integer.MAX_VALUE);
    public final SettingsEntry.Integer flareEntityLimitPerPlayer =
            new SettingsEntry.Integer(50, "flare_entity_limit_per_player", 0, Integer.MAX_VALUE);

    public final SettingsEntry.Boolean flareRecipesInSurvival =
            new SettingsEntry.Boolean(false, "flare_recipes_in_survival");

    public final SettingsEntry.Integer secondsUntilDimmingOut =
            new SettingsEntry.Integer(30, "seconds_until_dimming_out", 0, Integer.MAX_VALUE);
    public final SettingsEntry.Integer andThenSecondsUntilFizzlingOut =
            new SettingsEntry.Integer(20, "and_then_seconds_until_fizzling_out", 0, Integer.MAX_VALUE);
    public final SettingsEntry.Integer andThenSecondsUntilDespawn =
            new SettingsEntry.Integer(120, "and_then_seconds_until_despawn", 0, Integer.MAX_VALUE);

    public final SettingsEntry.Integer fullBrightnessLightLevel =
            new SettingsEntry.Integer(15, "full_brightness_light_level", 0, 15);
    public final SettingsEntry.Integer dimmedLightLevel =
            new SettingsEntry.Integer(8, "dimmed_light_level", 0, 15);

    public final SettingsEntry.Float flareGravity =
            new SettingsEntry.Float(1f, "flare_gravity", -10, 10);
    public final SettingsEntry.Float flareThrowSpeed =
            new SettingsEntry.Float(1f, "flare_throw_speed", 0, 10);
    public final SettingsEntry.Float flareThrowAngle =
            new SettingsEntry.Float(20f, "flare_throw_angle", -50, 50);
    public final SettingsEntry.Float flareSpeedBounceDivider =
            new SettingsEntry.Float(2f, "flare_speed_bounce_divider", 1, 100);

    public final SettingsEntry.Integer secondsUntilIdlingFlareGetsOptimized =
            new SettingsEntry.Integer(5, "seconds_until_idling_flare_gets_optimized", 0, Integer.MAX_VALUE);
    public final SettingsEntry.Integer lightSourceLifespanTicks =
            new SettingsEntry.Integer(10, "light_source_lifespan_ticks", 10, Integer.MAX_VALUE);
    public final SettingsEntry.Float lightSourceRefreshDistance =
            new SettingsEntry.Float(2f, "light_source_refresh_distance", 1, 50);
    public final SettingsEntry.Boolean creativeUnlimitedRegeneratingFlares =
            new SettingsEntry.Boolean(true, "creative_unlimited_regenerating_flares");
    public final SettingsEntry.Boolean serverSideLightSources =
            new SettingsEntry.Boolean(false, "server_side_light_sources");

    private final List<SettingsEntry<?>> entries = ImmutableList.of(
            regeneratingFlaresEnabled,
            regeneratingFlaresRechargeTime,
            regeneratingFlaresMaxCharges,
            flareEntityLimitPerPlayer,
            flareRecipesInSurvival,

            secondsUntilDimmingOut,
            andThenSecondsUntilFizzlingOut,
            andThenSecondsUntilDespawn,

            fullBrightnessLightLevel,
            dimmedLightLevel,

            secondsUntilIdlingFlareGetsOptimized,
            lightSourceLifespanTicks,
            lightSourceRefreshDistance,
            creativeUnlimitedRegeneratingFlares,
            serverSideLightSources,

            flareGravity,
            flareThrowSpeed,
            flareThrowAngle,
            flareSpeedBounceDivider);

    public ServerSettings(File file)
    {
        super();
        loadFromFile(file);
    }

    public ServerSettings(JsonObject settings)
    {
        super();
        loadFromJson(settings);
    }

    @Override
    protected List<SettingsEntry<?>> getEntries()
    {
        return entries;
    }

    public boolean unlimitedSurvivalFlares()
    {
        return regeneratingFlaresRechargeTime.value <= 0 || regeneratingFlaresMaxCharges.value <= 0;
    }
}