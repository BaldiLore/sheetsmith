package cloud.baldilorenzo.sheetsmith.convert;

/**
 * Converts a field value into a {@link CellValue}, the value written to a cell.
 * <p>
 * Built-in converters cover text ({@code CharSequence}, {@code Character}), numbers ({@code Number} and the
 * numeric primitives), booleans, enums (written as the constant name), {@code LocalDate} and
 * {@code LocalDateTime}. A column of any other type needs a converter, otherwise its sheet class violates rule
 * V-10. Converters produce values, not formatting: display formats come from the styles and the application
 * defaults.
 *
 * <h2>Contract</h2>
 * <ul>
 *   <li>The value passed to {@link #convert(Object, ConversionContext)} is never null: a null value produces an
 *       empty cell that keeps its style, without calling the converter.</li>
 *   <li>The result must never be null. Return {@link CellValue#blank()} for an empty cell; a null result causes a
 *       {@link cloud.baldilorenzo.sheetsmith.SheetsmithGenerationException}.</li>
 *   <li>Any {@link RuntimeException} thrown by the converter is wrapped in a
 *       {@link cloud.baldilorenzo.sheetsmith.SheetsmithGenerationException} that names the sheet, the row and the
 *       field, with the original exception as its cause.</li>
 *   <li>A generator uses one instance of each converter for all its calls, possibly from several threads at the
 *       same time: implementations must be thread-safe, ideally stateless.</li>
 * </ul>
 *
 * <h2>Field converters and application converters</h2>
 * A <em>field converter</em> is declared on one column with
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#converter()} and applies to that column only. It
 * must handle a type assignable from the field type (rule V-11), and the
 * {@link CellConverterFactory} must be able to create it (rule V-12).
 * <p>
 * An <em>application converter</em> is registered for a type with
 * {@link cloud.baldilorenzo.sheetsmith.Sheetsmith.Builder#converter(Class, CellConverter)}, or declared as a bean
 * in a Spring Boot application. It applies to every column of that type or of its subtypes, in every sheet class.
 * Only one application converter can be registered per type, and it replaces the built-in converter for the same
 * type.
 *
 * <h2>Resolution</h2>
 * The converter of a column is chosen once, from the declared type of the field, primitive types being looked up
 * as their wrappers:
 * <ol>
 *   <li>the field converter, when declared;</li>
 *   <li>the application converter registered for the exact type;</li>
 *   <li>the application converter registered for the closest superclass;</li>
 *   <li>the application converter registered for an implemented interface, the closest first, by breadth-first
 *       distance;</li>
 *   <li>the built-in converters, looked up with the same rules 2 to 4.</li>
 * </ol>
 * An application converter for a supertype therefore wins over a built-in converter for the exact type. When two
 * interfaces at the same distance both have a converter, the resolution is ambiguous and violates rule V-10:
 * declare a field converter, or register a converter for the exact type.
 *
 * <h2>Instantiation</h2>
 * Application converters are registered as instances. Field converters are created by the
 * {@link CellConverterFactory} of the generator, once per converter class and generator:
 * <ul>
 *   <li>without Spring, the default factory requires a public class with a public no-argument constructor;</li>
 *   <li>with the Spring Boot auto-configuration, the converter is the bean of that class when exactly one exists,
 *       otherwise a new instance created with dependency injection, so that its constructor can receive beans
 *       without the converter being a bean.</li>
 * </ul>
 * In a Spring Boot application, a converter declared as a bean is also registered as an application converter
 * for its type. A converter meant for one column only should therefore not be a bean.
 *
 * <h2>Example</h2>
 * A field converter, and a sheet class that declares it:
 * <pre>
 * public class UuidConverter implements CellConverter&lt;UUID&gt; {
 *
 *     &#64;Override
 *     public CellValue convert(UUID value, ConversionContext context) {
 *         return CellValue.text(value.toString());
 *     }
 * }
 *
 * &#64;ExcelSheet
 * public record DocumentRow(
 *         &#64;ExcelColumn(header = "Id", order = 1, converter = UuidConverter.class) UUID id,
 *         &#64;ExcelColumn(header = "Title", order = 2) String title) {
 * }
 * </pre>
 *
 * @param <T> the type of the values this converter handles
 * @see CellValue
 * @see CellConverterFactory
 * @see cloud.baldilorenzo.sheetsmith.Sheetsmith.Builder#converter(Class, CellConverter)
 * @since 1.0.0
 */
@FunctionalInterface
public interface CellConverter<T> {

    /**
     * Converts a value into the value written to the cell.
     *
     * @param value   the value to convert, never null
     * @param context where the value is being written
     * @return the value to write, never null; {@link CellValue#blank()} for an empty cell
     */
    CellValue convert(T value, ConversionContext context);

    /**
     * Marker meaning "no field converter", the default of
     * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#converter()}.
     * <p>
     * Never instantiated, never invoked, and never used directly: leave
     * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#converter()} unset instead.
     *
     * @since 1.0.0
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
