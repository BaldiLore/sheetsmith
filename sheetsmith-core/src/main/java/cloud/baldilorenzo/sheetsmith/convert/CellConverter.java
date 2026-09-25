package cloud.baldilorenzo.sheetsmith.convert;

/**
 * Converts a field value into a {@link CellValue}.
 * <p>
 * A converter can be declared on a single field through
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#converter()} or registered for a type across
 * the application.
 *
 * @param <T> the type of the values this converter handles
 */
@FunctionalInterface
public interface CellConverter<T> {

    /**
     * Converts a non-null value.
     *
     * @param value   the value to convert, never null
     * @param context where the value is being written
     * @return the value to write, never null; {@link CellValue#blank()} for an empty cell
     */
    CellValue convert(T value, ConversionContext context);

    /**
     * Marker meaning "no field-level converter", used as the default of
     * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#converter()}. Never instantiated nor invoked.
     */
    final class None implements CellConverter<Object> {

        private None() {
        }

        /**
         * Always throws: this marker is never invoked.
         *
         * @param value   ignored
         * @param context ignored
         * @return never returns
         * @throws UnsupportedOperationException always
         */
        @Override
        public CellValue convert(Object value, ConversionContext context) {
            throw new UnsupportedOperationException("CellConverter.None is a marker and cannot convert values");
        }
    }
}
