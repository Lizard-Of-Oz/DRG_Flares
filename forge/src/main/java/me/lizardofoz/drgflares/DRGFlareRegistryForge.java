package me.lizardofoz.drgflares;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.Getter;
import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.block.FlareLightBlockEntity;
import me.lizardofoz.drgflares.client.FlareEntityRenderer;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.item.FlareDispenserBehavior;
import me.lizardofoz.drgflares.item.FlareItem;
import me.lizardofoz.drgflares.packet.PacketStuff;
import me.lizardofoz.drgflares.packet.SpawnFlareEntityS2CPacket;
import me.lizardofoz.drgflares.util.FlareColor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;
import java.util.HashMap;
import java.util.Map;

public class DRGFlareRegistryForge extends DRGFlareRegistry
{
    @Getter private EntityType<FlareEntity> flareEntityType;
    @Getter private Map<FlareColor, Item> flareItemTypes;
    @Getter private Block lightSourceBlockType;
    @Getter private BlockEntityType<FlareLightBlockEntity> lightSourceBlockEntityType;

    @Getter(lazy = true) private final boolean isClothConfigLoaded = ModList.get().isLoaded("cloth_config"); //Come on, why did it have to change the mod id?
    @Getter(lazy = true) private final boolean isInventorioLoaded = ModList.get().isLoaded("inventorio");

    public static void initialize()
    {
        DRGFlareRegistry.instance = new DRGFlareRegistryForge();
        FMLJavaModLoadingContext.get().getModEventBus().register(DRGFlareRegistry.instance);
    }

    private DRGFlareRegistryForge()
    {
    }

    @SubscribeEvent
    public void registerFlareItems(RegistryEvent.Register<Item> event)
    {
        Map<FlareColor, Item> flares = new HashMap<>();

        for (FlareColor color : FlareColor.values())
        {
            Item flareItem = new FlareItem(new Item.Settings().group(color == FlareColor.RANDOM || color == FlareColor.RANDOM_BRIGHT_ONLY ? null : ItemGroup.MISC));
            flareItem.setRegistryName("drg_flare_" + color.toString());
            event.getRegistry().register(flareItem);
            flares.put(color, flareItem);
        }

        flareItemTypes = ImmutableMap.copyOf(flares);
        FlareDispenserBehavior.initialize();
    }

    @SubscribeEvent
    public void registerFlareEntity(RegistryEvent.Register<EntityType<?>> event)
    {
        flareEntityType = EntityType.Builder.create(FlareEntity::make, SpawnGroup.MISC)
                .setDimensions(0.4f, 0.2f)
                .setTrackingRange(64)
                .spawnableFarFromPlayer()
                .trackingTickInterval(1)
                .build("drg_flares:drg_flare");
        flareEntityType.setRegistryName("drg_flare");
        event.getRegistry().register(flareEntityType);
    }

    @SubscribeEvent
    public void registerLightBlock(RegistryEvent.Register<Block> event)
    {
        lightSourceBlockType = new FlareLightBlock(
                AbstractBlock.Settings.of(
                        new Material.Builder(MapColor.CLEAR)
                                .replaceable()
                                .notSolid()
                                .build())
                        .sounds(BlockSoundGroup.WOOD)
                        .strength(3600000.8F)
                        .dropsNothing()
                        .nonOpaque()
                        .luminance((state) -> state.get(FlareLightBlock.LIGHT_LEVEL)))
                        .setRegistryName("flare_light_block");
        event.getRegistry().register(lightSourceBlockType);
    }

    @SubscribeEvent
    public void registerLightBlockEntity(RegistryEvent.Register<BlockEntityType<?>> event)
    {
        lightSourceBlockEntityType = BlockEntityType.Builder.create(FlareLightBlockEntity::new, lightSourceBlockType).build(null);
        lightSourceBlockEntityType.setRegistryName("flare_light_block_entity");
        event.getRegistry().register(lightSourceBlockEntityType);
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event)
    {
        FLARE_THROW_EVENT.setRegistryName(FLARE_THROW.getPath());
        FLARE_BOUNCE_EVENT.setRegistryName(FLARE_BOUNCE.getPath());
        FLARE_BOUNCE_FAR_EVENT.setRegistryName(FLARE_BOUNCE_FAR.getPath());
        event.getRegistry().register(FLARE_THROW_EVENT);
        event.getRegistry().register(FLARE_BOUNCE_EVENT);
        event.getRegistry().register(FLARE_BOUNCE_FAR_EVENT);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(DRGFlareRegistryForge.instance.getFlareEntityType(), FlareEntityRenderer::new);
    }

    @Override
    public Packet<?> createSpawnFlareEntityPacket(FlareEntity flareEntity)
    {
        SpawnFlareEntityS2CPacket packet = new SpawnFlareEntityS2CPacket(flareEntity);
        PacketByteBuf buf = new PacketByteBuf(PooledByteBufAllocator.DEFAULT.buffer());
        packet.write(buf);
        return PacketStuff.sendFlareSpawnS2CPacket(packet);
    }

    @Override
    public void broadcastSettingsChange()
    {
        try
        {
            for (ServerPlayerEntity serverPlayerEntity : ServerLifecycleHooks.getCurrentServer().getPlayerManager().getPlayerList())
                PacketStuff.sendSettingsSyncS2CPacket(serverPlayerEntity);
        }
        catch (Throwable ignored) { }
    }
}