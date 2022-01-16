package me.lizardofoz.drgflares.packet;

import io.netty.buffer.PooledByteBufAllocator;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.FlareColor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PacketStuff
{
    public static void sendSettingsSyncPacket(ServerPlayerEntity targetPlayer)
    {
        PacketByteBuf buf = new PacketByteBuf(PooledByteBufAllocator.DEFAULT.buffer());
        new SyncServerSettingsS2CPacket(ServerSettings.LOCAL.asJson()).write(buf);
        ServerPlayNetworking.send(targetPlayer, SyncServerSettingsS2CPacket.IDENTIFIER, buf);
    }

    public static void sendFlareThrowPacket(FlareColor color)
    {
        PacketByteBuf buf = new PacketByteBuf(PooledByteBufAllocator.DEFAULT.buffer());
        new ThrowFlareC2SPacket(color).write(buf);
        ClientPlayNetworking.send(ThrowFlareC2SPacket.IDENTIFIER, buf);
    }
}