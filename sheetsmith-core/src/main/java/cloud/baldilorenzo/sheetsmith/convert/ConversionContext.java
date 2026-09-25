package cloud.baldilorenzo.sheetsmith.convert;

/**
 * Describes where a value is being written when a {@link CellConverter} is invoked.
 */
public interface ConversionContext {

    /**
     * Returns the name of the sheet being written.
     *
     * @return the sheet name
     */
    String sheetName();

    /**
     * Returns the index of the data row being written, starting from 1.
     *
     * @return the 1-based data row index
     */
    int rowIndex();

    /**
     * Returns the name of the field whose value is converted.
     *
     * @return the field name
     */
    String fieldName();

    /**
     * Returns the exported class, the one annotated with
     * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet}.
     *
     * @return the exported class
     */
    Class<?> sourceType();

    /**
     * Returns the declared type of the field whose value is converted.
     *
     * @return the declared field type
     */
    Class<?> valueType();
}
