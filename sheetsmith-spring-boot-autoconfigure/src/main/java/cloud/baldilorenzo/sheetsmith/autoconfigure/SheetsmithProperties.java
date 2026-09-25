package cloud.baldilorenzo.sheetsmith.autoconfigure;

import cloud.baldilorenzo.sheetsmith.SheetsmithDefaults;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Configuration properties of sheetsmith, bound from the {@code sheetsmith} prefix.
 *
 * @param formats     Default formats of dates, date-times and numbers.
 * @param preset      Preset of classes that do not choose one. Must not be INHERIT.
 * @param accentColor Accent colour of presets for classes that do not choose one, as #RRGGBB or the name of an
 *                    IndexedColors constant.
 * @param validation  Startup validation of annotated classes.
 */
@ConfigurationProperties("sheetsmith")
public record SheetsmithProperties(
        @DefaultValue Formats formats,
        @DefaultValue("NONE") TablePreset preset,
        @DefaultValue("#4472C4") String accentColor,
        @DefaultValue Validation validation) {

    /**
     * Returns the core defaults described by these properties.
     *
     * @return the defaults
     * @throws IllegalArgumentException if a value is invalid, for example {@code preset=INHERIT}
     */
    public SheetsmithDefaults toDefaults() {
        return new SheetsmithDefaults(formats.date(), formats.dateTime(), formats.number(), preset, accentColor);
    }

    /**
     * Default formats, in Excel format syntax, applied to cells whose style sets no format.
     *
     * @param date     Excel format of LocalDate values.
     * @param dateTime Excel format of LocalDateTime values.
     * @param number   Excel format of numeric values. Empty means the "General" format.
     */
    public record Formats(
            @DefaultValue("yyyy-mm-dd") String date,
            @DefaultValue("yyyy-mm-dd hh:mm:ss") String dateTime,
            @DefaultValue("") String number) {

        /**
         * Creates the formats; a missing number format means the "General" format.
         *
         * @param date     format of dates
         * @param dateTime format of date-times
         * @param number   format of numbers, or null
         */
        public Formats {
            number = number == null ? "" : number;
        }
    }

    /**
     * Startup validation.
     *
     * @param packages Packages scanned at startup for classes annotated with @ExcelSheet, which are all validated.
     *                 Empty disables the validation.
     */
    public record Validation(@DefaultValue List<String> packages) {

        /**
         * Creates the validation settings.
         *
         * @param packages packages to scan, or null for none
         */
        public Validation {
            packages = packages == null ? List.of() : List.copyOf(packages);
        }
    }
}
