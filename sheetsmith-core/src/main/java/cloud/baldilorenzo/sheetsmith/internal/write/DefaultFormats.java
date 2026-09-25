package cloud.baldilorenzo.sheetsmith.internal.write;

import java.util.Objects;

/**
 * Formats applied to date, date-time and numeric cells whose effective style sets no data format.
 *
 * @param dateFormat     the Excel format of dates
 * @param dateTimeFormat the Excel format of date-times
 * @param numberFormat   the Excel format of numbers; empty means the Excel "General" format
 */
public record DefaultFormats(String dateFormat, String dateTimeFormat, String numberFormat) {

    /**
     * Creates default formats.
     *
     * @param dateFormat     the Excel format of dates, not null
     * @param dateTimeFormat the Excel format of date-times, not null
     * @param numberFormat   the Excel format of numbers, not null, possibly empty
     */
    public DefaultFormats {
        Objects.requireNonNull(dateFormat, "dateFormat");
        Objects.requireNonNull(dateTimeFormat, "dateTimeFormat");
        Objects.requireNonNull(numberFormat, "numberFormat");
    }
}
