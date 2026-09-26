package cloud.baldilorenzo.sheetsmith.autoconfigure;

import cloud.baldilorenzo.sheetsmith.SheetsmithDefaults;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Configuration properties of sheetsmith, bound from the {@code sheetsmith} prefix.
 * <p>
 * The formats, the preset and the accent colour become the {@link SheetsmithDefaults} of the auto-configured
 * {@link cloud.baldilorenzo.sheetsmith.Sheetsmith} bean; their defaults are those of
 * {@link SheetsmithDefaults#standard()}. Formats use the Excel format syntax described in
 * {@link SheetsmithDefaults}, not the syntax of {@link java.time.format.DateTimeFormatter}. Invalid values, such
 * as {@code sheetsmith.preset=INHERIT}, a blank date format or an invalid colour, stop the application at startup.
 * <p>
 * In YAML, quote every value that starts with {@code #}, otherwise it is read as a comment:
 * <pre>{@code
 * sheetsmith:
 *   formats:
 *     date: dd/mm/yyyy
 *     date-time: dd/mm/yyyy hh:mm
 *     number: "#,##0.00"
 *   preset: LIGHT
 *   accent-color: "#1F4E79"
 *   validation:
 *     packages:
 *       - com.example.export
 * }</pre>
 *
 * @param formats     Default Excel formats of date, date-time and numeric cells, applied only to cells whose style
 *                    sets no format.
 * @param preset      Preset of the sheet classes that declare preset INHERIT: NONE, LIGHT, MEDIUM or DARK. Default
 *                    NONE. INHERIT is not allowed and stops the application at startup.
 * @param accentColor Accent colour of the preset of the sheet classes that declare no accent colour, as #RRGGBB or
 *                    the name of an Apache POI IndexedColors constant. Default #4472C4. In YAML, quote the value,
 *                    because # starts a comment. An invalid colour stops the application at startup.
 * @param validation  Startup validation of sheet classes.
 * @see SheetsmithAutoConfiguration
 * @since 1.0.0
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
     * @throws IllegalArgumentException if a value is invalid, for example {@code preset=INHERIT}, a blank date
     *                                  format or an invalid accent colour
     */
    public SheetsmithDefaults toDefaults() {
        return new SheetsmithDefaults(formats.date(), formats.dateTime(), formats.number(), preset, accentColor);
    }

    /**
     * Default formats, in Excel format syntax, applied only to cells whose style sets no format.
     *
     * @param date     Default Excel format of date cells, such as LocalDate values, for example dd/mm/yyyy. Excel
     *                 syntax, not DateTimeFormatter syntax: mm is the month. Default yyyy-mm-dd. Must not be
     *                 blank.
     * @param dateTime Default Excel format of date-time cells, such as LocalDateTime values, for example
     *                 dd/mm/yyyy hh:mm. Excel syntax, not DateTimeFormatter syntax: mm is the month, or the minutes
     *                 after an hour code. Default yyyy-mm-dd hh:mm:ss. Must not be blank.
     * @param number   Default Excel format of numeric cells, integers included, for example #,##0.00. Default
     *                 empty, which means the Excel General format. In YAML, quote values that start with #.
     * @since 1.0.0
     */
    public record Formats(
            @DefaultValue("yyyy-mm-dd") String date,
            @DefaultValue("yyyy-mm-dd hh:mm:ss") String dateTime,
            @DefaultValue("") String number) {

        /**
         * Creates the formats; a missing number format means the Excel "General" format.
         *
         * @param date     the format of date cells
         * @param dateTime the format of date-time cells
         * @param number   the format of numeric cells, or null for the "General" format
         */
        public Formats {
            number = number == null ? "" : number;
        }
    }

    /**
     * Startup validation of sheet classes.
     *
     * @param packages Packages scanned at startup, subpackages included. Every type annotated directly with the
     *                 annotation @ExcelSheet is validated, abstract and nested classes included; annotation types
     *                 and types only meta-annotated with @ExcelSheet are not. Any error stops the application with
     *                 one exception listing all of them. Default empty, which disables the validation.
     * @since 1.0.0
     */
    public record Validation(@DefaultValue List<String> packages) {

        /**
         * Creates the validation settings.
         *
         * @param packages the packages to scan, or null for none
         */
        public Validation {
            packages = packages == null ? List.of() : List.copyOf(packages);
        }
    }
}
