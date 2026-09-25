package cloud.baldilorenzo.sheetsmith.convert;

/**
 * Creates the field-level converters declared with
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#converter()}.
 * <p>
 * Each converter class is created once per generator instance. The default factory uses the public no-argument
 * constructor; the Spring integration takes converters from the application context.
 */
public interface CellConverterFactory {

    /**
     * Creates a converter.
     *
     * @param converterClass the converter class
     * @param <C>            the converter type
     * @return the converter, never null
     * @throws RuntimeException if the converter cannot be created
     */
    <C extends CellConverter<?>> C create(Class<C> converterClass);
}
