package me.lizardofoz.drgflares.block;

import lombok.Getter;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.config.ServerSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

@SuppressWarnings("deprecation")
public class FlareLightBlock extends Block implements BlockEntityProvider
{
    public static final IntProperty LIGHT_LEVEL = Properties.LEVEL_15;

    @Getter private static BlockState fullBrightnessBlockState;
    @Getter private static BlockState dimmedOutBlockState;

    public static void refreshBlockStates()
    {
        BlockState defaultState = DRGFlareRegistry.getInstance().getLightSourceBlockType().getDefaultState();
        fullBrightnessBlockState = defaultState.with(LIGHT_LEVEL, ServerSettings.CURRENT.fullBrightnessLightLevel.value);
        dimmedOutBlockState = defaultState.with(LIGHT_LEVEL, ServerSettings.CURRENT.dimmedLightLevel.value);
    }

    public FlareLightBlock(Settings settings)
    {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView world)
    {
        return new FlareLightBlockEntity();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
    {
        builder.add(LIGHT_LEVEL);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
    {
        return VoxelShapes.empty();
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos)
    {
        return true;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos)
    {
        return 1;
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state)
    {
        return PistonBehavior.DESTROY;
    }
}