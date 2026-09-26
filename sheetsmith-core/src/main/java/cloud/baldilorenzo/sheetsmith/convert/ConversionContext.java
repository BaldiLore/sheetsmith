package cloud.baldilorenzo.sheetsmith.convert;

/**
 * Describes where a value is being written when a {@link CellConverter} is called.
 * <p>
 * Converters can use it to adapt the value to the sheet, the row or the field, or to build error messages. It is
 * an interface so that methods can be added in later versions without breaking existing implementations.
 *
 * @see CellConverter#convert(Object, ConversionContext)
 * @since 1.0.0
 */
public interface ConversionContext {

    /**
     * Returns the name of the sheet being written, as given in its
     * {@link cloud.baldilorenzo.sheetsmith.SheetData}.
     *
     * @return the sheet name
     */
    String sheetName();

    /**
     * Returns the index of the data row being written, starting from 1 and following the order of the data list:
     * the first element of the list is row 1. Title and header rows are not counted.
     *
     * @return the 1-based data row index
     */
    int rowIndex();

    /**
     * Returns the name of the field whose value is converted, as declared in the sheet class.
     *
     * @return the field name
     */
    String fieldName();

    /**
     * Returns the sheet class, the class annotated with {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet}
     * whose instance is being written.
     *
     * @return the sheet class
     */
    Class<?> sourceType();

    /**
     * Returns the declared type of the field whose value is converted. For a primitive field, this is the primitive
     * type, even though the value is passed boxed.
     *
     * @return the declared field type
     */
    Class<?> valueType();
}
