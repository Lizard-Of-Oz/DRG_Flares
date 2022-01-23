package me.lizardofoz.drgflares;

import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.client.SettingsScreen;
import me.lizardofoz.drgflares.config.PlayerSettings;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.Collection;

public abstract class CommonEvents
{
    protected abstract void sendSettingsSyncS2CPacket(ServerPlayerEntity player);

    protected void onServerStart(MinecraftServer server)
    {
        DRGFlareRegistry.getInstance().serverSyncMode = ServerSyncMode.SYNC_WITH_SERVER;
        DRGFlareLimiter.initOrReset();
        DRGFlarePlayerAspect.initOrReset();
        ServerSettings.CURRENT.loadFromJson(ServerSettings.LOCAL.asJson());
        FlareLightBlock.refreshBlockStates();
        if (!ServerSettings.CURRENT.flareRecipesInSurvival.value)
        {
            Collection<Recipe<?>> values = server.getRecipeManager().values();
            values.removeIf(it -> it.getId().getNamespace().equals("drg_flares"));
            DRGFlaresUtil.setRecipes(server.getRecipeManager(), values);
        }
    }

    protected void onServerTick()
    {
        DRGFlareLimiter.tick();
        DRGFlarePlayerAspect.tickAll();
    }

    protected void onPlayerJoinServer(ServerPlayerEntity player)
    {
        DRGFlareLimiter.onPlayerJoin(player);
        DRGFlarePlayerAspect.onPlayerJoin(player);
        DRGFlaresUtil.unlockFlareRecipes(player);
        sendSettingsSyncS2CPacket(player);
    }

    protected void onPlayerLeaveServer(PlayerEntity player)
    {
        DRGFlareLimiter.onPlayerLeave(player);
        DRGFlarePlayerAspect.onPlayerLeave(player);
    }

    @Environment(EnvType.CLIENT)
    protected abstract static class Client
    {
        protected abstract void sendFlareThrowC2SPacket(FlareColor color);

        protected void onClientTick(MinecraftClient client)
        {
            ClientPlayerEntity player = client.player;
            if (player == null || client.isPaused())
                return;
            DRGFlareLimiter.tick();
            DRGFlarePlayerAspect.clientLocal.tick();

            if (DRGFlareRegistry.getInstance().isClothConfigLoaded() && PlayerSettings.INSTANCE.flareModSettingsKey.wasPressed())
                client.openScreen(SettingsScreen.create(client.currentScreen));

            if (PlayerSettings.INSTANCE.throwFlareKey.wasPressed() && !DRGFlaresUtil.isRegenFlareOnCooldown(player))
            {
                if (DRGFlarePlayerAspect.clientLocal.checkFlareToss(player))
                {
                    FlareColor flareColor = FlareColor.RandomColorPicker.unwrapRandom(PlayerSettings.INSTANCE.flareColor.value, true);
                    if (DRGFlareRegistry.getInstance().serverSyncMode != ServerSyncMode.SYNC_WITH_SERVER && ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
                        DRGFlarePlayerAspect.clientLocal.tryThrowRegeneratingFlare(MinecraftClient.getInstance().player, flareColor);
                    else
                        sendFlareThrowC2SPacket(flareColor);
                }
                else if (ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
                    player.playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, PlayerSettings.INSTANCE.flareSoundVolume.value / 1234f, 1.7f);
            }
        }

        protected void onClientConnect()
        {
            if (DRGFlareRegistry.getInstance().serverSyncMode == ServerSyncMode.UNDEFINED)
            {
                DRGFlareRegistry.getInstance().serverSyncMode = ServerSyncMode.CLIENT_ONLY;
                FlareLightBlock.refreshBlockStates();
                ServerSettings.CURRENT.loadFromJson(ServerSettings.LOCAL.asJson());
            }
        }

        protected void onClientDisconnect()
        {
            DRGFlareRegistry.getInstance().serverSyncMode = ServerSyncMode.UNDEFINED;
        }
    }
}