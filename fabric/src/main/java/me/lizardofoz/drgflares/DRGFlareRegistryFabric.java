package me.lizardofoz.drgflares;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.Getter;
import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.block.FlareLightBlockEntity;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.item.FlareItem;
import me.lizardofoz.drgflares.packet.PacketStuff;
import me.lizardofoz.drgflares.packet.SpawnFlareEntityS2CPacket;
import me.lizardofoz.drgflares.util.FlareColor;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class DRGFlareRegistryFabric extends DRGFlareRegistry
{
    @Getter private EntityType<FlareEntity> flareEntityType;
    @Getter private Map<FlareColor, Item> flareItemTypes;
    @Getter private Block lightSourceBlockType;
    @Getter private BlockEntityType<FlareLightBlockEntity> lightSourceBlockEntityType;

    @Getter(lazy = true) private final boolean isClothConfigLoaded = FabricLoader.getInstance().isModLoaded("cloth-config2");
    @Getter(lazy = true) private final boolean isInventorioLoaded = FabricLoader.getInstance().isModLoaded("inventorio");

    public static void initialize()
    {
        DRGFlareRegistry.instance = new DRGFlareRegistryFabric();
    }

    private DRGFlareRegistryFabric()
    {
        registerFlareItems();
        registerFlareEntity();
        registerLightBlock();
        registerSounds();
    }

    private void registerFlareItems()
    {
        Map<FlareColor, Item> flares = new HashMap<>();

        for (FlareColor color : FlareColor.colors)
            flares.put(color, Registry.register(
                    Registry.ITEM,
                    new Identifier("drg_flares", "drg_flare_" + color.toString()),
                    new FlareItem(new FabricItemSettings().group(ItemGroup.MISC))));

        flareItemTypes = ImmutableMap.copyOf(flares);
    }

    private void registerLightBlock()
    {
        lightSourceBlockType = new FlareLightBlock(
                AbstractBlock.Settings.of(
                        new FabricMaterialBuilder(MaterialColor.CLEAR)
                                .replaceable()
                                .lightPassesThrough()
                                .notSolid()
                                .build())
                        .sounds(BlockSoundGroup.WOOD)
                        .strength(3600000.8F)
                        .dropsNothing()
                        .nonOpaque()
                        .luminance((state) -> state.get(FlareLightBlock.LIGHT_LEVEL)));
        lightSourceBlockEntityType = BlockEntityType.Builder.create(FlareLightBlockEntity::new, lightSourceBlockType).build(null);

        Registry.register(Registry.BLOCK, new Identifier("drg_flares", "flare_light_block"), lightSourceBlockType);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier("drg_flares", "flare_light_block_entity"), lightSourceBlockEntityType);
    }

    private void registerFlareEntity()
    {
        flareEntityType = Registry.register(
                Registry.ENTITY_TYPE,
                new Identifier("drg_flares", "drg_flare"),
                FabricEntityTypeBuilder.create(SpawnGroup.MISC, FlareEntity::make)
                        .dimensions(EntityDimensions.fixed(0.4f, 0.2f))
                        .trackRangeBlocks(64)
                        .spawnableFarFromPlayer()
                        .trackedUpdateRate(1)
                        .build());
    }

    private void registerSounds()
    {
        Registry.register(Registry.SOUND_EVENT, FLARE_THROW, FLARE_THROW_EVENT);
        Registry.register(Registry.SOUND_EVENT, FLARE_BOUNCE, FLARE_BOUNCE_EVENT);
        Registry.register(Registry.SOUND_EVENT, FLARE_BOUNCE_FAR, FLARE_BOUNCE_FAR_EVENT);
    }

    @Override
    public Packet<?> createSpawnFlareEntityPacket(FlareEntity flareEntity)
    {
        SpawnFlareEntityS2CPacket packet = new SpawnFlareEntityS2CPacket(flareEntity);
        PacketByteBuf buf = new PacketByteBuf(PooledByteBufAllocator.DEFAULT.buffer());
        packet.write(buf);
        return ServerPlayNetworking.createS2CPacket(SpawnFlareEntityS2CPacket.IDENTIFIER, buf);
    }

    @Override
    public void broadcastSettingsChange()
    {
        try
        {
            for (ServerPlayerEntity serverPlayerEntity : ((MinecraftServer) FabricLoader.getInstance().getGameInstance()).getPlayerManager().getPlayerList())
                PacketStuff.sendSettingsSyncS2CPacket(serverPlayerEntity);
        }
        catch (Throwable ignored) { }
        try
        {
            for (ServerPlayerEntity serverPlayerEntity : MinecraftClient.getInstance().getServer().getPlayerManager().getPlayerList())
                PacketStuff.sendSettingsSyncS2CPacket(serverPlayerEntity);
        }
        catch (Throwable ignored) { }
    }
}