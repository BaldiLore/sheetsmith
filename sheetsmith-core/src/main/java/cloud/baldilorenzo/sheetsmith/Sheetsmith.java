package cloud.baldilorenzo.sheetsmith;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellConverterFactory;
import cloud.baldilorenzo.sheetsmith.internal.DefaultSheetsmith;

import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Generates Excel files ({@code .xlsx}) from lists of objects of annotated sheet classes.
 * <p>
 * A workbook is described by an ordered list of {@link SheetData}, one per sheet: the sheet name, the sheet class
 * and the objects to write, one per data row. The structure and the look of each sheet come from the annotations
 * of its sheet class, starting with {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet}; the content comes
 * from the objects.
 * <p>
 * Instances are immutable and thread-safe. Create one once, with {@link #builder()} or through the Spring Boot
 * auto-configuration, and share it: the metadata of each sheet class and the converter bound to each of its
 * columns are computed on first use and cached per class, so later calls reuse them. Every generation builds its
 * own workbook, so concurrent calls do not interfere.
 * <p>
 * The file is either returned as a {@code byte[]}, with {@link #generate(List)}, or written to a stream supplied by
 * the caller, with {@link #generate(List, OutputStream)}. The two produce the same content. The whole workbook is
 * built in memory before it is written, so memory grows with the number of cells; the stream method avoids holding
 * a second copy of the file as a {@code byte[]}, and is preferable for large files. The known limits of very large
 * exports are described in {@link cloud.baldilorenzo.sheetsmith.convert.CellValue}.
 *
 * <pre>{@code
 * // Created once, for example in a static field or as a singleton, and reused
 * Sheetsmith sheetsmith = Sheetsmith.builder().build();
 *
 * byte[] file = sheetsmith.generate(List.of(SheetData.of("Customers", CustomerRow.class, customers)));
 * Files.write(Path.of("customers.xlsx"), file);
 * }</pre>
 *
 * @see SheetData
 * @see SheetsmithDefaults
 * @see cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet
 * @since 1.0.0
 */
public interface Sheetsmith {

    /**
     * Generates an xlsx file containing the given sheets.
     * <p>
     * Sheets appear in the workbook in list order. Each sheet can use a different sheet class, and one class can
     * be used by several sheets. An empty data list is valid: its sheet contains the title, if any, and the header
     * only.
     * <p>
     * The input and every sheet class are validated before anything is written: the sheet list must not be empty
     * (rule V-18), sheet names must be valid (V-19) and unique ignoring case (V-20), and every sheet class must
     * satisfy rules V-01 to V-17. The errors of the input and of all the classes are reported together in one
     * {@link SheetsmithConfigurationException}, and no workbook is written. A class that fails validation is not
     * cached and is rejected at every call until it is fixed.
     * <p>
     * Problems in the data are found while writing and reported as a {@link SheetsmithGenerationException} naming
     * the sheet, the 1-based data row and, where relevant, the field: a null element in a data list, a getter or
     * converter that throws, a converter that returns null, a text longer than 32,767 characters, or more rows
     * than Excel allows in a sheet (1,048,576, title and header included).
     *
     * @param sheets the sheets, in workbook order; not null and without null elements
     * @return the content of the xlsx file
     * @throws SheetsmithConfigurationException if the input violates rules V-18 to V-20 or a sheet class violates
     *                                          any of rules V-01 to V-17; every error found is listed
     * @throws SheetsmithGenerationException    if an element or a value of the data cannot be written
     * @throws UncheckedIOException             if serialising the workbook fails. It is an infrastructure error, not
     *                                          a configuration or data error, so it is not wrapped in a
     *                                          {@link SheetsmithException}; with this method, which writes to
     *                                          memory, it is practically unreachable
     * @throws NullPointerException             if {@code sheets} is null or contains a null element
     * @see #generate(List, OutputStream)
     * @see SheetsmithConfigurationException
     * @see cloud.baldilorenzo.sheetsmith.convert.CellValue
     */
    byte[] generate(List<SheetData<?>> sheets);

    /**
     * Generates an xlsx file containing the given sheets and writes it to a stream.
     * <p>
     * The content of the file is the same as the one returned by {@link #generate(List)} for the same input, and
     * the same rules apply to the sheets, the validation and the data. The stream belongs to the caller: it is
     * flushed after the file is written and never closed.
     * <p>
     * The whole workbook is built before serialisation starts, so nothing is written to the stream when a
     * configuration error or a generation error occurs. Partial content can be left in the stream only when an I/O
     * failure occurs during serialisation.
     *
     * <pre>{@code
     * try (OutputStream out = Files.newOutputStream(Path.of("customers.xlsx"))) {
     *     sheetsmith.generate(List.of(SheetData.of("Customers", CustomerRow.class, customers)), out);
     * }
     * }</pre>
     *
     * @param sheets the sheets, in workbook order; not null and without null elements
     * @param out    the stream that receives the file, not null; flushed, never closed
     * @throws SheetsmithConfigurationException if the input violates rules V-18 to V-20 or a sheet class violates
     *                                          any of rules V-01 to V-17; every error found is listed, and nothing
     *                                          is written to the stream
     * @throws SheetsmithGenerationException    if an element or a value of the data cannot be written; nothing is
     *                                          written to the stream
     * @throws UncheckedIOException             if serialising the workbook fails, including a failure of the stream
     *                                          itself, with the original {@link java.io.IOException} as cause;
     *                                          partial content may have been written. It is an infrastructure
     *                                          error, not a configuration or data error, so it is not wrapped in a
     *                                          {@link SheetsmithException}
     * @throws NullPointerException             if {@code sheets} or {@code out} is null, or {@code sheets} contains
     *                                          a null element
     * @see #generate(List)
     * @see SheetsmithConfigurationException
     */
    void generate(List<SheetData<?>> sheets, OutputStream out);

    /**
     * Validates a sheet class without generating anything.
     * <p>
     * Runs every check that {@link #generate(List)} runs on a class: the annotations (rules V-01 to V-09 and V-13
     * to V-17) and the binding of a converter to each column (V-10 to V-12), using the application converters and
     * the converter factory of this instance. All the errors of the class are reported together.
     * <p>
     * Intended for startup validation and unit tests, so that a mistake in a sheet class fails the build or the
     * startup instead of the first generation. Because converter binding depends on the application converters,
     * validate with an instance configured like the one that generates the files.
     *
     * @param type the sheet class, not null
     * @throws SheetsmithConfigurationException listing every error of the class
     * @throws NullPointerException             if {@code type} is null
     * @see SheetsmithConfigurationException
     */
    void validate(Class<?> type);

    /**
     * Returns a new builder with default settings.
     * <p>
     * Unless configured otherwise, the built instance has no application converters, so only the built-in
     * converters apply; creates field converters through their public no-argument constructor; and uses
     * {@link SheetsmithDefaults#standard()}.
     *
     * @return a new builder
     */
    static Builder builder() {
        return new DefaultSheetsmith.Builder();
    }

    /**
     * Configures and builds {@link Sheetsmith} instances.
     * <p>
     * Obtain a builder with {@link Sheetsmith#builder()}. Every configuration method returns this builder, so calls
     * can be chained.
     *
     * <pre>{@code
     * Sheetsmith sheetsmith = Sheetsmith.builder()
     *         .defaults(new SheetsmithDefaults("dd/mm/yyyy", "dd/mm/yyyy hh:mm", "", TablePreset.LIGHT, "#1F4E79"))
     *         .converter(Money.class, new MoneyConverter())
     *         .build();
     * }</pre>
     *
     * @since 1.0.0
     */
    interface Builder {

        /**
         * Registers an application converter for a type.
         * <p>
         * The converter applies to every column whose declared type is {@code type} or one of its subtypes, in
         * every sheet class, unless the column declares a field converter or an application converter is
         * registered for a closer supertype of the column type. Primitive types are registered as their
         * wrappers, so a converter registered for {@code double.class} or {@code Double.class} applies to both
         * {@code double} and {@code Double} fields. A converter registered for a type that has a built-in
         * converter, such as {@code Boolean}, {@code LocalDate} or {@code Enum}, replaces the built-in behaviour.
         * <p>
         * The instance built calls the converter from several threads when it generates concurrently, so the
         * converter must be thread-safe.
         *
         * @param type      the type, not null
         * @param converter the converter, not null
         * @param <T>       the type
         * @return this builder
         * @throws IllegalArgumentException if a converter is already registered for the type, a primitive type and
         *                                  its wrapper counting as the same type
         * @throws NullPointerException     if an argument is null
         * @see CellConverter
         */
        <T> Builder converter(Class<T> type, CellConverter<? super T> converter);

        /**
         * Sets the factory that creates field converters, the converters declared with
         * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#converter()}.
         * <p>
         * Use it to create field converters through a dependency injection container, so that they can receive
         * dependencies. The instance built calls the factory once per converter class, the first time a sheet
         * class declaring it is validated or generated, and reuses the result; a converter that the factory cannot
         * create violates rule V-12. Without this call, field converters are created through their public
         * no-argument constructor. The Spring Boot auto-configuration sets a factory that takes converters from
         * the application context.
         *
         * @param factory the factory, not null
         * @return this builder
         * @throws NullPointerException if {@code factory} is null
         * @see CellConverterFactory
         */
        Builder converterFactory(CellConverterFactory factory);

        /**
         * Sets the application defaults: the default formats of dates, date-times and numbers, and the preset and
         * accent colour of sheet classes that do not choose their own. Without this call,
         * {@link SheetsmithDefaults#standard()} is used.
         *
         * @param defaults the defaults, not null
         * @return this builder
         * @throws NullPointerException if {@code defaults} is null
         * @see SheetsmithDefaults
         */
        Builder defaults(SheetsmithDefaults defaults);

        /**
         * Builds an instance with the current settings.
         *
         * @return a new immutable and thread-safe instance
         */
        Sheetsmith build();
    }
}
