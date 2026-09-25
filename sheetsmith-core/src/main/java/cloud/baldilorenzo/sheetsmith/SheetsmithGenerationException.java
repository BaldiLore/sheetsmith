package cloud.baldilorenzo.sheetsmith;

import java.util.Objects;
import java.util.Optional;

/**
 * Thrown when an error occurs while writing a sheet, for example a failing converter or a null element in the
 * data list. Names the sheet, the data row and, where relevant, the field.
 */
public final class SheetsmithGenerationException extends SheetsmithException {

    private static final long serialVersionUID = 1L;

    private final String sheetName;
    private final int rowIndex;
    private final String fieldName;

    /**
     * Creates a generation exception.
     *
     * @param message   the description of the error, not null
     * @param sheetName the name of the sheet being written, not null
     * @param rowIndex  the 1-based data row, or 0 when the error is not specific to a row
     * @param fieldName the field involved, or null
     * @param cause     the original exception, or null
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
     * Returns the name of the sheet being written.
     *
     * @return the sheet name
     */
    public String sheetName() {
        return sheetName;
    }

    /**
     * Returns the data row being written.
     *
     * @return the 1-based data row, or 0 when the error is not specific to a row
     */
    public int rowIndex() {
        return rowIndex;
    }

    /**
     * Returns the field involved.
     *
     * @return the field name, or empty when the error is not specific to a field
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
