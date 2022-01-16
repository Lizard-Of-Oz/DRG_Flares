package me.lizardofoz.drgflares;

import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.client.FlareHUDRenderer;
import me.lizardofoz.drgflares.client.SettingsScreen;
import me.lizardofoz.drgflares.config.PlayerSettings;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.packet.PacketStuff;
import me.lizardofoz.drgflares.util.DRGFlareLimiter;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import me.lizardofoz.drgflares.util.FlareColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

import java.util.Collection;

public class ForgeEvents
{
    protected ForgeEvents()
    {
    }

    @SubscribeEvent
    public void onEvent(FMLServerStartedEvent event)
    {
        FlareLightBlock.refreshBlockStates();
        ServerSettings.CURRENT.loadFromJson(ServerSettings.LOCAL.asJson());
        if (!ServerSettings.CURRENT.flareRecipesInSurvival.value)
        {
            Collection<Recipe<?>> values = event.getServer().getRecipeManager().values();
            values.removeIf(it -> it.getId().getNamespace().equals("drg_flares"));
            DRGFlaresUtil.setRecipes(event.getServer().getRecipeManager(), values);
        }
    }

    @SubscribeEvent
    public void onEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        DRGFlarePlayerAspect.add(player);
        DRGFlaresUtil.unlockFlareRecipes(player);
        PacketStuff.sendSettingsSyncPacket(player);
    }

    @SubscribeEvent
    public void onEvent(PlayerEvent.PlayerLoggedOutEvent event)
    {
        DRGFlarePlayerAspect.remove(event.getPlayer());
    }

    @SubscribeEvent
    public void onEvent(TickEvent.ServerTickEvent event)
    {
        DRGFlareLimiter.tick();
        DRGFlarePlayerAspect.tickAll();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onEvent(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR)
            FlareHUDRenderer.render(event.getMatrixStack(), event.getPartialTicks());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onEvent(TickEvent.ClientTickEvent event)
    {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null)
            return;

        DRGFlarePlayerAspect.clientLocal.tick();
        if (DRGFlareRegistryForge.getInstance().isClothConfigLoaded() && PlayerSettings.INSTANCE.flareModSettingsKey.wasPressed())
            MinecraftClient.getInstance().openScreen(SettingsScreen.create(MinecraftClient.getInstance().currentScreen));

        if (PlayerSettings.INSTANCE.throwFlareKey.wasPressed() && !DRGFlaresUtil.isRegenFlareOnCooldown(player))
        {
            if (DRGFlarePlayerAspect.clientLocal.checkFlareToss(player))
            {
                PacketStuff.sendFlareThrowPacket(FlareColor.RandomColorPicker.unwrapRandom(PlayerSettings.INSTANCE.flareColor.value, true));
                DRGFlarePlayerAspect.clientLocal.reduceFlareCount(player);
            }
            else if (ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
                player.playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.1f, 1.7f);
        }
    }
}