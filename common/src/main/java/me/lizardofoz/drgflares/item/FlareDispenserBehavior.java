package me.lizardofoz.drgflares.item;

import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public class FlareDispenserBehavior extends ProjectileDispenserBehavior
{
    public static void initialize()
    {
        FlareDispenserBehavior behavior = new FlareDispenserBehavior();
        for (Item item : DRGFlareRegistry.getInstance().getFlareItemTypes().values())
            DispenserBlock.registerBehavior(item, behavior);
    }

    @Override
    protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack)
    {
        FlareEntity flare = new FlareEntity(world, DRGFlaresUtil.getFlareColorFromItem(stack));
        flare.setPos(position.getX(), position.getY(), position.getZ());
        return flare;
    }

    @Override
    protected float getForce()
    {
        return ServerSettings.CURRENT.flareThrowSpeed.value;
    }
}