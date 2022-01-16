package me.lizardofoz.drgflares.item;

import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FlareItem extends Item
{
    public FlareItem(Item.Settings settings)
    {
        super(settings);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
    {
        ItemStack itemStack = player.getStackInHand(hand);
        if (!world.isClient)
            FlareEntity.throwFlare(player, DRGFlaresUtil.getFlareColorFromItem(itemStack));
        if (!player.abilities.creativeMode)
            itemStack.decrement(1);
        player.getItemCooldownManager().set(this, 5);
        player.incrementStat(Stats.USED.getOrCreateStat(this));
        return TypedActionResult.success(itemStack, world.isClient());
    }
}