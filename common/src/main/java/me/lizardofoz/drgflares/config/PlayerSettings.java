package me.lizardofoz.drgflares.config;

import com.google.common.collect.ImmutableList;
import me.lizardofoz.drgflares.util.FlareColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import java.io.File;
import java.util.List;

@Environment(EnvType.CLIENT)
public class PlayerSettings extends AbstractSettings
{
    public static final PlayerSettings INSTANCE = new PlayerSettings(new File(".", "config/drg_flares_client.json"));

    public final KeyBinding throwFlareKey = new KeyBinding(
            "drg_flares.keys.throw_flare",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "drg_flares.keys.category"
    );
    public final KeyBinding flareModSettingsKey = new KeyBinding(
            "drg_flares.keys.flare_mod_settings",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_BRACKET,
            "drg_flares.keys.category"
    );

    public final SettingsEntry.FlareColor flareColor =
            new SettingsEntry.FlareColor(FlareColor.RANDOM_BRIGHT_ONLY, "flare_color");
    public final SettingsEntry.Float flareUISlotX =
            new SettingsEntry.Float(0.8f, "flare_ui_x", -1, 2);
    public final SettingsEntry.Float flareUISlotY =
            new SettingsEntry.Float(1.0f, "flare_ui_y", -1, 2);
    public final SettingsEntry.Boolean flareButtonHint =
            new SettingsEntry.Boolean(true, "flare_button_hint");
    public final SettingsEntry.Integer flareSoundVolume =
            new SettingsEntry.Integer(100, "flare_sound_volume", 0, 200);

    private final List<SettingsEntry<?>> entries = ImmutableList.of(
            flareColor,
            flareUISlotX,
            flareUISlotY,
            flareButtonHint,
            flareSoundVolume);

    public PlayerSettings(File file)
    {
        super();
        loadFromFile(file);
    }

    @Override
    protected List<SettingsEntry<?>> getEntries()
    {
        return entries;
    }
}