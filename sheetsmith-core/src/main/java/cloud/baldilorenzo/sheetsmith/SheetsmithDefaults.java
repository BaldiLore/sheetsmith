package cloud.baldilorenzo.sheetsmith;

import cloud.baldilorenzo.sheetsmith.internal.metadata.MetadataValidator;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;

import java.util.Objects;

/**
 * Application defaults: the default formats of dates, date-times and numbers, and the preset and accent colour of
 * sheet classes that do not choose their own.
 * <p>
 * Set them with {@link Sheetsmith.Builder#defaults(SheetsmithDefaults)}. In Spring Boot applications they are
 * bound from the {@code sheetsmith.*} properties. When nothing is configured, {@link #standard()} applies.
 *
 * <h2>Components</h2>
 * <table class="striped">
 *   <caption>Components of the application defaults</caption>
 *   <thead>
 *     <tr><th scope="col">Component</th><th scope="col">Meaning</th><th scope="col">Standard value</th>
 *         <th scope="col">Constraint</th></tr>
 *   </thead>
 *   <tbody>
 *     <tr><th scope="row">{@code dateFormat}</th><td>Format of date cells, such as {@code LocalDate} values</td>
 *         <td>{@code yyyy-mm-dd}</td><td>Not blank</td></tr>
 *     <tr><th scope="row">{@code dateTimeFormat}</th><td>Format of date-time cells, such as {@code LocalDateTime}
 *         values</td><td>{@code yyyy-mm-dd hh:mm:ss}</td><td>Not blank</td></tr>
 *     <tr><th scope="row">{@code numberFormat}</th><td>Format of numeric cells, integers included</td>
 *         <td>empty, the Excel "General" format</td><td>May be empty</td></tr>
 *     <tr><th scope="row">{@code preset}</th><td>Preset of sheet classes that declare
 *         {@link TablePreset#INHERIT}</td><td>{@link TablePreset#NONE}</td><td>Not {@code INHERIT}</td></tr>
 *     <tr><th scope="row">{@code accentColor}</th><td>Accent colour of sheet classes that declare none</td>
 *         <td>{@code #4472C4}</td><td>A valid colour, as described in
 *         {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle}</td></tr>
 *   </tbody>
 * </table>
 *
 * <h2>Default formats</h2>
 * A default format applies only when the effective style of the cell sets no format, that is when neither
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#format()} nor the
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle#dataFormat()} of any style applied to the cell sets
 * one. A data cell therefore takes its format from the first of these sources that sets one: the column
 * {@code format}, the {@code dataFormat} of the cascade, the default format for its kind of value.
 * <p>
 * {@code numberFormat} applies to every numeric cell, integers included: with {@code #,##0.00}, an integer
 * column shows two decimals unless it has its own format, for example {@code format = "0"}. Text, boolean and
 * blank cells receive no default format.
 *
 * <h2 id="excel-format-syntax">Excel format syntax</h2>
 * Every format of sheetsmith, here, in {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#format()} and
 * in {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle#dataFormat()}, uses the Excel format syntax, the
 * one of the Excel "Format Cells" dialog. It is not the syntax of {@link java.time.format.DateTimeFormatter} or
 * {@link java.text.DecimalFormat}: the two look similar but are not the same. Formats are written to the file as
 * they are, without translation.
 * <table class="striped">
 *   <caption>Common date and time codes</caption>
 *   <thead>
 *     <tr><th scope="col">Meaning</th><th scope="col">Excel</th><th scope="col">{@code DateTimeFormatter}</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr><th scope="row">Day of the month: 5, 05</th><td>{@code d}, {@code dd}</td><td>{@code d}, {@code dd}</td>
 *     </tr>
 *     <tr><th scope="row">Day name: Mon, Monday</th><td>{@code ddd}, {@code dddd}</td>
 *         <td>{@code EEE}, {@code EEEE}</td></tr>
 *     <tr><th scope="row">Month: 3, 03</th><td>{@code m}, {@code mm}</td><td>{@code M}, {@code MM}</td></tr>
 *     <tr><th scope="row">Month name: Mar, March</th><td>{@code mmm}, {@code mmmm}</td>
 *         <td>{@code MMM}, {@code MMMM}</td></tr>
 *     <tr><th scope="row">Year: 26, 2026</th><td>{@code yy}, {@code yyyy}</td><td>{@code yy}, {@code yyyy}</td>
 *     </tr>
 *     <tr><th scope="row">Hours, 0 to 23</th><td>{@code h}, {@code hh}</td><td>{@code H}, {@code HH}</td></tr>
 *     <tr><th scope="row">Minutes</th><td>{@code m}, {@code mm}, after an hour code or before a seconds
 *         code</td><td>{@code m}, {@code mm}</td></tr>
 *     <tr><th scope="row">Seconds</th><td>{@code s}, {@code ss}</td><td>{@code s}, {@code ss}</td></tr>
 *   </tbody>
 * </table>
 * <p>
 * In Excel, {@code m} and {@code mm} mean the month, unless they follow an hour code or precede a seconds code, in
 * which case they mean minutes. So {@code dd/mm/yyyy hh:mm} shows the day, the month, the year, the hours and the
 * minutes. For example, the Java pattern {@code dd/MM/yyyy} is written in Excel as {@code dd/mm/yyyy}, and
 * {@code HH:mm} as {@code hh:mm}.
 * <table class="striped">
 *   <caption>Common number formats</caption>
 *   <thead>
 *     <tr><th scope="col">Format</th><th scope="col">Example output</th></tr>
 *   </thead>
 *   <tbody>
 *     <tr><th scope="row">{@code 0}</th><td>{@code 1234}</td></tr>
 *     <tr><th scope="row">{@code #,##0.00}</th><td>{@code 1,234.50}</td></tr>
 *     <tr><th scope="row">{@code 0.0%}</th><td>{@code 12.5%}</td></tr>
 *     <tr><th scope="row">{@code #,##0.00 "EUR"}</th><td>{@code 1,234.50 EUR}</td></tr>
 *     <tr><th scope="row">{@code @}</th><td>The value as text</td></tr>
 *   </tbody>
 * </table>
 * <p>
 * In number formats, {@code 0} is a digit always shown, {@code #} a digit shown only when significant, {@code ,}
 * the thousands separator, {@code .} the decimal separator, and text in double quotes is shown as it is. The
 * separators actually displayed follow the regional settings of the person who opens the file: {@code #,##0.00}
 * is displayed as {@code 1.234,50} on an Italian system.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * SheetsmithDefaults defaults = new SheetsmithDefaults(
 *         "dd/mm/yyyy",          // dates
 *         "dd/mm/yyyy hh:mm",    // date-times
 *         "",                    // numbers: General
 *         TablePreset.LIGHT,     // preset of sheet classes that declare INHERIT
 *         "#1F4E79");            // accent colour of sheet classes that declare none
 *
 * Sheetsmith sheetsmith = Sheetsmith.builder().defaults(defaults).build();
 * }</pre>
 *
 * @param dateFormat     the default Excel format of date cells, not blank
 * @param dateTimeFormat the default Excel format of date-time cells, not blank
 * @param numberFormat   the default Excel format of numeric cells, integers included; empty means the Excel
 *                       "General" format
 * @param preset         the preset of sheet classes that declare {@link TablePreset#INHERIT}; not {@code INHERIT}
 *                       itself
 * @param accentColor    the accent colour of sheet classes that declare none, as {@code #RRGGBB} or the name of an
 *                       {@link org.apache.poi.ss.usermodel.IndexedColors} constant; used only when the effective
 *                       preset is not {@link TablePreset#NONE}
 * @see Sheetsmith.Builder#defaults(SheetsmithDefaults)
 * @see cloud.baldilorenzo.sheetsmith.style.TablePreset
 * @since 1.0.0
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
     * Creates defaults, checking every component.
     *
     * @param dateFormat     the format of date cells, not null and not blank
     * @param dateTimeFormat the format of date-time cells, not null and not blank
     * @param numberFormat   the format of numeric cells, not null, possibly empty
     * @param preset         the default preset, not null and not {@code INHERIT}
     * @param accentColor    the default accent colour, not null, as {@code #RRGGBB} or the name of an
     *                       {@link org.apache.poi.ss.usermodel.IndexedColors} constant
     * @throws IllegalArgumentException if {@code dateFormat} or {@code dateTimeFormat} is blank, {@code preset} is
     *                                  {@link TablePreset#INHERIT}, or {@code accentColor} is not a valid colour,
     *                                  an empty string included
     * @throws NullPointerException     if an argument is null
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
     * Returns the standard defaults: dates {@code yyyy-mm-dd}, date-times {@code yyyy-mm-dd hh:mm:ss}, numbers in
     * the Excel "General" format, preset {@link TablePreset#NONE} and accent colour {@code #4472C4}.
     *
     * @return the standard defaults
     */
    public static SheetsmithDefaults standard() {
        return STANDARD;
    }
}
