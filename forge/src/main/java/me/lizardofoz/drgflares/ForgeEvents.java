package me.lizardofoz.drgflares;

import me.lizardofoz.drgflares.client.FlareHUDRenderer;
import me.lizardofoz.drgflares.packet.PacketStuff;
import me.lizardofoz.drgflares.util.DRGFlareLimiter;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.FlareColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ForgeEvents extends CommonEvents
{
    public static void initialize()
    {
        DRGFlareLimiter.initOrReset();
        DRGFlarePlayerAspect.initOrReset();

        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
        if (FMLEnvironment.dist == Dist.CLIENT)
            MinecraftForge.EVENT_BUS.register(new Client());
    }

    private ForgeEvents() { }

    @SubscribeEvent
    public void onEvent(FMLServerStartedEvent event)
    {
        onServerStart(event.getServer());
    }

    @SubscribeEvent
    public void onEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        onPlayerJoinServer((ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    public void onEvent(PlayerEvent.PlayerLoggedOutEvent event)
    {
        onPlayerLeaveServer(event.getPlayer());
    }

    @SubscribeEvent
    public void onEvent(TickEvent.ServerTickEvent event)
    {
        onServerTick();
    }

    @Override
    protected void sendSettingsSyncS2CPacket(ServerPlayerEntity player)
    {
        PacketStuff.sendSettingsSyncS2CPacket(player);
    }

    @OnlyIn(Dist.CLIENT)
    private static class Client extends CommonEvents.Client
    {
        private Client() { }

        @SubscribeEvent
        public void onEvent(RenderGameOverlayEvent.Post event)
        {
            if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR)
                FlareHUDRenderer.render(event.getMatrixStack(), event.getPartialTicks());
        }

        @SubscribeEvent
        public void onEvent(ClientPlayerNetworkEvent.LoggedInEvent event)
        {
            onClientConnect();
        }

        @SubscribeEvent
        public void onEvent(ClientPlayerNetworkEvent.LoggedOutEvent event)
        {
            onClientDisconnect();
        }

        @SubscribeEvent
        public void onEvent(TickEvent.ClientTickEvent event)
        {
            onClientTick(MinecraftClient.getInstance());
        }

        @Override
        protected void sendFlareThrowC2SPacket(FlareColor color)
        {
            PacketStuff.sendFlareThrowC2SPacket(color);
        }
    }
}