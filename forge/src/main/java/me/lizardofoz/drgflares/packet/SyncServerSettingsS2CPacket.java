package me.lizardofoz.drgflares.packet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.ServerSyncMode;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SyncServerSettingsS2CPacket
{
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

    //Receiver's consumer
    public void invokeOnClient(Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() -> {
            DRGFlareRegistry.getInstance().serverSyncMode = ServerSyncMode.SYNC_WITH_SERVER;
            ServerSettings.CURRENT.loadFromJson(settings);
            FlareLightBlock.refreshBlockStates();
        });
        supplier.get().setPacketHandled(true);
    }
}