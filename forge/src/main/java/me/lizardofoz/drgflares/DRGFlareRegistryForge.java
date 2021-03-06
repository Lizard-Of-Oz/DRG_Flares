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
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
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
    public void forgePleaseStopChangingYourAPI(RegisterEvent event)
    {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            Map<FlareColor, Item> flares = new HashMap<>();

            for (FlareColor color : FlareColor.values())
            {
                ItemGroup creativeTab = color == FlareColor.RANDOM || color == FlareColor.RANDOM_BRIGHT_ONLY ? null : ItemGroup.MISC;
                Item flareItem = new FlareItem(new Item.Settings().group(creativeTab));
                helper.register(new Identifier("drg_flares", "drg_flare_" + color.toString()), flareItem);
                flares.put(color, flareItem);
            }

            flareItemTypes = ImmutableMap.copyOf(flares);
            FlareDispenserBehavior.initialize();
        });

        event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
            flareEntityType = EntityType.Builder.create(FlareEntity::make, SpawnGroup.MISC)
                    .setDimensions(0.4f, 0.2f)
                    .setTrackingRange(64)
                    .spawnableFarFromPlayer()
                    .trackingTickInterval(1)
                    .build("drg_flares:drg_flare");
            helper.register(new Identifier("drg_flares", "drg_flare"), flareEntityType);
        });

        event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
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
                            .luminance((state) -> state.get(FlareLightBlock.LIGHT_LEVEL)));
            helper.register(new Identifier("drg_flares", "flare_light_block"), lightSourceBlockType);
        });

        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
            lightSourceBlockEntityType = BlockEntityType.Builder.create(FlareLightBlockEntity::new, lightSourceBlockType).build(null);
            helper.register(new Identifier("drg_flares", "flare_light_block_entity"), lightSourceBlockEntityType);
        });

        event.register(ForgeRegistries.Keys.SOUND_EVENTS, helper -> {
            helper.register(FLARE_THROW, FLARE_THROW_EVENT);
            helper.register(FLARE_BOUNCE, FLARE_BOUNCE_EVENT);
            helper.register(FLARE_BOUNCE_FAR, FLARE_BOUNCE_FAR_EVENT);
        });
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