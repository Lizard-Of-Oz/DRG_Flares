package me.lizardofoz.drgflares;

import me.lizardofoz.drgflares.client.FlareEntityRenderer;
import me.lizardofoz.drgflares.client.FlareHUDRenderer;
import me.lizardofoz.drgflares.config.PlayerSettings;
import me.lizardofoz.drgflares.packet.PacketStuff;
import me.lizardofoz.drgflares.packet.SpawnFlareEntityS2CPacket;
import me.lizardofoz.drgflares.packet.SyncServerSettingsS2CPacket;
import me.lizardofoz.drgflares.packet.ThrowFlareC2SPacket;
import me.lizardofoz.drgflares.util.DRGFlareLimiter;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.FlareColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;

public class FabricEvents extends CommonEvents
{
    public static void initialize()
    {
        DRGFlareLimiter.initOrReset();
        DRGFlarePlayerAspect.initOrReset();

        new FabricEvents();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            new Client();
    }

    private FabricEvents()
    {
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);
        ServerTickEvents.START_SERVER_TICK.register(server -> onServerTick());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoinServer(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerLeaveServer(handler.player));

        ServerPlayNetworking.registerGlobalReceiver(ThrowFlareC2SPacket.IDENTIFIER,
                (server, player, handler, buf, sender) -> new ThrowFlareC2SPacket(buf).invokeOnServer(server, player));
    }

    @Override
    protected void sendSettingsSyncS2CPacket(ServerPlayerEntity player)
    {
        PacketStuff.sendSettingsSyncS2CPacket(player);
    }

    //Has to be a separate class or else JVM will try to load client-only classes on a dedicated server
    @Environment(EnvType.CLIENT)
    private static class Client extends CommonEvents.Client
    {
        private Client()
        {
            ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onClientConnect());
            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onClientDisconnect());
            ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);

            //Keybinds
            KeyBindingHelper.registerKeyBinding(PlayerSettings.INSTANCE.throwFlareKey);
            if (DRGFlareRegistryFabric.getInstance().isClothConfigLoaded())
                KeyBindingHelper.registerKeyBinding(PlayerSettings.INSTANCE.flareModSettingsKey);

            //HUD and Entity Renderer
            HudRenderCallback.EVENT.register(FlareHUDRenderer::render);
            EntityRendererRegistry.INSTANCE.register(DRGFlareRegistryFabric.getInstance().getFlareEntityType(), (dispatcher, context) -> new FlareEntityRenderer(dispatcher));

            //Packets
            ClientPlayNetworking.registerGlobalReceiver(SyncServerSettingsS2CPacket.IDENTIFIER, (client, handler, buf, sender) ->
                    new SyncServerSettingsS2CPacket(buf).invokeOnClient());

            ClientPlayNetworking.registerGlobalReceiver(SpawnFlareEntityS2CPacket.IDENTIFIER, (client, handler, buf, sender) -> {
                SpawnFlareEntityS2CPacket packet = new SpawnFlareEntityS2CPacket();
                packet.read(buf);
                MinecraftClient.getInstance().execute(packet::spawnOnClient);
            });
        }

        @Override
        protected void sendFlareThrowC2SPacket(FlareColor color)
        {
            PacketStuff.sendFlareThrowC2SPacket(color);
        }
    }
}
