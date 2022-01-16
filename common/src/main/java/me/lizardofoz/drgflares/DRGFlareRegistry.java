package me.lizardofoz.drgflares;

import lombok.Getter;
import me.lizardofoz.drgflares.block.FlareLightBlockEntity;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.util.FlareColor;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.Map;

public abstract class DRGFlareRegistry
{
    @Getter protected static DRGFlareRegistry instance;

    protected final Identifier FLARE_THROW = new Identifier("drg_flares:flare_throw");
    protected final Identifier FLARE_BOUNCE = new Identifier("drg_flares:flare_bounce");
    protected final Identifier FLARE_BOUNCE_FAR = new Identifier("drg_flares:flare_bounce_far");

    public final SoundEvent FLARE_THROW_EVENT = new SoundEvent(FLARE_THROW);
    public final SoundEvent FLARE_BOUNCE_EVENT = new SoundEvent(FLARE_BOUNCE);
    public final SoundEvent FLARE_BOUNCE_FAR_EVENT = new SoundEvent(FLARE_BOUNCE_FAR);

    public abstract EntityType<FlareEntity> getFlareEntityType();
    public abstract Map<FlareColor, Item> getFlareItemTypes();
    public abstract Block getLightSourceBlockType();
    public abstract BlockEntityType<FlareLightBlockEntity> getLightSourceBlockEntityType();
    public abstract Packet<?> createSpawnFlareEntityPacket(FlareEntity flareEntity);
    public abstract boolean isClothConfigLoaded();
    public abstract boolean isInventorioLoaded();
}
