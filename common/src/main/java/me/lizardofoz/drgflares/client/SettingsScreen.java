package me.lizardofoz.drgflares.client;

import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.config.PlayerSettings;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.config.SettingsEntry;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import me.lizardofoz.drgflares.util.FlareColor;
import me.lizardofoz.drgflares.util.ServerSyncMode;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.FloatListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class SettingsScreen
{
    private SettingsScreen()
    {
    }

    public static Screen create(Screen parent)
    {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setSavingRunnable(SettingsScreen::onSave)
                .setTitle(Text.translatable("drg_flares.settings.title"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(Text.translatable("drg_flares.settings.title"));

        //"About DRG Flares" Section
        SubCategoryBuilder aboutSection = entryBuilder.startSubCategory(Text.translatable("drg_flares.settings.about.title"));
        aboutSection.add(entryBuilder.startTextDescription(Text.translatable("drg_flares.settings.about.text",
                ServerSettings.CURRENT.secondsUntilDimmingOut.value, ServerSettings.CURRENT.andThenSecondsUntilFizzlingOut.value, ServerSettings.CURRENT.andThenSecondsUntilDespawn.value
        )).build());
        category.addEntry(aboutSection.build());

        //Client Settings
        category.addEntry(entryBuilder
                .startIntSlider(
                        Text.translatable(PlayerSettings.INSTANCE.flareColor.displayText),
                        PlayerSettings.INSTANCE.flareColor.value.id,
                        -2, 15
                )
                .setDefaultValue(PlayerSettings.INSTANCE.flareColor.defaultValue.id)
                .setSaveConsumer(it -> PlayerSettings.INSTANCE.flareColor.value = FlareColor.byId(it))
                .setTextGetter(it -> Text.translatable(PlayerSettings.INSTANCE.flareColor.displayText + "." + it))
                .setTooltip(Text.translatable(PlayerSettings.INSTANCE.flareColor.displayToolTip)).build());
        addFloatEntry(category, entryBuilder, PlayerSettings.INSTANCE.flareUISlotX, true);
        addFloatEntry(category, entryBuilder, PlayerSettings.INSTANCE.flareUISlotY, true);
        addIntegerEntry(category, entryBuilder, PlayerSettings.INSTANCE.flareSoundVolume, true);
        addBoolEntry(category, entryBuilder, PlayerSettings.INSTANCE.flareButtonHint, true);

        boolean editable = !DRGFlaresUtil.isOnRemoteServer();
        if (DRGFlareRegistry.getInstance().serverSyncMode == ServerSyncMode.CLIENT_ONLY)
        {
            editable = true;
            category.addEntry(entryBuilder.startTextDescription(Text.translatable("drg_flares.settings.server_desc_client_only")).build());
        }
        else if (editable)
            category.addEntry(entryBuilder.startTextDescription(Text.translatable("drg_flares.settings.server_desc")).build());
        else
            category.addEntry(entryBuilder.startTextDescription(Text.translatable("drg_flares.settings.disabled_by_server")).build());

        //Server Settings
        ServerSettings serverSettings = editable ? ServerSettings.LOCAL : ServerSettings.CURRENT;
        addBoolEntry(category, entryBuilder, serverSettings.regeneratingFlaresEnabled, editable);
        addIntegerEntry(category, entryBuilder, serverSettings.regeneratingFlaresRechargeTime, editable);
        addIntegerEntry(category, entryBuilder, serverSettings.regeneratingFlaresMaxCharges, editable);
        addIntegerEntry(category, entryBuilder, serverSettings.flareEntityLimitPerPlayer, editable);
        addBoolEntry(category, entryBuilder, serverSettings.flareRecipesInSurvival, editable);
        category.addEntry(entryBuilder.startTextDescription(Text.literal(" ")).build());

        addIntegerEntry(category, entryBuilder, serverSettings.secondsUntilDimmingOut, editable);
        addIntegerEntry(category, entryBuilder, serverSettings.andThenSecondsUntilFizzlingOut, editable);
        addIntegerEntry(category, entryBuilder, serverSettings.andThenSecondsUntilDespawn, editable);
        category.addEntry(entryBuilder.startTextDescription(Text.literal(" ")).build());

        addIntegerEntry(category, entryBuilder, serverSettings.fullBrightnessLightLevel, editable);
        addIntegerEntry(category, entryBuilder, serverSettings.dimmedLightLevel, editable);
        category.addEntry(entryBuilder.startTextDescription(Text.literal(" ")).build());

        addIntegerEntry(category, entryBuilder, serverSettings.secondsUntilIdlingFlareGetsOptimized, editable);
        addIntegerEntry(category, entryBuilder, serverSettings.lightSourceLifespanTicks, editable);
        addIntegerEntry(category, entryBuilder, serverSettings.lightSourceRefreshDistance, editable);
        addIntegerEntry(category, entryBuilder, serverSettings.lightSourceSearchDistance, editable);
        addBoolEntry(category, entryBuilder, serverSettings.creativeUnlimitedRegeneratingFlares, editable);
        addBoolEntry(category, entryBuilder, serverSettings.serverSideLightSources, editable);
        category.addEntry(entryBuilder.startTextDescription(Text.literal(" ")).build());

        addFloatEntry(category, entryBuilder, serverSettings.flareGravity, editable);
        addFloatEntry(category, entryBuilder, serverSettings.flareThrowSpeed, editable);
        addFloatEntry(category, entryBuilder, serverSettings.flareThrowAngle, editable);
        addFloatEntry(category, entryBuilder, serverSettings.flareSpeedBounceDivider, editable);
        category.addEntry(entryBuilder.startTextDescription(Text.literal(" ")).build());

        try
        {
            category.addEntry(entryBuilder.startTextDescription(Text.translatable("controls.title")).build());
            category.addEntry(entryBuilder.fillKeybindingField(Text.translatable("drg_flares.keys.throw_flare"), PlayerSettings.INSTANCE.throwFlareKey).build());
            category.addEntry(entryBuilder.fillKeybindingField(Text.translatable("drg_flares.keys.flare_mod_settings"), PlayerSettings.INSTANCE.flareModSettingsKey).build());
        }
        catch (Throwable e)
        {
            category.addEntry(entryBuilder.startTextDescription(Text.translatable("drg_flares.settings.keybind_error")).build());
        }

        return builder.build();
    }

    private static void addIntegerEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, SettingsEntry.Integer settingsEntry, boolean editable)
    {
        AbstractConfigListEntry<?> entry;
        if (settingsEntry.max == Integer.MAX_VALUE)
        {
            entry = entryBuilder
                    .startIntField(
                            Text.translatable(settingsEntry.displayText),
                            settingsEntry.value
                    )
                    .setMin(settingsEntry.min)
                    .setDefaultValue(settingsEntry.defaultValue)
                    .setSaveConsumer(it -> settingsEntry.value = it)
                    .setTooltip(Text.translatable(settingsEntry.displayText + ".desc"))
                    .build();
        }
        else
        {
            entry = entryBuilder
                    .startIntSlider(
                            Text.translatable(settingsEntry.displayText),
                            settingsEntry.value,
                            settingsEntry.min,
                            settingsEntry.max
                    )
                    .setDefaultValue(settingsEntry.defaultValue)
                    .setSaveConsumer(it -> settingsEntry.value = it)
                    .setTooltip(Text.translatable(settingsEntry.displayText + ".desc"))
                    .build();
        }
        entry.setEditable(editable);
        category.addEntry(entry);
    }

    private static void addFloatEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, SettingsEntry.Float settingsEntry, boolean editable)
    {
        FloatListEntry entry = entryBuilder
                .startFloatField(
                        Text.translatable(settingsEntry.displayText),
                        settingsEntry.value
                )
                .setMin(settingsEntry.min)
                .setMax(settingsEntry.max)
                .setDefaultValue(settingsEntry.defaultValue)
                .setSaveConsumer(it -> settingsEntry.value = it)
                .setTooltip(Text.translatable(settingsEntry.displayToolTip))
                .build();
        entry.setEditable(editable);
        category.addEntry(entry);
    }

    private static void addBoolEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, SettingsEntry.Boolean settingsEntry, boolean editable)
    {
        BooleanListEntry entry = entryBuilder
                .startBooleanToggle(
                        Text.translatable(settingsEntry.displayText),
                        settingsEntry.value
                )
                .setDefaultValue(settingsEntry.defaultValue)
                .setSaveConsumer(it -> settingsEntry.value = it)
                .setTooltip(Text.translatable(settingsEntry.displayToolTip))
                .build();
        entry.setEditable(editable);
        category.addEntry(entry);
    }

    private static void onSave()
    {
        PlayerSettings.INSTANCE.save();

        if (!DRGFlaresUtil.isOnRemoteServer() || DRGFlareRegistry.getInstance().serverSyncMode != ServerSyncMode.SYNC_WITH_SERVER)
        {
            ServerSettings.LOCAL.save();
            ServerSettings.CURRENT.loadFromJson(ServerSettings.LOCAL.asJson());
            FlareLightBlock.refreshBlockStates();
            DRGFlareRegistry.getInstance().broadcastSettingsChange();
        }
    }
}