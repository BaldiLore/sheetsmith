package cloud.baldilorenzo.sheetsmith.internal.style;

import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;

import java.util.Locale;
import java.util.Objects;

/**
 * Colour arithmetic used by presets. Colours are {@code #RRGGBB} strings or {@link IndexedColors} names; results
 * are always {@code #RRGGBB}.
 */
public final class ColorUtils {

    private ColorUtils() {
    }

    /**
     * Mixes a colour with white.
     *
     * @param color    the colour
     * @param fraction from 0 (the colour itself) to 1 (white)
     * @return the tinted colour
     */
    public static String tint(String color, double fraction) {
        checkFraction(fraction);
        int[] rgb = rgb(color);
        return hex(mix(rgb[0], 255, fraction), mix(rgb[1], 255, fraction), mix(rgb[2], 255, fraction));
    }

    /**
     * Mixes a colour with black.
     *
     * @param color    the colour
     * @param fraction from 0 (the colour itself) to 1 (black)
     * @return the shaded colour
     */
    public static String shade(String color, double fraction) {
        checkFraction(fraction);
        int[] rgb = rgb(color);
        return hex(mix(rgb[0], 0, fraction), mix(rgb[1], 0, fraction), mix(rgb[2], 0, fraction));
    }

    /**
     * Returns the text colour readable on a background: white when the relative luminance of the background is
     * below 0.5, black otherwise.
     *
     * @param color the background colour
     * @return {@code #FFFFFF} or {@code #000000}
     */
    public static String contrast(String color) {
        return luminance(color) < 0.5 ? "#FFFFFF" : "#000000";
    }

    /**
     * Returns the relative luminance of a colour, as defined by WCAG, from 0 (black) to 1 (white).
     *
     * @param color the colour
     * @return the relative luminance
     */
    public static double luminance(String color) {
        int[] rgb = rgb(color);
        return 0.2126 * linear(rgb[0]) + 0.7152 * linear(rgb[1]) + 0.0722 * linear(rgb[2]);
    }

    /**
     * Returns the red, green and blue components of a colour. {@link IndexedColors#AUTOMATIC}, which has no fixed
     * value, is treated as black, the automatic colour of text.
     *
     * @param color {@code #RRGGBB} or an {@link IndexedColors} name
     * @return the components, from 0 to 255
     * @throws IllegalArgumentException if the colour is not valid
     */
    public static int[] rgb(String color) {
        Objects.requireNonNull(color, "color");
        if (color.startsWith("#") && color.length() == 7) {
            int value = Integer.parseInt(color.substring(1), 16);
            return new int[] {(value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF};
        }
        byte[] bytes = DefaultIndexedColorMap.getDefaultRGB(IndexedColors.valueOf(color).getIndex());
        if (bytes == null) {
            return new int[] {0, 0, 0};
        }
        return new int[] {bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF};
    }

    private static int mix(int channel, int target, double fraction) {
        return (int) Math.round(channel + (target - channel) * fraction);
    }

    private static double linear(int channel) {
        double c = channel / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static String hex(int r, int g, int b) {
        return String.format(Locale.ROOT, "#%02X%02X%02X", r, g, b);
    }

    private static void checkFraction(double fraction) {
        if (!(fraction >= 0 && fraction <= 1)) {
            throw new IllegalArgumentException("fraction must be between 0 and 1: " + fraction);
        }
    }
}
