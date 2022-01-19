package me.lizardofoz.drgflares;

import me.lizardofoz.drgflares.client.SettingsScreen;
import me.lizardofoz.drgflares.config.PlayerSettings;
import me.lizardofoz.drgflares.item.FlareDispenserBehavior;
import me.lizardofoz.drgflares.packet.PacketStuff;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import java.util.Arrays;

@Mod("drg_flares")
public final class DRGFlaresForge extends DRGFlares
{
    public DRGFlaresForge()
    {
        DRGFlareRegistryForge.initialize();
        FlareDispenserBehavior.initialize();
        PacketStuff.initialize();
        ForgeEvents.initialize();

        if (FMLEnvironment.dist == Dist.CLIENT)
            Client.initialize();
    }

    //Has to be a separate class or else JVM will try to load client-only classes on a dedicated server
    @OnlyIn(Dist.CLIENT)
    private static class Client
    {
        private static void initialize()
        {
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
            GameOptions options = MinecraftClient.getInstance().options;
            //Keybinds and integration with Forge's Built-in Mod Menu.
            KeyBinding[] keys = Arrays.copyOf(
                    options.keysAll,
                    options.keysAll.length + (DRGFlareRegistry.getInstance().isClothConfigLoaded() ? 2 : 1));
            keys[keys.length - 1] = PlayerSettings.INSTANCE.throwFlareKey;
            if (DRGFlareRegistryForge.getInstance().isClothConfigLoaded())
            {
                ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (client, parent) -> SettingsScreen.create(parent));
                keys[keys.length - 2] = PlayerSettings.INSTANCE.flareModSettingsKey;
            }
            options.keysAll = keys;
        }
    }
}