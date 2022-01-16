package me.lizardofoz.drgflares.packet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

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
    @Environment(EnvType.CLIENT)
    public void invokeOnClient(Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() -> {
            ServerSettings.CURRENT.loadFromJson(settings);
            DRGFlarePlayerAspect.clientLocal.reset();
            FlareLightBlock.refreshBlockStates();
        });
        supplier.get().setPacketHandled(true);
    }
}