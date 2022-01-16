package me.lizardofoz.drgflares.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class SettingsEntry<T>
{
    public final T defaultValue;
    public final String configKey;
    public final String displayText;
    public final String displayToolTip;
    @Getter protected Supplier<JsonElement> valueAsJsonElement;
    protected Function<JsonElement, T> jsonElementAsValue;
    public T value;

    public SettingsEntry(T defaultValue, String configKey)
    {
        this.defaultValue = defaultValue;
        this.configKey = configKey;
        this.value = defaultValue;
        this.displayText = "drg_flares.settings." + configKey;
        this.displayToolTip = displayText + ".desc";
    }

    public void tryElementAsValue(JsonElement element)
    {
        try
        {
            value = putValueInRange(jsonElementAsValue.apply(element));
        }
        catch (Throwable e)
        {
            value = defaultValue;
        }
    }
    public abstract T putValueInRange(T value);

    @Override
    public boolean equals(Object other)
    {
        return other instanceof SettingsEntry && configKey.equals(((SettingsEntry<?>) other).configKey);
    }

    @Override
    public int hashCode()
    {
        return configKey.hashCode();
    }

    public static class Boolean extends SettingsEntry<java.lang.Boolean>
    {
        public Boolean(java.lang.Boolean defaultValue, String configKey)
        {
            super(defaultValue, configKey);
            valueAsJsonElement = () -> new JsonPrimitive(value);
            jsonElementAsValue = jsonElement -> jsonElement == null ? defaultValue : jsonElement.getAsBoolean();
        }

        @Override
        public java.lang.Boolean putValueInRange(java.lang.Boolean value)
        {
            return value;
        }
    }

    public static class Integer extends SettingsEntry<java.lang.Integer>
    {
        public final int min;
        public final int max;
        public Integer(java.lang.Integer defaultValue, String configKey, int min, int max)
        {
            super(defaultValue, configKey);
            valueAsJsonElement = () -> new JsonPrimitive(value);
            jsonElementAsValue = jsonElement -> jsonElement == null ? defaultValue : jsonElement.getAsInt();
            this.min = min;
            this.max = max;
        }

        @Override
        public java.lang.Integer putValueInRange(java.lang.Integer value)
        {
            return Math.max(min, Math.min(max, value));
        }
    }

    public static class Float extends SettingsEntry<java.lang.Float>
    {
        public final float min;
        public final float max;
        public Float(java.lang.Float defaultValue, String configKey, float min, float max)
        {
            super(defaultValue, configKey);
            valueAsJsonElement = () -> new JsonPrimitive(value);
            jsonElementAsValue = jsonElement -> jsonElement == null ? defaultValue : jsonElement.getAsFloat();
            this.min = min;
            this.max = max;
        }

        @Override
        public java.lang.Float putValueInRange(java.lang.Float value)
        {
            return Math.max(min, Math.min(max, value));
        }
    }

    public static class FlareColor extends SettingsEntry<me.lizardofoz.drgflares.util.FlareColor>
    {
        public FlareColor(me.lizardofoz.drgflares.util.FlareColor defaultValue, String configKey)
        {
            super(defaultValue, configKey);
            valueAsJsonElement = () -> new JsonPrimitive(value.toString());
            jsonElementAsValue = jsonElement -> {
                if (jsonElement == null)
                    return defaultValue;
                String value = jsonElement.getAsString();
                for (me.lizardofoz.drgflares.util.FlareColor flareColor : me.lizardofoz.drgflares.util.FlareColor.values())
                {
                    if (flareColor.toString().equals(value))
                        return flareColor;
                }
                return defaultValue;
            };
        }

        @Override
        public me.lizardofoz.drgflares.util.FlareColor putValueInRange(me.lizardofoz.drgflares.util.FlareColor value)
        {
            return value;
        }
    }
}