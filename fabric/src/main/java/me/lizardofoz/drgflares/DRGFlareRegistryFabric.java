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
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

public class DRGFlareRegistryFabric extends DRGFlareRegistry
{
    @Getter private EntityType<FlareEntity> flareEntityType;
    @Getter private Map<FlareColor, Item> flareItemTypes;
    @Getter private Block lightSourceBlockType;
    @Getter private BlockEntityType<FlareLightBlockEntity> lightSourceBlockEntityType;
    @Getter private ItemGroup creativeItemGroup;

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
        creativeItemGroup = FabricItemGroup
                .builder(new Identifier("drg_flares", "drg_flares"))
                .icon(() -> new ItemStack(flareItemTypes.get(FlareColor.MAGENTA)))
                .displayName(Text.translatable("itemGroup.drg_flares"))
                .build();

        Map<FlareColor, Item> flares = new HashMap<>();

        for (FlareColor color : FlareColor.colors)
        {
            Item flareItem = Registry.register(
                    Registries.ITEM,
                    new Identifier("drg_flares", "drg_flare_" + color.toString()),
                    new FlareItem(new FabricItemSettings()));
            flares.put(color, flareItem);
            if (color != FlareColor.RANDOM && color != FlareColor.RANDOM_BRIGHT_ONLY)
                ItemGroupEvents.modifyEntriesEvent(creativeItemGroup).register(content -> content.add(flareItem));
        }

        flareItemTypes = ImmutableMap.copyOf(flares);
    }

    private void registerLightBlock()
    {
        lightSourceBlockType = new FlareLightBlock(
                AbstractBlock.Settings.of(
                        new FabricMaterialBuilder(MapColor.CLEAR)
                                .replaceable()
                                .lightPassesThrough()
                                .notSolid()
                                .build())
                        .sounds(BlockSoundGroup.WOOD)
                        .strength(3600000.8F)
                        .dropsNothing()
                        .nonOpaque()
                        .luminance((state) -> state.get(FlareLightBlock.LIGHT_LEVEL)));
        lightSourceBlockEntityType = FabricBlockEntityTypeBuilder.create(FlareLightBlockEntity::new, lightSourceBlockType).build(null);

        Registry.register(Registries.BLOCK, new Identifier("drg_flares", "flare_light_block"), lightSourceBlockType);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("drg_flares", "flare_light_block_entity"), lightSourceBlockEntityType);
    }

    private void registerFlareEntity()
    {
        flareEntityType = Registry.register(
                Registries.ENTITY_TYPE,
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
        Registry.register(Registries.SOUND_EVENT, FLARE_THROW, FLARE_THROW_EVENT);
        Registry.register(Registries.SOUND_EVENT, FLARE_BOUNCE, FLARE_BOUNCE_EVENT);
        Registry.register(Registries.SOUND_EVENT, FLARE_BOUNCE_FAR, FLARE_BOUNCE_FAR_EVENT);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnFlareEntityPacket(FlareEntity flareEntity)
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