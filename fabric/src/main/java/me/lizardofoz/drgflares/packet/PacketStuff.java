package me.lizardofoz.drgflares.packet;

import io.netty.buffer.PooledByteBufAllocator;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.FlareColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PacketStuff
{
    public static void sendSettingsSyncS2CPacket(ServerPlayerEntity targetPlayer)
    {
        PacketByteBuf buf = new PacketByteBuf(PooledByteBufAllocator.DEFAULT.buffer());
        new SyncServerSettingsS2CPacket(ServerSettings.LOCAL.asJson()).write(buf);
        ServerPlayNetworking.send(targetPlayer, SyncServerSettingsS2CPacket.IDENTIFIER, buf);
    }

    @Environment(EnvType.CLIENT)
    public static void sendFlareThrowC2SPacket(FlareColor color)
    {
        PacketByteBuf buf = new PacketByteBuf(PooledByteBufAllocator.DEFAULT.buffer());
        new ThrowFlareC2SPacket(color).write(buf);
        ClientPlayNetworking.send(ThrowFlareC2SPacket.IDENTIFIER, buf);
        DRGFlarePlayerAspect.clientLocal.reduceFlareCount(MinecraftClient.getInstance().player);
    }
}