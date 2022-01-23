package me.lizardofoz.drgflares.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.lizardofoz.drgflares.DRGFlares;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

public abstract class AbstractSettings
{
    private File configFile = null;

    protected AbstractSettings()
    {
    }

    public void loadFromFile(File file)
    {
        this.configFile = file;
        if (file.exists())
        {
            try (FileReader reader = new FileReader(file))
            {
                loadFromJson(new Gson().fromJson(reader, JsonObject.class));
                return;
            }
            catch (Throwable e)
            {
                DRGFlares.LOGGER.error("Failed to read the config file: " + file, e);
            }
        }
        save();
    }

    protected abstract List<SettingsEntry<?>> getEntries();

    public JsonObject asJson()
    {
        JsonObject result = new JsonObject();
        for (SettingsEntry<?> entry : getEntries())
            result.add(entry.configKey, entry.getValueAsJsonElement().get());
        return result;
    }

    public void loadFromJson(JsonObject jsonObject)
    {
        for (SettingsEntry<?> entry : getEntries())
            entry.tryElementAsValue(jsonObject.get(entry.configKey));
    }

    public void save()
    {
        if (configFile == null)
            return;
        try { configFile.getParentFile().mkdirs(); } catch (Throwable ignored) { }
        try (FileWriter writer = new FileWriter(configFile))
        {
            new GsonBuilder().setPrettyPrinting().create().toJson(asJson(), writer);
        }
        catch (Throwable e)
        {
            DRGFlares.LOGGER.error("Failed to write the config file: " + configFile, e);
        }
    }
}