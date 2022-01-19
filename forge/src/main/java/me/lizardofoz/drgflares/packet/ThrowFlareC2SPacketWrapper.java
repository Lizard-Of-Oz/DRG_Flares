package me.lizardofoz.drgflares.packet;

import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import me.lizardofoz.drgflares.util.FlareColor;
import me.lizardofoz.inventorio.api.InventorioAPI;
import me.lizardofoz.inventorio.player.PlayerInventoryAddon;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;
import java.util.function.Supplier;

public class ThrowFlareC2SPacketWrapper
{
    public final FlareColor color;

    public ThrowFlareC2SPacketWrapper(FlareColor color)
    {
        this.color = color;
    }

    //Receiver's constructor
    public ThrowFlareC2SPacketWrapper(PacketByteBuf buf)
    {
        color = FlareColor.byId(buf.readByte());
    }

    //Sender's writer
    public void write(PacketByteBuf buf)
    {
        buf.writeByte(color.id);
    }

    //Receiver's consumer
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void invokeOnServer(Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() -> {
            PlayerEntity player = supplier.get().getSender();
            if (player == null)
                return;

            if (ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
            {
                DRGFlarePlayerAspect playerAspect = DRGFlarePlayerAspect.get(player);
                if (playerAspect != null)
                    playerAspect.tryThrowRegeneratingFlare(player, color);
            }
            else
            {
                //Here we exploit the fact, that when any tryFlare returns true, the subsequent tryFlare~s never get called
                if (DRGFlaresUtil.tryFlare(player, player.inventory.offHand) || DRGFlaresUtil.tryFlare(player, player.inventory.main))
                    return;
                if (DRGFlareRegistry.getInstance().isInventorioLoaded())
                {
                    PlayerInventoryAddon playerInventoryAddon = InventorioAPI.getInventoryAddon(player);
                    if (playerInventoryAddon != null)
                    {
                        if (DRGFlaresUtil.tryFlare(player, playerInventoryAddon.utilityBelt)
                                || DRGFlaresUtil.tryFlare(player, playerInventoryAddon.toolBelt)
                                || DRGFlaresUtil.tryFlare(player, playerInventoryAddon.deepPockets))
                            return; //Don't remove this, we abuse Java, lol
                    }
                }
            }
        });
        supplier.get().setPacketHandled(true);
    }
}