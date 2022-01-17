package me.lizardofoz.drgflares.block;

import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.config.ServerSettings;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Tickable;

public class FlareLightBlockEntity extends BlockEntity implements Tickable
{
    private int lifespan = 0;

    public FlareLightBlockEntity()
    {
        super(DRGFlareRegistry.getInstance().getLightSourceBlockEntityType());
    }

    public void refresh(int lifeExtension)
    {
        lifespan = -lifeExtension;
    }

    @Override
    public void tick()
    {
        if (lifespan++ >= ServerSettings.CURRENT.lightSourceLifespanTicks.value)
        {
            if (world.getBlockState(getPos()).getBlock() instanceof FlareLightBlock)
                world.setBlockState(getPos(), Blocks.AIR.getDefaultState());
            else
                markInvalid();
        }
    }
}