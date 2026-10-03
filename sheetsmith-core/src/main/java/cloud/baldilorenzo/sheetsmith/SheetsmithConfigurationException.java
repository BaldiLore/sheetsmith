package cloud.baldilorenzo.sheetsmith;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Thrown when the input or a sheet class is invalid. Reports every error found, not only the first one.
 * <p>
 * A configuration error is a programming error: the code must be fixed. It is detected before anything is written,
 * by {@link Sheetsmith#generate(java.util.List)}, by {@link Sheetsmith#validate(Class)} and by the startup
 * validation of the Spring Boot integration. A sheet class that fails validation is rejected at every call until
 * it is fixed.
 * <p>
 * {@link #errors()} returns the errors as {@link ConfigurationError} objects. The message lists them one per
 * line, each as {@code [code] class.element: message}, where the class and the element are omitted when absent:
 * <pre>
 * [V-06] com.example.export.InvoiceLine.amount: style 'money' not found (referenced by styles.base)
 * [V-15] com.example.export.InvoiceLine.&#64;ExcelSheet: titleStyle is set but title is empty
 * [V-18] the sheet list is empty
 * </pre>
 *
 * <h2 id="validation-rules">Validation rules</h2>
 * Rules V-01 to V-09 and V-13 to V-17 are checked on the annotations of a sheet class, rules V-10 to V-12 when a
 * converter is bound to each column, and rules V-18 to V-20 on the input of
 * {@link Sheetsmith#generate(java.util.List)}.
 * <table class="striped">
 *   <caption>Validation rules</caption>
 *   <thead>
 *     <tr><th scope="col">Code</th><th scope="col">Rule</th><th scope="col">How to fix</th></tr>
 *   </thead>
 *   <tbody>
 *     <tr><th scope="row">V-01</th><td>The class is annotated with
 *         {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet}.</td><td>Annotate the class.</td></tr>
 *     <tr><th scope="row">V-02</th><td>The class has at least one field annotated with
 *         {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn}.</td>
 *         <td>Annotate at least one field.</td></tr>
 *     <tr><th scope="row">V-03</th><td>{@code order} values are unique within the class, inherited columns
 *         included.</td><td>Give each column a different order.</td></tr>
 *     <tr><th scope="row">V-04</th><td>{@code header} is not blank.</td><td>Set a header text.</td></tr>
 *     <tr><th scope="row">V-05</th><td>{@code @ExcelColumn} is not placed on a static field.</td>
 *         <td>Move the annotation to an instance field.</td></tr>
 *     <tr><th scope="row">V-06</th><td>Every referenced style name exists: slots, {@code titleStyle},
 *         {@code headerStyle} and column slots.</td>
 *         <td>Declare the style, reference the style sheet that declares it, or fix the name.</td></tr>
 *     <tr><th scope="row">V-07</th><td>Style names are unique within one declaring class: among the styles
 *         declared on the sheet class, and among the styles declared on each style sheet.</td>
 *         <td>Rename or merge the duplicates.</td></tr>
 *     <tr><th scope="row">V-08</th><td>The style sheets referenced by one class do not define the same style
 *         name: a conflict between two style sheets, not within one.</td><td>Rename the style in one style sheet, or redefine it on the class.</td></tr>
 *     <tr><th scope="row">V-09</th><td>Every class listed in {@code styleSheets} is annotated with
 *         {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyleSheet}.</td>
 *         <td>Annotate the style sheet class.</td></tr>
 *     <tr><th scope="row">V-10</th><td>A converter can be resolved for the declared type of each column, and the
 *         resolution is not ambiguous.</td><td>Declare a field converter, or register an application converter
 *         for the type.</td></tr>
 *     <tr><th scope="row">V-11</th><td>A field converter handles a type assignable from the field type, when that
 *         type can be determined from its generic declaration.</td>
 *         <td>Use a converter of a compatible type.</td></tr>
 *     <tr><th scope="row">V-12</th><td>A field converter can be created by the
 *         {@link cloud.baldilorenzo.sheetsmith.convert.CellConverterFactory}.</td>
 *         <td>Make the converter a public class with a public no-argument constructor, or configure a factory
 *         that can create it, such as the one of the Spring Boot integration.</td></tr>
 *     <tr><th scope="row">V-13</th><td>Every colour attribute and {@code accentColor} is {@code #RRGGBB} or the
 *         name of an {@link org.apache.poi.ss.usermodel.IndexedColors} constant, or empty.</td>
 *         <td>Fix the colour.</td></tr>
 *     <tr><th scope="row">V-14</th><td>Numeric attributes are in range: {@code rotation} -90 to 90 or 255,
 *         {@code indent} 0 to 250, {@code fontSize} 1 to 409, {@code width} 1 to 255.</td>
 *         <td>Use a value in range.</td></tr>
 *     <tr><th scope="row">V-15</th><td>{@code titleStyle} is set only when {@code title} is set.</td>
 *         <td>Set a title, or remove the title style.</td></tr>
 *     <tr><th scope="row">V-16</th><td>Style names are not blank.</td><td>Name the style.</td></tr>
 *     <tr><th scope="row">V-17</th><td>Every column value is accessible.</td><td>Add a public getter, or open the
 *         package of the sheet class to sheetsmith in {@code module-info.java}.</td></tr>
 *     <tr><th scope="row">V-18</th><td>The sheet list is not empty.</td><td>Pass at least one sheet.</td></tr>
 *     <tr><th scope="row">V-19</th><td>Sheet names are 1 to 31 characters long, contain none of
 *         {@code \ / ? * [ ] :}, and do not start or end with {@code '}.</td>
 *         <td>Choose a valid name: sheetsmith never shortens or cleans names.</td></tr>
 *     <tr><th scope="row">V-20</th><td>Sheet names are unique within the workbook, ignoring case.</td>
 *         <td>Rename one of the sheets.</td></tr>
 *   </tbody>
 * </table>
 *
 * @see ConfigurationError
 * @see Sheetsmith#validate(Class)
 * @since 1.0.0
 */
public final class SheetsmithConfigurationException extends SheetsmithException {

    private static final long serialVersionUID = 1L;

    /** The errors: always an immutable, serialisable list created with {@code List.copyOf}. */
    @SuppressWarnings("serial")
    private final List<ConfigurationError> errors;

    /**
     * Creates an exception reporting the given errors. The message lists them one per line.
     *
     * @param errors the errors, not null and not empty
     * @throws IllegalArgumentException if {@code errors} is empty
     * @throws NullPointerException     if {@code errors} is null
     */
    public SheetsmithConfigurationException(List<ConfigurationError> errors) {
        super(describe(errors), null);
        this.errors = List.copyOf(errors);
    }

    /**
     * Returns the errors, in the order they were found.
     *
     * @return an unmodifiable list of the errors, never empty
     */
    public List<ConfigurationError> errors() {
        return errors;
    }

    private static String describe(List<ConfigurationError> errors) {
        Objects.requireNonNull(errors, "errors");
        if (errors.isEmpty()) {
            throw new IllegalArgumentException("errors must not be empty");
        }
        return errors.stream().map(SheetsmithConfigurationException::describe)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String describe(ConfigurationError error) {
        StringBuilder text = new StringBuilder("[").append(error.code()).append("] ");
        if (error.type() != null) {
            text.append(error.type().getName());
            if (!error.element().isEmpty()) {
                text.append('.');
            }
        }
        text.append(error.element());
        if (error.type() != null || !error.element().isEmpty()) {
            text.append(": ");
        }
        return text.append(error.message()).toString();
    }
}
