package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;

import java.util.Objects;

/**
 * Produces the style layers of a preset from an accent colour {@code A}.
 *
 * <table>
 *   <caption>Preset layers</caption>
 *   <tr><th>Layer</th><th>LIGHT</th><th>MEDIUM</th><th>DARK</th></tr>
 *   <tr><td>Title</td><td colspan="3">bold, 14 pt, font shade(A, 0.25)</td></tr>
 *   <tr><td>Header base</td><td>bold, font shade(A, 0.25), bottom border MEDIUM colour A</td>
 *       <td>bold, fill A, font contrast(A)</td>
 *       <td>bold, fill shade(A, 0.5), font contrast(shade(A, 0.5))</td></tr>
 *   <tr><td>Body base</td><td>bottom border THIN colour tint(A, 0.75)</td>
 *       <td>all borders THIN colour tint(A, 0.6)</td><td>none</td></tr>
 *   <tr><td>Body odd</td><td>fill tint(A, 0.85)</td><td>fill tint(A, 0.8)</td>
 *       <td>fill A, font contrast(A)</td></tr>
 *   <tr><td>Body even</td><td>none</td><td>none</td>
 *       <td>fill shade(A, 0.25), font contrast(shade(A, 0.25))</td></tr>
 * </table>
 */
public final class PresetFactory {

    private PresetFactory() {
    }

    /**
     * Returns the layers of a preset.
     *
     * @param preset the effective preset, not {@code INHERIT}
     * @param accent the effective accent colour, {@code #RRGGBB} or an {@code IndexedColors} name
     * @return the layers; empty for {@link TablePreset#NONE}
     * @throws IllegalArgumentException if the preset is {@code INHERIT}
     */
    public static Layers layers(TablePreset preset, String accent) {
        Objects.requireNonNull(preset, "preset");
        Objects.requireNonNull(accent, "accent");
        switch (preset) {
            case NONE:
                return Layers.NONE;
            case LIGHT:
                return light(accent);
            case MEDIUM:
                return medium(accent);
            case DARK:
                return dark(accent);
            default:
                throw new IllegalArgumentException("the preset must be resolved before building its layers: "
                        + preset);
        }
    }

    private static StyleAttributes title(String accent) {
        return StyleAttributes.builder().bold(true).fontSize(14).fontColor(ColorUtils.shade(accent, 0.25)).build();
    }

    private static Layers light(String accent) {
        return new Layers(
                title(accent),
                StyleAttributes.builder().bold(true).fontColor(ColorUtils.shade(accent, 0.25))
                        .borderBottom(Border.MEDIUM).borderBottomColor(accent).build(),
                StyleAttributes.builder().borderBottom(Border.THIN)
                        .borderBottomColor(ColorUtils.tint(accent, 0.75)).build(),
                StyleAttributes.builder().fillColor(ColorUtils.tint(accent, 0.85)).build(),
                StyleAttributes.EMPTY);
    }

    private static Layers medium(String accent) {
        return new Layers(
                title(accent),
                StyleAttributes.builder().bold(true).fillColor(accent).fontColor(ColorUtils.contrast(accent)).build(),
                StyleAttributes.builder().border(Border.THIN).borderColor(ColorUtils.tint(accent, 0.6)).build(),
                StyleAttributes.builder().fillColor(ColorUtils.tint(accent, 0.8)).build(),
                StyleAttributes.EMPTY);
    }

    private static Layers dark(String accent) {
        String header = ColorUtils.shade(accent, 0.5);
        String even = ColorUtils.shade(accent, 0.25);
        return new Layers(
                title(accent),
                StyleAttributes.builder().bold(true).fillColor(header).fontColor(ColorUtils.contrast(header)).build(),
                StyleAttributes.EMPTY,
                StyleAttributes.builder().fillColor(accent).fontColor(ColorUtils.contrast(accent)).build(),
                StyleAttributes.builder().fillColor(even).fontColor(ColorUtils.contrast(even)).build());
    }

    /**
     * Style layers of a preset, applied below the styles declared on the class.
     *
     * @param title      the title layer
     * @param headerBase the layer of every header cell
     * @param bodyBase   the layer of every data cell
     * @param bodyOdd    the layer of odd data rows
     * @param bodyEven   the layer of even data rows
     */
    public record Layers(
            StyleAttributes title,
            StyleAttributes headerBase,
            StyleAttributes bodyBase,
            StyleAttributes bodyOdd,
            StyleAttributes bodyEven) {

        /** No preset: every layer is empty. */
        public static final Layers NONE = new Layers(StyleAttributes.EMPTY, StyleAttributes.EMPTY,
                StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY);

        /**
         * Creates preset layers.
         *
         * @throws NullPointerException if a layer is null
         */
        public Layers {
            Objects.requireNonNull(title, "title");
            Objects.requireNonNull(headerBase, "headerBase");
            Objects.requireNonNull(bodyBase, "bodyBase");
            Objects.requireNonNull(bodyOdd, "bodyOdd");
            Objects.requireNonNull(bodyEven, "bodyEven");
        }
    }
}
