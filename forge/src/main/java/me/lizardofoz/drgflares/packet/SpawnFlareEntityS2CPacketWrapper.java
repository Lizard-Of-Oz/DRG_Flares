package me.lizardofoz.drgflares.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SpawnFlareEntityS2CPacketWrapper
{
    private SpawnFlareEntityS2CPacket packet = new SpawnFlareEntityS2CPacket();

    public SpawnFlareEntityS2CPacketWrapper(SpawnFlareEntityS2CPacket packet)
    {
        this.packet = packet;
    }

    //Receiver's constructor
    public SpawnFlareEntityS2CPacketWrapper(PacketByteBuf buf)
    {
        packet.read(buf);
    }

    //Sender's writer
    public void write(PacketByteBuf buf)
    {
        packet.write(buf);
    }

    //Receiver's consumer
    public void invokeOnClient(Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() -> packet.spawnOnClient());
        supplier.get().setPacketHandled(true);
    }
}