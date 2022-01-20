package me.lizardofoz.drgflares;

import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.client.FlareEntityRenderer;
import me.lizardofoz.drgflares.client.FlareHUDRenderer;
import me.lizardofoz.drgflares.client.SettingsScreen;
import me.lizardofoz.drgflares.config.PlayerSettings;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.packet.PacketStuff;
import me.lizardofoz.drgflares.packet.SpawnFlareEntityS2CPacket;
import me.lizardofoz.drgflares.packet.SyncServerSettingsS2CPacket;
import me.lizardofoz.drgflares.packet.ThrowFlareC2SPacket;
import me.lizardofoz.drgflares.util.DRGFlareLimiter;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import me.lizardofoz.drgflares.util.FlareColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.Collection;

public class FabricEvents
{
    private FabricEvents() { }

    public static void initialize()
    {
        //Server Start
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            DRGFlareLimiter.clear();
            DRGFlarePlayerAspect.clear();
            FlareLightBlock.refreshBlockStates();
            ServerSettings.CURRENT.loadFromJson(ServerSettings.LOCAL.asJson());
            if (!ServerSettings.CURRENT.flareRecipesInSurvival.value)
            {
                Collection<Recipe<?>> values = server.getRecipeManager().values();
                values.removeIf(it -> it.getId().getNamespace().equals("drg_flares"));
                DRGFlaresUtil.setRecipes(server.getRecipeManager(), values);
            }
        });

        //Server Tick
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            DRGFlareLimiter.tick();
            DRGFlarePlayerAspect.tickAll();
        });

        //Player Join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            DRGFlareLimiter.onPlayerJoin(handler.player);
            DRGFlarePlayerAspect.onPlayerJoin(handler.player);
            DRGFlaresUtil.unlockFlareRecipes(handler.player);
            PacketStuff.sendSettingsSyncPacket(handler.player);
        });

        //Player Leave
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            DRGFlareLimiter.onPlayerLeave(handler.player);
            DRGFlarePlayerAspect.onPlayerLeave(handler.player);
        });

        //Packets
        ServerPlayNetworking.registerGlobalReceiver(ThrowFlareC2SPacket.IDENTIFIER, (server, player, handler, buf, sender) ->
                new ThrowFlareC2SPacket(buf).invokeOnServer(server, player));

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            Client.enableClient();
    }

    //Has to be a separate class or else JVM will try to load client-only classes on a dedicated server
    @Environment(EnvType.CLIENT)
    private static class Client
    {
        private static void enableClient()
        {
            //Keybinds
            KeyBindingHelper.registerKeyBinding(PlayerSettings.INSTANCE.throwFlareKey);
            if (DRGFlareRegistryFabric.getInstance().isClothConfigLoaded())
                KeyBindingHelper.registerKeyBinding(PlayerSettings.INSTANCE.flareModSettingsKey);

            //Custom HUD Widget
            HudRenderCallback.EVENT.register(FlareHUDRenderer::render);

            //Flare Entity Renderer
            EntityRendererRegistry.INSTANCE.register(DRGFlareRegistryFabric.getInstance().getFlareEntityType(), FlareEntityRenderer::new);

            //Client-Side Tick
            ClientTickEvents.START_CLIENT_TICK.register(client -> {
                PlayerEntity player = client.player;
                if (player == null)
                    return;
                DRGFlarePlayerAspect.clientLocal.tick();
                if (DRGFlareRegistryFabric.getInstance().isClothConfigLoaded() && PlayerSettings.INSTANCE.flareModSettingsKey.wasPressed())
                    client.setScreen(SettingsScreen.create(client.currentScreen));

                if (PlayerSettings.INSTANCE.throwFlareKey.wasPressed() && !DRGFlaresUtil.isRegenFlareOnCooldown(player))
                {
                    if (DRGFlarePlayerAspect.clientLocal.checkFlareToss(player))
                    {
                        PacketStuff.sendFlareThrowPacket(FlareColor.RandomColorPicker.unwrapRandom(PlayerSettings.INSTANCE.flareColor.value, true));
                        DRGFlarePlayerAspect.clientLocal.reduceFlareCount(player);
                    }
                    else if (ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
                        player.playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.1f, 1.7f);
                }
            });

            //Packets
            ClientPlayNetworking.registerGlobalReceiver(SyncServerSettingsS2CPacket.IDENTIFIER, (client, handler, buf, sender) ->
                    new SyncServerSettingsS2CPacket(buf).invokeOnClient());

            ClientPlayNetworking.registerGlobalReceiver(SpawnFlareEntityS2CPacket.IDENTIFIER, (client, handler, buf, sender) -> {
                SpawnFlareEntityS2CPacket packet = new SpawnFlareEntityS2CPacket();
                packet.read(buf);
                MinecraftClient.getInstance().execute(packet::spawnOnClient);
            });
        }
    }
}
