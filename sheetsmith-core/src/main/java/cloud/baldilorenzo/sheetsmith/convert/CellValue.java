package cloud.baldilorenzo.sheetsmith.convert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A value that sheetsmith writes natively to a cell: text, number, boolean, date, date-time or blank.
 * <p>
 * Converters return a {@code CellValue}, created with the factory methods {@link #text(String)},
 * {@link #number(double)}, {@link #bool(boolean)}, {@link #date(LocalDate)}, {@link #dateTime(LocalDateTime)}
 * and {@link #blank()}. The built-in converters produce the same kinds of values. The display format of the cell
 * comes from the styles, never from the value.
 *
 * <h2>How each kind is written</h2>
 * <ul>
 *   <li>{@link Text}: always a text cell, never interpreted, so values such as postal codes or product codes keep
 *       their leading zeros.</li>
 *   <li>{@link Numeric}: a number cell. Excel stores numbers as 64-bit floating point values, with 15 significant
 *       digits.</li>
 *   <li>{@link Bool}: an Excel boolean cell, shown as {@code TRUE} or {@code FALSE}.</li>
 *   <li>{@link Date} and {@link DateTime}: Excel date cells. When the effective style of the cell sets no format,
 *       they receive the default date or date-time format of
 *       {@link cloud.baldilorenzo.sheetsmith.SheetsmithDefaults}.</li>
 *   <li>{@link Blank}: an empty cell that keeps its resolved style, so fills and borders stay continuous.</li>
 * </ul>
 * Numeric cells receive the default number format, when one is configured and the effective style sets no format.
 *
 * <h2 id="known-limitations">Known Excel limitations</h2>
 * The following behaviours come from Excel and Apache POI. Apart from the text length, sheetsmith does not turn
 * them into errors; handle them with a converter, or with naming, when they matter.
 * <table class="striped">
 *   <caption>Known Excel limitations</caption>
 *   <thead>
 *     <tr><th scope="col">Case</th><th scope="col">What happens</th><th scope="col">What to do</th></tr>
 *   </thead>
 *   <tbody>
 *     <tr><th scope="row">Date or date-time before 1900-01-01</th>
 *         <td>Excel cannot represent it: the cell contains {@code -1} and is displayed as {@code #####}</td>
 *         <td>Convert such values to text</td></tr>
 *     <tr><th scope="row">{@code NaN} or infinite number</th>
 *         <td>The cell becomes an Excel error: {@code #NUM!} for {@code NaN}, {@code #DIV/0!} for infinity</td>
 *         <td>Map them, for example to {@link #blank()}</td></tr>
 *     <tr><th scope="row">Number with more than 15 significant digits</th>
 *         <td>Precision is lost: a {@code long} above 2<sup>53</sup> or a large {@code BigDecimal} is rounded</td>
 *         <td>Write identifiers and exact decimals as text</td></tr>
 *     <tr><th scope="row">Text longer than 32,767 characters</th>
 *         <td>Excel cannot store it: generation fails with a
 *         {@link cloud.baldilorenzo.sheetsmith.SheetsmithGenerationException}</td>
 *         <td>Shorten the text before writing it</td></tr>
 *     <tr><th scope="row">Sheet named {@code History}</th>
 *         <td>Excel reserves the name for change tracking and may refuse or repair the file</td>
 *         <td>Choose another sheet name</td></tr>
 *     <tr><th scope="row">Very large exports</th>
 *         <td>The whole workbook is built in memory, so memory grows with the number of cells; automatic column
 *         sizing also takes longer as the number of rows grows</td>
 *         <td>Measure with realistic volumes; for large sheets, disable automatic sizing and set explicit widths;
 *         prefer {@link cloud.baldilorenzo.sheetsmith.Sheetsmith#generate(java.util.List, java.io.OutputStream)}
 *         to avoid an extra copy of the file</td></tr>
 *   </tbody>
 * </table>
 *
 * @see CellConverter
 * @since 1.0.0
 */
public sealed interface CellValue {

    /**
     * A text cell. The text is written as it is, without interpretation, so leading zeros are kept. A text longer
     * than 32,767 characters, the Excel limit, fails the generation.
     *
     * @param value the text, not null
     * @see CellValue#text(String)
     * @since 1.0.0
     */
    record Text(String value) implements CellValue {

        /**
         * Creates a text value.
         *
         * @param value the text, not null
         * @throws NullPointerException if {@code value} is null
         */
        public Text {
            Objects.requireNonNull(value, "value");
        }
    }

    /**
     * A numeric cell. Excel stores numbers as 64-bit floating point values with 15 significant digits; {@code NaN}
     * and infinite values become error cells, as described in {@link CellValue}.
     *
     * @param value the number
     * @see CellValue#number(double)
     * @since 1.0.0
     */
    record Numeric(double value) implements CellValue {
    }

    /**
     * A boolean cell, shown by Excel as {@code TRUE} or {@code FALSE}.
     *
     * @param value the boolean
     * @see CellValue#bool(boolean)
     * @since 1.0.0
     */
    record Bool(boolean value) implements CellValue {
    }

    /**
     * A date cell. Receives the default date format when the effective style of the cell sets no format. Dates
     * before 1900-01-01 cannot be represented by Excel, as described in {@link CellValue}.
     *
     * @param value the date, not null
     * @see CellValue#date(LocalDate)
     * @since 1.0.0
     */
    record Date(LocalDate value) implements CellValue {

        /**
         * Creates a date value.
         *
         * @param value the date, not null
         * @throws NullPointerException if {@code value} is null
         */
        public Date {
            Objects.requireNonNull(value, "value");
        }
    }

    /**
     * A date-time cell. Receives the default date-time format when the effective style of the cell sets no format.
     * Date-times before 1900-01-01 cannot be represented by Excel, as described in {@link CellValue}.
     *
     * @param value the date-time, not null
     * @see CellValue#dateTime(LocalDateTime)
     * @since 1.0.0
     */
    record DateTime(LocalDateTime value) implements CellValue {

        /**
         * Creates a date-time value.
         *
         * @param value the date-time, not null
         * @throws NullPointerException if {@code value} is null
         */
        public DateTime {
            Objects.requireNonNull(value, "value");
        }
    }

    /**
     * An empty cell that keeps its resolved style. The only instance is returned by {@link CellValue#blank()}.
     *
     * @since 1.0.0
     */
    final class Blank implements CellValue {

        private static final Blank INSTANCE = new Blank();

        private Blank() {
        }

        @Override
        public String toString() {
            return "Blank";
        }
    }

    /**
     * Returns a text value, written as a text cell.
     *
     * @param value the text, not null
     * @return the cell value
     * @throws NullPointerException if {@code value} is null
     */
    static CellValue text(String value) {
        return new Text(value);
    }

    /**
     * Returns a numeric value, written as a number cell.
     *
     * @param value the number; {@code NaN} and infinite values become Excel error cells
     * @return the cell value
     */
    static CellValue number(double value) {
        return new Numeric(value);
    }

    /**
     * Returns a boolean value, written as an Excel boolean cell.
     *
     * @param value the boolean
     * @return the cell value
     */
    static CellValue bool(boolean value) {
        return new Bool(value);
    }

    /**
     * Returns a date value, written as an Excel date cell.
     *
     * @param value the date, not null
     * @return the cell value
     * @throws NullPointerException if {@code value} is null
     */
    static CellValue date(LocalDate value) {
        return new Date(value);
    }

    /**
     * Returns a date-time value, written as an Excel date cell with a time.
     *
     * @param value the date-time, not null
     * @return the cell value
     * @throws NullPointerException if {@code value} is null
     */
    static CellValue dateTime(LocalDateTime value) {
        return new DateTime(value);
    }

    /**
     * Returns the blank value, written as an empty cell that keeps its resolved style.
     *
     * @return the singleton blank value; every call returns the same instance
     */
    static CellValue blank() {
        return Blank.INSTANCE;
    }
}
