package me.lizardofoz.drgflares.block;

import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.config.ServerSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FlareLightBlockEntity extends BlockEntity
{
    private int lifespan = 0;

    public FlareLightBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(DRGFlareRegistry.getInstance().getLightSourceBlockEntityType(), blockPos, blockState);
    }

    public void refresh(int lifeExtension)
    {
        lifespan = -lifeExtension;
    }

    private void tick()
    {
        if (lifespan++ >= ServerSettings.CURRENT.lightSourceLifespanTicks.value)
        {
            if (world.getBlockState(getPos()).getBlock() instanceof FlareLightBlock)
                world.setBlockState(getPos(), Blocks.AIR.getDefaultState());
            else
                markRemoved();
        }
    }

    public static void staticTick(World world, BlockPos blockPos, BlockState blockState, FlareLightBlockEntity blockEntity)
    {
        blockEntity.tick();
    }
}