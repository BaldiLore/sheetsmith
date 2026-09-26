package cloud.baldilorenzo.sheetsmith;

import java.util.Objects;
import java.util.Optional;

/**
 * Thrown when an error occurs while writing the data of a sheet.
 * <p>
 * A generation error usually means unexpected data at runtime. Its causes are:
 * <ul>
 *   <li>a null element in a data list;</li>
 *   <li>a getter, an accessor or a converter that throws a {@link RuntimeException}, kept as the cause of this
 *       exception;</li>
 *   <li>a converter that returns null instead of
 *       {@link cloud.baldilorenzo.sheetsmith.convert.CellValue#blank()};</li>
 *   <li>a text longer than 32,767 characters, the Excel limit for a cell;</li>
 *   <li>more rows than Excel allows in a sheet: 1,048,576, title and header included.</li>
 * </ul>
 * The exception tells where the problem is: {@link #sheetName()}, {@link #rowIndex()} and {@link #fieldName()}. The
 * message names them too, for example {@code converter failed: ... (sheet 'Invoice', row 12, field 'amount')}.
 *
 * @see SheetsmithException
 * @since 1.0.0
 */
public final class SheetsmithGenerationException extends SheetsmithException {

    private static final long serialVersionUID = 1L;

    /** The name of the sheet being written. */
    private final String sheetName;

    /** The 1-based data row, or 0 when the error is not specific to a row. */
    private final int rowIndex;

    /** The field involved, or null when the error is not specific to a field. */
    private final String fieldName;

    /**
     * Creates a generation exception. The message is completed with the sheet name and, when available, the row
     * and the field.
     *
     * @param message   the description of the error, not null
     * @param sheetName the name of the sheet being written, not null
     * @param rowIndex  the 1-based data row, or 0 when the error is not specific to a row
     * @param fieldName the field involved, or null when the error is not specific to a field
     * @param cause     the original exception, or null
     * @throws IllegalArgumentException if {@code rowIndex} is negative
     * @throws NullPointerException     if {@code message} or {@code sheetName} is null
     */
    public SheetsmithGenerationException(String message, String sheetName, int rowIndex, String fieldName,
                                         Throwable cause) {
        super(describe(message, sheetName, rowIndex, fieldName), cause);
        if (rowIndex < 0) {
            throw new IllegalArgumentException("rowIndex must not be negative: " + rowIndex);
        }
        this.sheetName = sheetName;
        this.rowIndex = rowIndex;
        this.fieldName = fieldName;
    }

    /**
     * Returns the name of the sheet being written, as given in its {@link SheetData}.
     *
     * @return the sheet name
     */
    public String sheetName() {
        return sheetName;
    }

    /**
     * Returns the data row being written.
     * <p>
     * Data rows are counted from 1, in the order of the data list, so the index points directly at the element
     * that failed: row 1 is the first element. Title and header rows are not counted.
     *
     * @return the 1-based data row, or 0 when the error is not specific to a row, such as too many rows
     */
    public int rowIndex() {
        return rowIndex;
    }

    /**
     * Returns the name of the field involved, as declared in the sheet class.
     *
     * @return the field name, or empty when the error is not specific to a field, such as a null element
     */
    public Optional<String> fieldName() {
        return Optional.ofNullable(fieldName);
    }

    private static String describe(String message, String sheetName, int rowIndex, String fieldName) {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(sheetName, "sheetName");
        StringBuilder text = new StringBuilder(message).append(" (sheet '").append(sheetName).append('\'');
        if (rowIndex > 0) {
            text.append(", row ").append(rowIndex);
        }
        if (fieldName != null) {
            text.append(", field '").append(fieldName).append('\'');
        }
        return text.append(')').toString();
    }
}
