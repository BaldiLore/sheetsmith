package cloud.baldilorenzo.sheetsmith.convert;

/**
 * Creates the field converters declared with {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#converter()}.
 * <p>
 * A generator calls its factory once per converter class, the first time a sheet class that declares the converter
 * is validated or generated, and reuses the converter for every column and every later call. A factory that throws
 * a {@link RuntimeException} or returns null makes the declaring class violate rule V-12.
 * <p>
 * Without configuration, the factory creates converters through their public no-argument constructor, so the
 * converter class must be public. Set another factory with
 * {@link cloud.baldilorenzo.sheetsmith.Sheetsmith.Builder#converterFactory(CellConverterFactory)} to create
 * converters through a dependency injection container. Spring Boot applications do not need to: the
 * auto-configuration sets {@code cloud.baldilorenzo.sheetsmith.autoconfigure.SpringConverterFactory}, which takes
 * the converter from the application context when exactly one bean of its class exists, and otherwise creates it
 * with dependency injection.
 *
 * @see CellConverter
 * @since 1.0.0
 */
public interface CellConverterFactory {

    /**
     * Creates a converter of the given class.
     *
     * @param converterClass the converter class, as declared in
     *                       {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#converter()}
     * @param <C>            the converter type
     * @return the converter, never null
     * @throws RuntimeException if the converter cannot be created; reported as a violation of rule V-12
     */
    <C extends CellConverter<?>> C create(Class<C> converterClass);
}
