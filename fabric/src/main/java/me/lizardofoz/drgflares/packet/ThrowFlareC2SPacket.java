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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class ThrowFlareC2SPacket
{
    public static final Identifier IDENTIFIER = new Identifier("drg_flares", "throw_flare");

    public final FlareColor color;

    public ThrowFlareC2SPacket(FlareColor color)
    {
        this.color = color;
    }

    //Receiver's constructor
    public ThrowFlareC2SPacket(PacketByteBuf buf)
    {
        color = FlareColor.byId(buf.readByte());
    }

    //Sender's writer
    public void write(PacketByteBuf buf)
    {
        buf.writeByte(color.id);
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    public void invokeOnServer(MinecraftServer server, PlayerEntity player)
    {
        //For some reason, trying to move this block of code into a different class causes a Flare to not spawn at all
        // or at least not be sent to the client
        server.execute(() -> {
            if (ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
            {
                DRGFlarePlayerAspect playerAspect = DRGFlarePlayerAspect.get(player);
                if (playerAspect != null)
                    playerAspect.tryThrowRegeneratingFlare(player, color);
            }
            else
            {
                //Here we exploit the fact, that when any tryFlare returns true, the subsequent tryFlare~s never get called
                if (DRGFlaresUtil.tryFlare(player, player.getInventory().offHand) || DRGFlaresUtil.tryFlare(player, player.getInventory().main))
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
    }
}