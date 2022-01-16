package me.lizardofoz.drgflares.util;

import lombok.AllArgsConstructor;

import java.util.Random;

@AllArgsConstructor
public enum FlareColor
{
    RANDOM(-2, "random"),
    RANDOM_BRIGHT_ONLY(-1, "random_bright_only"),
    WHITE(0, "white"),
    ORANGE(1, "orange"),
    MAGENTA(2, "magenta"),
    LIGHT_BLUE(3, "light_blue"),
    YELLOW(4, "yellow"),
    LIME(5, "lime"),
    PINK(6, "pink"),
    GRAY(7, "gray"),
    LIGHT_GRAY(8, "light_gray"),
    CYAN(9, "cyan"),
    PURPLE(10, "purple"),
    BLUE(11, "blue"),
    BROWN(12, "brown"),
    GREEN(13, "green"),
    RED(14, "red"),
    BLACK(15, "black");

    public static final FlareColor[] colors = {WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY, LIGHT_GRAY, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK};
    public static final FlareColor[] brightColors = {WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, CYAN, PURPLE, BLUE, BROWN, GREEN, RED};

    public final int id;
    public final String name;

    public static FlareColor byId(int id)
    {
        for (FlareColor flareColor : values())
        {
            if (flareColor.id == id)
                return flareColor;
        }
        return RED;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public static class RandomColorPicker
    {
        private static FlareColor nextRandomColor = FlareColor.colors[new Random().nextInt(FlareColor.colors.length)];

        public static FlareColor unwrapRandom(FlareColor color, boolean reroll)
        {
            if (color.id >= 0)
                return color;
            if (!reroll)
                return nextRandomColor;

            FlareColor prevColor = nextRandomColor;
            while (nextRandomColor == prevColor)
            {
                if (color == FlareColor.RANDOM_BRIGHT_ONLY)
                    nextRandomColor = FlareColor.brightColors[new Random().nextInt(FlareColor.brightColors.length)];
                else
                    nextRandomColor = FlareColor.colors[new Random().nextInt(FlareColor.colors.length)];
            }
            return prevColor;
        }
    }
}