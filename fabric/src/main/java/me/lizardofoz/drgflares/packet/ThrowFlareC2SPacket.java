package me.lizardofoz.drgflares.packet;

import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.DRGFlareRegistryFabric;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.item.FlareItem;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import me.lizardofoz.drgflares.util.FlareColor;
import me.lizardofoz.inventorio.api.InventorioAPI;
import me.lizardofoz.inventorio.player.PlayerInventoryAddon;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.Map;

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
                if (playerAspect == null || !playerAspect.checkFlareToss(player) || DRGFlaresUtil.isRegenFlareOnCooldown(player) || player.isSpectator())
                    return;
                FlareEntity.throwFlare(player, color);
                Map<FlareColor, Item> itemTypes = DRGFlareRegistry.getInstance().getFlareItemTypes();
                player.getItemCooldownManager().set(itemTypes.get(FlareColor.RED), 5);
                player.incrementStat(Stats.USED.getOrCreateStat(itemTypes.get(color)));
                playerAspect.reduceFlareCount(player);
            }
            else
            {
                //Here we exploit the fact, that when any tryFlare returns true, the subsequent tryFlare~s never get called
                if (tryFlare(player, player.getInventory().offHand) || tryFlare(player, player.getInventory().main))
                    return;
                if (DRGFlareRegistryFabric.getInstance().isInventorioLoaded())
                {
                    PlayerInventoryAddon playerInventoryAddon = InventorioAPI.getInventoryAddon(player);
                    if (playerInventoryAddon != null)
                    {
                        if (tryFlare(player, playerInventoryAddon.utilityBelt)
                                || tryFlare(player, playerInventoryAddon.toolBelt)
                                || tryFlare(player, playerInventoryAddon.deepPockets))
                            return; //Don't remove this, we abuse Java, lol
                    }
                }
            }
        });
    }

    private boolean tryFlare(PlayerEntity player, List<ItemStack> inventorySection)
    {
        for (ItemStack itemStack : inventorySection)
        {
            Item item = itemStack.getItem();
            if (item instanceof FlareItem)
            {
                if (player.getItemCooldownManager().isCoolingDown(item))
                    return true;
                FlareEntity.throwFlare(player, DRGFlaresUtil.getFlareColorFromItem(itemStack));
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                if (!player.getAbilities().creativeMode)
                    itemStack.decrement(1);
                player.getItemCooldownManager().set(item, 5);
                return true;
            }
        }
        return false;
    }
}