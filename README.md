# DRG Flares (Forge / Fabric)

## Showcase Video
[![Video Demonstration](https://user-images.githubusercontent.com/701551/149723954-4e4b0d37-10c3-4a06-90c8-85882124f1b9.png)](https://youtu.be/IlnwIly0Qh0)

If you want to support the development of mods like this, you can do it [here](https://boosty.to/lizardofoz).

[![Support me](https://static.boosty.to/assets/images/boostyLogo.WbAVE.svg)](https://boosty.to/lizardofoz)

Every dollar (or, in this case, ruble) counts and allows me to spend more time making various projects for your enjoyment.

## About
_"Did I hear a Rock and Stone?"_

This mod introduces High-Intensity Flares from Deep Rock Galactic into Minecraft in several ways.

Flares are throwable temporary light sources which bounce off of surfaces.

After 30 seconds, a flare will partially lose its brightness, and after additional 20 seconds it will fizzle out completely. For the performance purposes, 120 seconds later a fizzled out flare will despawn.

Most flare's parameters, such as durations, light levels, throw speed, etc [are configurable](#Settings).

Flares come in Minecraft's standard 16 colors, including Red, Pink, and Black... wait, how does that work?

Please note that a flying flare may cause a noticeable FPS drop due to how Minecraft's lighting engine works.

### Regenerating Flares
As seen in DRG, a player has up to 4 flares which regenerate over time, which can be thrown with a dedicated "Throw Flare" keybind ('v' by default) 

Enabled by default, but can be turned off in favor of (or ran simultaneously with) the following option. 

### Survival Flares
Disabled by default. If enabled, players will be able to craft and use flares as any other Minecraft item. Flares can also be shot from Dispensers.

If Regenerating Flares are disabled, "Throw Flare" keybind will throw a Survival Flare item if available. If [Inventorio](https://github.com/Lizard-Of-Oz/Inventorio) mod is present, it will scan its additional slots as well.

## Client-Side-Only Mode

Regenerating flares work even without the mod being installed on the server!

In this case, nobody else will see your flares, even if they also have the mod installed. After all, flares don't actually exist in this mode and just faked on your side.   

Survival Flares and Server-Side Light Sources also won't work unless installed on the server.

**Warning!** As with most client-side mods, there's a chance to trigger anti-cheat systems installed on public servers. Use it at your own risk.

## Installation
Grab the jar file from CurseForge: [Fabric Version](https://www.curseforge.com/minecraft/mc-mods/drg-flares) and [Forge Version](https://www.curseforge.com/minecraft/mc-mods/drg-flares-forge)

Copy the jar file into `%root_folder%/mods/` alongside other mods.

Fabric version has a dependency: [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api), which is used by most Fabric mods anyway.

It's also highly recommended to install Cloth Config [(Fabric)](https://www.curseforge.com/minecraft/mc-mods/cloth-config) or [(Forge)](https://www.curseforge.com/minecraft/mc-mods/cloth-config-forge) on the Client to enable the in-game settings menu.

## Settings
Cloth Config is an optional client-side dependency that enables the in-game settings menu.
Without it, configs have to be manually edited at `%root_folder%/config/drg_flares_client.json` and `%root_folder%/config/drg_flares_server.json` respectively.

When you join a remote server featuring this mod, your settings will be synchronized with the servers' during the current play session without overriding your local config.

#### Player Settings
* `flare_color (default: random_bright_only)` - The color of Regenerating Flares (if enabled). Can be `random`, `random_bright_only`, or one of 16 Minecraft dye color names.
* `flare_ui_x (0.8)` - Horizontal position of the Flare UI widget.
* `flare_ui_y (1.0)` - Vertical position of the Flare UI widget.
* `flare_sound_volume (100)` - Flare sound volume from 0% to 200%.
* `flare_button_hint (true)` - Display the key assigned to throwing a flare on the HUD. The hint won't be displayed for non-letter keybinds.

#### Server Settings
* `regenerating_flares_enabled (default: true)` - Flares regenerate over time and can be thrown with a press of a button, as seen in Deep Rock Galactic.  
* `regenerating_flare_recharge_time (4)` - Sets the Flare regeneration period in seconds. Set to 0 for unlimited Flares in Survival.
* `regenerating_flare_max_charges (4)` - Max amount of Regenerating Flares you can hold at any given time. Set to 0 for unlimited Flares in Survival.
* `flare_entity_limit_per_player (50)` - Too many entities (which flares are) may cause lag. Each player has their own threshold, after exceeding which their oldest flares will get deleted. Flares of non-player origin (e.g. a dispenser or modded things) go into their own shared pool. Set to 0 for Unlimited.
* `flare_recipes_in_survival (false)` - Allows to craft Flare Items in Survival.

 

* `seconds_until_dimming_out (30)` - Flare starts at its max brightness and dims out after a set period of time in seconds.
* `and_then_seconds_until_fizzling_out (20)` - After being dimmed out for a set period of time, a flare fizzles out completely.
* `and_then_seconds_until_despawn (120)` - After being fizzled for a set period of time, a flare despawns.

 

* `full_brightness_light_level (15)` - Light level a flare emits after being thrown.
* `dimmed_light_level (8)` - Light level a flare emits when dimmed out.

 

* `seconds_until_idling_flare_gets_optimized (5)` -Flares' movement rarely needs to be calculated after it lands. This sets the threshold when an idling flare disables movement calculation.
* `light_source_lifespan_ticks (10)` - Flying flares leave fake invisible light sources behind to achieve its ability to light things up. This value sets for how long said lights should stay. Doesn't affect idling flares, since they create a light source as close to the flare as possible, and only once.
* `light_source_refresh_distance (2)` - Sets the max distance between the flare and the old light source at which a new light source won't be created.
* `light_source_search_distance (2)` - Sets the max cubic distance from a flare it will search for a valid space to put a Light Source
* `creative_unlimited_regenerating_flares (true)` - Allows unlimited Regenerating Flares (if enabled) in Creative Mode.
* `server_side_light_sources (false)` - We don't need temporary light sources from flares to exist on the server side. While it COULD be useful for temporal mob-proofing, enabling it lowers server performance, flares may interfere with the flow of liquids or cause observers to fire.

 

* `flare_gravity (1)` - The bigger the value, the faster a flare falls.
* `flare_throw_speed (1)` - The bigger the value, the faster and farther a flare goes.
* `flare_throw_angle (20)` - By default, a flare gets thrown at a slightly higher angle than set by the crosshair. This effect gets scaled down as the aiming angle gets higher.
* `flare_speed_bounce_divider (2)` - Speed of a flare gets divided by this number upon hitting a block.

Tip: you can configure flares to "warm up", if `full_brightness_light_level` will be set lower, than `dimmed_light_level`.

## Use in modpacks and with other mods
You can include this mod in a modpack or as a dependency for your own mod.

I just ask you to respect my work and include it in a way that would count as a download of my mod by CurseForge and its Reward Program.

* For Modpacks, CurseForge by default links a mod in the modpack manifest when you add it. Use _that_ instead of embedding the mod's jar into the modpack.
* For Mods, don't embed the mod's jar inside your mod, but mark it as a dependency. 

If you want to use this mode as a dependency, I recommend using [CurseMaven](https://www.cursemaven.com/).

Be advised that you need to manually keep track of the latest version available.   

```
repositories {
  maven {
    url "https://cursemaven.com"
    content {
      includeGroup "curse.maven"
    }
  }
}

dependencies {
  modCompileOnly "curse.maven:drg_flares-568533:4619250" //Fabric
  modCompileOnly "curse.maven:drg_flares-568536:4619249" //Forge
}
```