package me.lizardofoz.drgflares.packet;

import me.lizardofoz.drgflares.DRGFlares;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.util.FlareColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class SpawnFlareEntityS2CPacket
{
    public static final Identifier IDENTIFIER = new Identifier("drg_flares", "spawn_flare");

    private int id;
    private UUID uuid;
    private double x;
    private double y;
    private double z;
    private int velocityX;
    private int velocityY;
    private int velocityZ;
    private int lifespan;
    private FlareColor color;
    private int bounceCount;

    public SpawnFlareEntityS2CPacket()
    {
    }

    public SpawnFlareEntityS2CPacket(int id, UUID uuid, double x, double y, double z, Vec3d velocity, int lifespan, FlareColor color, int bounceCount)
    {
        this.id = id;
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.velocityX = (int) (MathHelper.clamp(velocity.x, -3.9, 3.9) * 8000);
        this.velocityY = (int) (MathHelper.clamp(velocity.y, -3.9, 3.9) * 8000);
        this.velocityZ = (int) (MathHelper.clamp(velocity.z, -3.9, 3.9) * 8000);
        this.lifespan = lifespan;
        this.color = color;
        this.bounceCount = bounceCount;
    }

    public SpawnFlareEntityS2CPacket(FlareEntity entity)
    {
        this(entity.getEntityId(), entity.getUuid(), entity.getX(), entity.getY(), entity.getZ(), entity.getVelocity(), entity.lifespan, entity.color, entity.bounceCount);
    }

    /** Please make sure you're calling it from the main thread */
    @Environment(EnvType.CLIENT)
    public void spawnOnClient()
    {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null)
            return;
        FlareEntity entity = new FlareEntity(world, color);

        entity.updateTrackedPosition(x, y, z);
        entity.refreshPositionAfterTeleport(x, y, z);
        entity.setEntityId(id);
        entity.setUuid(uuid);
        entity.lifespan = lifespan;
        entity.color = color;
        entity.bounceCount = bounceCount;
        world.addEntity(id, entity);
    }

    public void read(PacketByteBuf buf)
    {
        try
        {
            this.id = buf.readVarInt();
            this.uuid = buf.readUuid();
            this.x = buf.readDouble();
            this.y = buf.readDouble();
            this.z = buf.readDouble();
            this.velocityX = buf.readShort();
            this.velocityY = buf.readShort();
            this.velocityZ = buf.readShort();
            this.lifespan = buf.readShort();
            this.color = FlareColor.byId(buf.readByte());
            this.bounceCount = buf.readByte();
        }
        catch (Exception e)
        {
            DRGFlares.LOGGER.error("Failed to read SpawnFlareEntityS2CPacket", e);
        }
    }

    public void write(PacketByteBuf buf)
    {
        try
        {
            buf.writeVarInt(this.id);
            buf.writeUuid(this.uuid);
            buf.writeDouble(this.x);
            buf.writeDouble(this.y);
            buf.writeDouble(this.z);
            buf.writeShort(this.velocityX);
            buf.writeShort(this.velocityY);
            buf.writeShort(this.velocityZ);
            buf.writeShort(this.lifespan);
            buf.writeByte(this.color.id);
            buf.writeByte(this.bounceCount);
        }
        catch (Exception e)
        {
            DRGFlares.LOGGER.error("Failed to write SpawnFlareEntityS2CPacket", e);
        }
    }
}