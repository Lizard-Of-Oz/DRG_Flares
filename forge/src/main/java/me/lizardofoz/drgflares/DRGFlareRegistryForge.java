package me.lizardofoz.drgflares;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.Getter;
import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.block.FlareLightBlockEntity;
import me.lizardofoz.drgflares.client.FlareEntityRenderer;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.item.FlareItem;
import me.lizardofoz.drgflares.packet.PacketStuff;
import me.lizardofoz.drgflares.packet.SpawnFlareEntityS2CPacket;
import me.lizardofoz.drgflares.util.FlareColor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class DRGFlareRegistryForge extends DRGFlareRegistry
{
    private final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "drg_flares");
    private final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "drg_flares");
    private final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "drg_flares");
    private final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, "drg_flares");
    private final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, "drg_flares");

    @Getter private EntityType<FlareEntity> flareEntityType;
    @Getter private Map<FlareColor, Item> flareItemTypes;
    @Getter private Block lightSourceBlockType;
    @Getter private BlockEntityType<FlareLightBlockEntity> lightSourceBlockEntityType;

    @Getter(lazy = true) private final boolean isClothConfigLoaded = ModList.get().isLoaded("cloth-config"); //It's not "cloth-config2" unlike in Fabric - it's not a typo
    @Getter(lazy = true) private final boolean isInventorioLoaded = ModList.get().isLoaded("inventorio");

    public static void initialize()
    {
        DRGFlareRegistry.instance = new DRGFlareRegistryForge();
    }

    private DRGFlareRegistryForge()
    {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());

        registerFlareItems();
        registerFlareEntity();
        registerLightBlock();
        registerSounds();
    }

    private void registerFlareItems()
    {
        Map<FlareColor, Item> flares = new HashMap<>();

        for (FlareColor color : FlareColor.values())
        {
            Item flareItem = new FlareItem(new Item.Settings().group(ItemGroup.MISC));
            ITEMS.register("drg_flare_" + color.toString(), () -> flareItem);
            flares.put(color, flareItem);
        }

        flareItemTypes = ImmutableMap.copyOf(flares);
    }

    private void registerFlareEntity()
    {
        flareEntityType = EntityType.Builder.create(FlareEntity::make, SpawnGroup.MISC)
                .setDimensions(0.4f, 0.2f)
                .setTrackingRange(64)
                .spawnableFarFromPlayer()
                .trackingTickInterval(1)
                .build("drg_flares:drg_flare");
        ENTITIES.register("drg_flare", () -> flareEntityType);

        if (FMLEnvironment.dist == Dist.CLIENT)
            RenderingRegistry.registerEntityRenderingHandler(flareEntityType, FlareEntityRenderer::new);
    }

    private void registerLightBlock()
    {
        lightSourceBlockType = new FlareLightBlock(
                AbstractBlock.Settings.of(
                        new Material.Builder(MaterialColor.CLEAR)
                                .replaceable()
                                .notSolid()
                                .build())
                        .sounds(BlockSoundGroup.WOOD)
                        .strength(3600000.8F)
                        .dropsNothing()
                        .nonOpaque()
                        .luminance((state) -> state.get(FlareLightBlock.LIGHT_LEVEL)));
        lightSourceBlockEntityType = BlockEntityType.Builder.create(FlareLightBlockEntity::new, lightSourceBlockType).build(null);

        BLOCKS.register("flare_light_block", () -> lightSourceBlockType);
        TILE_ENTITIES.register("flare_light_block_entity", () -> lightSourceBlockEntityType);
    }

    private void registerSounds()
    {
        SOUNDS.register(FLARE_THROW.getPath(), () -> FLARE_THROW_EVENT);
        SOUNDS.register(FLARE_BOUNCE.getPath(), () -> FLARE_BOUNCE_EVENT);
        SOUNDS.register(FLARE_BOUNCE_FAR.getPath(), () -> FLARE_BOUNCE_FAR_EVENT);
    }

    @Override
    public Packet<?> createSpawnFlareEntityPacket(FlareEntity flareEntity)
    {
        SpawnFlareEntityS2CPacket packet = new SpawnFlareEntityS2CPacket(flareEntity);
        PacketByteBuf buf = new PacketByteBuf(PooledByteBufAllocator.DEFAULT.buffer());
        packet.write(buf);
        return PacketStuff.sendFlareSpawnS2CPacket(packet);
    }
}