package cloud.baldilorenzo.sheetsmith;

import cloud.baldilorenzo.sheetsmith.internal.metadata.MetadataValidator;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;

import java.util.Objects;

/**
 * Application-wide defaults. Formats use Excel format syntax, for example {@code dd/mm/yyyy}, and apply only to
 * cells whose effective style sets no format.
 *
 * @param dateFormat     format of {@code LocalDate} values, not blank
 * @param dateTimeFormat format of {@code LocalDateTime} values, not blank
 * @param numberFormat   format of numeric values; empty means the Excel "General" format
 * @param preset         preset of classes that do not choose one, not {@link TablePreset#INHERIT}
 * @param accentColor    accent colour of presets for classes that do not choose one, as {@code #RRGGBB} or the name
 *                       of an {@link org.apache.poi.ss.usermodel.IndexedColors} constant
 */
public record SheetsmithDefaults(
        String dateFormat,
        String dateTimeFormat,
        String numberFormat,
        TablePreset preset,
        String accentColor) {

    private static final SheetsmithDefaults STANDARD =
            new SheetsmithDefaults("yyyy-mm-dd", "yyyy-mm-dd hh:mm:ss", "", TablePreset.NONE, "#4472C4");

    /**
     * Creates defaults.
     *
     * @param dateFormat     format of dates, not null and not blank
     * @param dateTimeFormat format of date-times, not null and not blank
     * @param numberFormat   format of numbers, not null, possibly empty
     * @param preset         default preset, not null and not {@code INHERIT}
     * @param accentColor    default accent colour, not null and valid
     * @throws IllegalArgumentException if a value is invalid
     */
    public SheetsmithDefaults {
        Objects.requireNonNull(dateFormat, "dateFormat");
        Objects.requireNonNull(dateTimeFormat, "dateTimeFormat");
        Objects.requireNonNull(numberFormat, "numberFormat");
        Objects.requireNonNull(preset, "preset");
        Objects.requireNonNull(accentColor, "accentColor");
        if (dateFormat.isBlank()) {
            throw new IllegalArgumentException("dateFormat must not be blank");
        }
        if (dateTimeFormat.isBlank()) {
            throw new IllegalArgumentException("dateTimeFormat must not be blank");
        }
        if (preset == TablePreset.INHERIT) {
            throw new IllegalArgumentException("preset must not be INHERIT");
        }
        if (!MetadataValidator.isValidColor(accentColor)) {
            throw new IllegalArgumentException("accentColor '" + accentColor
                    + "' is not a valid colour: expected #RRGGBB or the name of an IndexedColors constant");
        }
    }

    /**
     * Returns the standard defaults: dates {@code yyyy-mm-dd}, date-times {@code yyyy-mm-dd hh:mm:ss}, numbers in the
     * "General" format, preset {@link TablePreset#NONE}, accent colour {@code #4472C4}.
     *
     * @return the standard defaults
     */
    public static SheetsmithDefaults standard() {
        return STANDARD;
    }
}
