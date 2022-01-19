package me.lizardofoz.drgflares.packet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.ServerSyncMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SyncServerSettingsS2CPacket
{
    public static final Identifier IDENTIFIER = new Identifier("drg_flares", "sync_server_settings");

    public final JsonObject settings;

    public SyncServerSettingsS2CPacket(JsonObject settings)
    {
        this.settings = settings;
    }

    //Receiver's constructor
    public SyncServerSettingsS2CPacket(PacketByteBuf buf)
    {
        settings = new Gson().fromJson(buf.readString(), JsonObject.class);
    }

    //Sender's writer
    public void write(PacketByteBuf buf)
    {
        buf.writeString(settings.toString());
    }

    @Environment(EnvType.CLIENT)
    public void invokeOnClient()
    {
        MinecraftClient.getInstance().execute(() -> {
            DRGFlareRegistry.getInstance().serverSyncMode = ServerSyncMode.SYNC_WITH_SERVER;
            ServerSettings.CURRENT.loadFromJson(settings);
            FlareLightBlock.refreshBlockStates();
        });
    }
}