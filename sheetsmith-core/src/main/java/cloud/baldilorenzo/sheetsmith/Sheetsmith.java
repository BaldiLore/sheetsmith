package cloud.baldilorenzo.sheetsmith;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellConverterFactory;
import cloud.baldilorenzo.sheetsmith.internal.DefaultSheetsmith;

import java.util.List;

/**
 * Generates Excel files from lists of annotated objects.
 * <p>
 * Instances are immutable and thread-safe. Create one with {@link #builder()}, or let the Spring Boot
 * auto-configuration provide it.
 */
public interface Sheetsmith {

    /**
     * Generates an xlsx file containing the given sheets, in list order.
     *
     * @param sheets the sheets, not empty
     * @return the content of the xlsx file
     * @throws SheetsmithConfigurationException if the input or an annotated class is invalid
     * @throws SheetsmithGenerationException    if an error occurs while writing
     */
    byte[] generate(List<SheetData<?>> sheets);

    /**
     * Validates the annotations of a class without generating anything, including the converter bound to each
     * column.
     *
     * @param type the class to validate
     * @throws SheetsmithConfigurationException listing every error of the class
     */
    void validate(Class<?> type);

    /**
     * Returns a builder with default settings: no application converters, converters declared on fields created
     * through their public no-argument constructor, and {@link SheetsmithDefaults#standard()}.
     *
     * @return a new builder
     */
    static Builder builder() {
        return new DefaultSheetsmith.Builder();
    }

    /**
     * Builds {@link Sheetsmith} instances.
     */
    interface Builder {

        /**
         * Registers an application converter for a type and, unless a closer converter exists, its subtypes.
         * A converter registered for a type that has a built-in converter replaces it.
         *
         * @param type      the type, not null; a primitive type is registered as its wrapper
         * @param converter the converter, not null
         * @param <T>       the type
         * @return this builder
         * @throws IllegalArgumentException if a converter is already registered for the type
         */
        <T> Builder converter(Class<T> type, CellConverter<? super T> converter);

        /**
         * Sets the factory that creates the converters declared on fields.
         *
         * @param factory the factory, not null
         * @return this builder
         */
        Builder converterFactory(CellConverterFactory factory);

        /**
         * Sets the application-wide defaults.
         *
         * @param defaults the defaults, not null
         * @return this builder
         */
        Builder defaults(SheetsmithDefaults defaults);

        /**
         * Builds an instance with the current settings.
         *
         * @return the instance
         */
        Sheetsmith build();
    }
}
