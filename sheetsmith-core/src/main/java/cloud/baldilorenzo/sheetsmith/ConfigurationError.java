package cloud.baldilorenzo.sheetsmith;

import java.util.Objects;

/**
 * A single configuration error, reported by {@link SheetsmithConfigurationException}.
 *
 * @param code    the id of the violated validation rule, for example {@code V-06}
 * @param type    the class the error refers to, or null for errors on the input
 * @param element the field, style or attribute involved, or empty
 * @param message the description of the error
 */
public record ConfigurationError(String code, Class<?> type, String element, String message) {

    /**
     * Creates a configuration error.
     *
     * @param code    the id of the violated validation rule, not null
     * @param type    the class the error refers to, or null
     * @param element the field, style or attribute involved, not null, possibly empty
     * @param message the description of the error, not null
     */
    public ConfigurationError {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(message, "message");
    }
}
