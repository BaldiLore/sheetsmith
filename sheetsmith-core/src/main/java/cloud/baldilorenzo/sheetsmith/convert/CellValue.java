package cloud.baldilorenzo.sheetsmith.convert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A value that sheetsmith can write natively to a cell.
 */
public sealed interface CellValue {

    /**
     * A text cell.
     *
     * @param value the text, not null
     */
    record Text(String value) implements CellValue {

        /**
         * Creates a text value.
         *
         * @param value the text, not null
         */
        public Text {
            Objects.requireNonNull(value, "value");
        }
    }

    /**
     * A numeric cell.
     *
     * @param value the number
     */
    record Numeric(double value) implements CellValue {
    }

    /**
     * A boolean cell.
     *
     * @param value the boolean
     */
    record Bool(boolean value) implements CellValue {
    }

    /**
     * A date cell.
     *
     * @param value the date, not null
     */
    record Date(LocalDate value) implements CellValue {

        /**
         * Creates a date value.
         *
         * @param value the date, not null
         */
        public Date {
            Objects.requireNonNull(value, "value");
        }
    }

    /**
     * A date-time cell.
     *
     * @param value the date-time, not null
     */
    record DateTime(LocalDateTime value) implements CellValue {

        /**
         * Creates a date-time value.
         *
         * @param value the date-time, not null
         */
        public DateTime {
            Objects.requireNonNull(value, "value");
        }
    }

    /**
     * An empty cell. The only instance is returned by {@link CellValue#blank()}.
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
     * Returns a text value.
     *
     * @param value the text, not null
     * @return the cell value
     */
    static CellValue text(String value) {
        return new Text(value);
    }

    /**
     * Returns a numeric value.
     *
     * @param value the number
     * @return the cell value
     */
    static CellValue number(double value) {
        return new Numeric(value);
    }

    /**
     * Returns a boolean value.
     *
     * @param value the boolean
     * @return the cell value
     */
    static CellValue bool(boolean value) {
        return new Bool(value);
    }

    /**
     * Returns a date value.
     *
     * @param value the date, not null
     * @return the cell value
     */
    static CellValue date(LocalDate value) {
        return new Date(value);
    }

    /**
     * Returns a date-time value.
     *
     * @param value the date-time, not null
     * @return the cell value
     */
    static CellValue dateTime(LocalDateTime value) {
        return new DateTime(value);
    }

    /**
     * Returns the empty cell value.
     *
     * @return the singleton blank value
     */
    static CellValue blank() {
        return Blank.INSTANCE;
    }
}
