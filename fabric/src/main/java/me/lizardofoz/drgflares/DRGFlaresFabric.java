package me.lizardofoz.drgflares;

import me.lizardofoz.drgflares.item.FlareDispenserBehavior;
import net.fabricmc.api.ModInitializer;

public final class DRGFlaresFabric extends DRGFlares implements ModInitializer
{
    public void onInitialize()
    {
        DRGFlareRegistryFabric.initialize();
        FlareDispenserBehavior.initialize();
        FabricEvents.initialize();
    }
}