package me.lizardofoz.drgflares.packet;

import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.FlareColor;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class PacketStuff
{
    private static final String PROTOCOL_VERSION = "1.0";

    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new Identifier("drg_flares", "packets"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private PacketStuff() { }

    public static void initialize()
    {
        INSTANCE.registerMessage(0, ThrowFlareC2SPacketWrapper.class,
                ThrowFlareC2SPacketWrapper::write,
                ThrowFlareC2SPacketWrapper::new,
                ThrowFlareC2SPacketWrapper::invokeOnServer);
        INSTANCE.registerMessage(1, SyncServerSettingsS2CPacket.class,
                SyncServerSettingsS2CPacket::write,
                SyncServerSettingsS2CPacket::new,
                SyncServerSettingsS2CPacket::invokeOnClient);
        INSTANCE.registerMessage(2, SpawnFlareEntityS2CPacketWrapper.class,
                SpawnFlareEntityS2CPacketWrapper::write,
                SpawnFlareEntityS2CPacketWrapper::new,
                SpawnFlareEntityS2CPacketWrapper::invokeOnClient);
    }

    public static void sendSettingsSyncPacket(ServerPlayerEntity player)
    {
        INSTANCE.sendTo(new SyncServerSettingsS2CPacket(ServerSettings.LOCAL.asJson()), player.networkHandler.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendFlareThrowPacket(FlareColor color)
    {
        INSTANCE.sendToServer(new ThrowFlareC2SPacketWrapper(color));
    }

    public static Packet<?> sendFlareSpawnPacket(SpawnFlareEntityS2CPacket packet)
    {
        return INSTANCE.toVanillaPacket(new SpawnFlareEntityS2CPacketWrapper(packet), NetworkDirection.PLAY_TO_CLIENT);
    }
}