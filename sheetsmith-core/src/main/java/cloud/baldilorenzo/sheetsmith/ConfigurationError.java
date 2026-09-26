package cloud.baldilorenzo.sheetsmith;

import java.io.Serializable;
import java.util.Objects;

/**
 * A single configuration error, reported by {@link SheetsmithConfigurationException}.
 * <p>
 * Each error names the violated validation rule, the class and the element involved, and describes the problem.
 * The rules are listed in {@link SheetsmithConfigurationException}.
 *
 * @param code    the id of the violated validation rule, from {@code V-01} to {@code V-20}
 * @param type    the class the error refers to: the sheet class, or the style sheet that declares the faulty style;
 *                null for errors on the input of {@link Sheetsmith#generate(java.util.List)}, rules V-18 to V-20
 * @param element the element involved, such as a field name, {@code @ExcelSheet} or
 *                {@code @ExcelStyle(header)}; empty when the error concerns the class or the input as a whole
 * @param message the description of the error
 * @see SheetsmithConfigurationException
 * @since 1.0.0
 * @serial exclude
 */
public record ConfigurationError(String code, Class<?> type, String element, String message)
        implements Serializable {

    /**
     * Creates a configuration error.
     *
     * @param code    the id of the violated validation rule, not null
     * @param type    the class the error refers to, or null
     * @param element the element involved, not null, possibly empty
     * @param message the description of the error, not null
     * @throws NullPointerException if {@code code}, {@code element} or {@code message} is null
     */
    public ConfigurationError {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(message, "message");
    }
}
