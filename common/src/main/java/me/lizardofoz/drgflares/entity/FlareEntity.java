package me.lizardofoz.drgflares.entity;

import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.DRGFlares;
import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.block.FlareLightBlockEntity;
import me.lizardofoz.drgflares.config.PlayerSettings;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.DRGFlareLimiter;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import me.lizardofoz.drgflares.util.FlareColor;
import me.lizardofoz.drgflares.util.ServerSyncMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class FlareEntity extends ThrownEntity
{
    //Persistent Entity Data
    public FlareColor color;
    public int lifespan = -1;

    //Temporary/Service Entity Data
    public int bounceCount = 1;
    public float rotation = 0;
    private int idleTicks = 0;
    private float prevPartialTick = 0;

    private BlockPos lightBlockPos = null;
    private BlockPos lastHitBlockPos = null;
    private BlockState lastHitBlockState = null;

    public FlareEntity(World world, FlareColor color)
    {
        super(DRGFlareRegistry.getInstance().getFlareEntityType(), world);
        this.color = FlareColor.RandomColorPicker.unwrapRandom(color, false);
        //Technically, we shouldn't receive "Random" as a Flare Color here, but just in case...
    }

    private FlareEntity(LivingEntity owner, FlareColor color)
    {
        super(DRGFlareRegistry.getInstance().getFlareEntityType(), owner, owner.getWorld());
        this.color = FlareColor.RandomColorPicker.unwrapRandom(color, false);
        //Technically, we shouldn't receive "Random" as a Flare Color here, but just in case...
    }

    //Note: we call this only on the server's side
    //UPD: if this called on the client's side, we know it has to be in the client-side-only mode
    public static FlareEntity throwFlare(@NotNull LivingEntity owner, FlareColor color)
    {
        float throwAngle = ServerSettings.CURRENT.flareThrowAngle.value;
        float pitchModifier = Math.min(throwAngle, Math.max(0, owner.getPitch() + (95 - throwAngle)));

        FlareEntity flareEntity = new FlareEntity(owner, color);
        flareEntity.setPos(flareEntity.getX(), flareEntity.getY() - 0.5, flareEntity.getZ());
        flareEntity.setVelocity(owner, owner.getPitch() - pitchModifier, owner.getYaw(), 0, 0.75f * ServerSettings.CURRENT.flareThrowSpeed.value, 1);
        if (!owner.getWorld().isClient)
            owner.getWorld().spawnEntity(flareEntity);
        else
        {
            //EntityId magic to avoid clashing with entityId-s of real entities
            int randomNegativeId = flareEntity.getId() - 100000;
            while (owner.getWorld().getEntityById(randomNegativeId) != null)
                randomNegativeId = owner.getWorld().getRandom().nextInt(1000000) - 2000000;
            flareEntity.setId(randomNegativeId);
            DRGFlaresUtil.addEntityOnClient(owner.getWorld(), flareEntity);
        }
        return flareEntity;
    }

    public static FlareEntity make(EntityType<FlareEntity> entityType, World world)
    {
        return new FlareEntity(world, FlareColor.RED);
    }

    @Override
    protected void initDataTracker()
    {
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag)
    {
        super.writeNbt(tag);
        tag.putInt("lifespan", lifespan);
        tag.putShort("color", (short) color.id);
        return tag;
    }

    @Override
    public void readNbt(NbtCompound tag)
    {
        super.readNbt(tag);
        lifespan = tag.getInt("lifespan");
        color = FlareColor.byId(tag.getShort("color"));
    }

    @Override
    public boolean isAttackable()
    {
        return false;
    }

    @Override
    protected float getGravity()
    {
        return 0.024f * ServerSettings.CURRENT.flareGravity.value;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket()
    {
        return DRGFlareRegistry.getInstance().createSpawnFlareEntityPacket(this);
    }

    @Environment(EnvType.CLIENT)
    public void frame(float partialTick)
    {
        if (MinecraftClient.getInstance().isPaused())
            return;
        while (partialTick <= prevPartialTick)
            prevPartialTick -=1;
        float delta = partialTick - prevPartialTick;
        prevPartialTick = partialTick;

        if (getVelocity().lengthSquared() < 0.1 && bounceCount > 2)
            rotation = 0;
        else
            rotation += 10.0f * delta / bounceCount;
    }

    public boolean isLit()
    {
        return lifespan < (ServerSettings.CURRENT.secondsUntilDimmingOut.value + ServerSettings.CURRENT.andThenSecondsUntilFizzlingOut.value) * 20;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult)
    {
        super.onBlockHit(blockHitResult);
        BlockPos hitBlockPos = blockHitResult.getBlockPos();

        bounceCount++;
        if (getWorld().isClient && !isTouchingWater())
        {
            float pitch = 1.1f + getWorld().random.nextFloat() * 0.3f;
            float volume = PlayerSettings.INSTANCE.flareSoundVolume.value / 100.0f;
            float farVolume = volume < 0.25f ? volume * 3 : volume < 0.5f ? volume * 2 : volume;
            DRGFlaresUtil.playSoundFromEntityOnClient(this, DRGFlareRegistry.getInstance().FLARE_BOUNCE_EVENT, SoundCategory.MASTER, volume, pitch);
            DRGFlaresUtil.playSoundFromEntityOnClient(this, DRGFlareRegistry.getInstance().FLARE_BOUNCE_FAR_EVENT, SoundCategory.MASTER, farVolume * 3, pitch);
        }

        //If we don't disable gravity, a flare will just bob up and down when laying on the ground
        if (hitBlockPos.equals(lastHitBlockPos) && getVelocity().lengthSquared() < 0.01)
        {
            setVelocity(Vec3d.ZERO);
            setNoGravity(true);
            return;
        }
        else
        {
            lastHitBlockPos = hitBlockPos;
            lastHitBlockState = getWorld().getBlockState(hitBlockPos);
        }

        double speedDivider = ServerSettings.CURRENT.flareSpeedBounceDivider.value;
        Vec3d velocity = getVelocity();
        if (blockHitResult.getSide() == Direction.EAST || blockHitResult.getSide() == Direction.WEST)
            setVelocity(-velocity.x / speedDivider, velocity.y / speedDivider, velocity.z / speedDivider);
        else if (blockHitResult.getSide() == Direction.UP || blockHitResult.getSide() == Direction.DOWN)
            setVelocity(velocity.x / speedDivider, -velocity.y / speedDivider, velocity.z / speedDivider);
        else
            setVelocity(velocity.x / speedDivider, velocity.y / speedDivider, -velocity.z / speedDivider);
    }

    @Override
    public void tick()
    {
        int ticksUntilDespawn = ServerSettings.CURRENT.secondsUntilDimmingOut.value
                + ServerSettings.CURRENT.andThenSecondsUntilFizzlingOut.value
                + ServerSettings.CURRENT.andThenSecondsUntilDespawn.value;

        if (++lifespan == 0 && getWorld().isClient)
            DRGFlaresUtil.playSoundFromEntityOnClient(this, DRGFlareRegistry.getInstance().FLARE_THROW_EVENT, SoundCategory.MASTER, PlayerSettings.INSTANCE.flareSoundVolume.value / 100.0f, 1);

        if (lifespan > ticksUntilDespawn * 20 || isInLava() || getY() <= DRGFlaresUtil.getVoidDamageLevel(getWorld()))
            kill();

        if (!getWorld().isClient || DRGFlareRegistry.getInstance().serverSyncMode == ServerSyncMode.CLIENT_ONLY)
            DRGFlareLimiter.reportFlare(this);
        else
            frame(0);

        int idleOpt = ServerSettings.CURRENT.secondsUntilIdlingFlareGetsOptimized.value * 20;
        if (getVelocity().lengthSquared() < 0.01 && bounceCount > 2)
            idleTicks++;
        else
            idleTicks = 0;
        if (idleOpt <= 0 || idleTicks < idleOpt)
            super.tick();

        if (isTouchingWater())
        {
            idleTicks = 0;
            bounceCount = 10;              //Stop the flare from spinning in water
            addVelocity(0, 0.04, 0);       //Make it float in water
        }

        if (idleTicks == 20)
            lightBlockPos = null;

        //If the block bellow the flare has changed or it's in water, then re-enable gravity
        boolean isInsideWaterBlock = getWorld().isWater(getBlockPos());
        if (hasNoGravity() && (isInsideWaterBlock || !getLandingBlockState().equals(lastHitBlockState)))
        {
            setNoGravity(false);
            idleTicks = 0;
        }

        //If the Flare has fizzled out, we skip the bottom part which is responsible for lighting things up
        //
        //Here's a trick - we don't need the light source from the flare to exist on the server side.
        //While it COULD be useful for temporal mob-proofing, the upsides,
        //  such as server performance, not firing observers and not interfering with the flow of liquids, are far more important.
        //However, there's an option to enable server-side light sources anyway
        if (isLit() && (getWorld().isClient || ServerSettings.CURRENT.serverSideLightSources.value))
            spawnLightSource(isInsideWaterBlock);
    }

    private void spawnLightSource(boolean isInWaterBlock)
    {
        try
        {
            //This bit is kinda ugly, but there's a set of edge cases when light sources get replaces with actual blocks
            //And a flare losing any place it can place a light source at
            if (lightBlockPos == null)
            {
                lightBlockPos = findFreeSpace(getWorld(), getBlockPos(), ServerSettings.CURRENT.lightSourceSearchDistance.value);
                if (lightBlockPos == null)
                    return;

                BlockEntity blockEntity = getWorld().getBlockEntity(lightBlockPos);
                if (blockEntity instanceof FlareLightBlockEntity)
                    ((FlareLightBlockEntity) blockEntity).refresh(isInWaterBlock ? 20 : 0);
                else if (lifespan < ServerSettings.CURRENT.secondsUntilDimmingOut.value * 20)
                    getWorld().setBlockState(lightBlockPos, FlareLightBlock.getFullBrightnessBlockState());
                else
                    getWorld().setBlockState(lightBlockPos, FlareLightBlock.getDimmedOutBlockState());
            }
            else if (checkDistance(lightBlockPos, getBlockPos(), ServerSettings.CURRENT.lightSourceRefreshDistance.value))
            {
                //Because a flare moves slightly in water, it used to create an edge case when a light source would rapidly flash on and off.
                //By having an old light source survive for longer than it takes to spawn a new one, we make sure the flicker doesn't happen
                BlockEntity blockEntity = getWorld().getBlockEntity(lightBlockPos);
                if (blockEntity instanceof FlareLightBlockEntity)
                {
                    int lightLevel = FlareLightBlock.getLightLevel(getWorld(), lightBlockPos);
                    if (lifespan < ServerSettings.CURRENT.secondsUntilDimmingOut.value * 20)
                    {
                        if (lightLevel != ServerSettings.CURRENT.fullBrightnessLightLevel.value)
                            getWorld().setBlockState(lightBlockPos, FlareLightBlock.getFullBrightnessBlockState());
                    }
                    else if (lightLevel != ServerSettings.CURRENT.dimmedLightLevel.value)
                        getWorld().setBlockState(lightBlockPos, FlareLightBlock.getDimmedOutBlockState());
                    ((FlareLightBlockEntity) blockEntity).refresh(isInWaterBlock ? 20 : 0);
                }
                else
                    lightBlockPos = null;
            }
            else
                lightBlockPos = null;
        }
        catch (Throwable e)
        {
            DRGFlares.LOGGER.error("Failed to process a light source block for " + this+" -> "+lightBlockPos, e);
        }
    }

    private boolean checkDistance(BlockPos blockPosA, BlockPos blockPosB, int distance)
    {
        return Math.abs(blockPosA.getX() - blockPosB.getX()) <= distance
                && Math.abs(blockPosA.getY() - blockPosB.getY()) <= distance
                && Math.abs(blockPosA.getZ() - blockPosB.getZ()) <= distance;
    }

    private BlockPos findFreeSpace(World world, BlockPos blockPos, int maxDistance)
    {
        if (blockPos == null)
            return null;

        //We want to find a valid position for a light source as close as possible to the flare
        //Which means we want to iterate from 0;0;0 offset and move outside, rather than x=-2..2; y=-2..2; z=-2..2
        int[] offsets = new int[maxDistance * 2 + 1];
        offsets[0] = 0;
        for (int i = 2; i <= maxDistance * 2; i += 2)
        {
            offsets[i - 1] = i / 2;
            offsets[i] = -i / 2;
        }
        for (int x : offsets)
            for (int y : offsets)
                for (int z : offsets)
                {
                    BlockPos offsetPos = blockPos.add(x, y, z);
                    BlockState state = world.getBlockState(offsetPos);
                    if (state.isAir() || state.getBlock().equals(DRGFlareRegistry.getInstance().getLightSourceBlockType()))
                        return offsetPos;
                }

        return null;
    }
}