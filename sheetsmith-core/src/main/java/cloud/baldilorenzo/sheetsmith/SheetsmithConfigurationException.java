package cloud.baldilorenzo.sheetsmith;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Thrown when the input or the annotations of a class are invalid. Reports every error found, not only the
 * first one.
 * <p>
 * The message lists the errors one per line, for example
 * {@code [V-06] com.example.Invoice.amount: style 'money' not found (styles.base)}.
 */
public final class SheetsmithConfigurationException extends SheetsmithException {

    private static final long serialVersionUID = 1L;

    private final transient List<ConfigurationError> errors;

    /**
     * Creates an exception reporting the given errors.
     *
     * @param errors the errors, not null and not empty
     */
    public SheetsmithConfigurationException(List<ConfigurationError> errors) {
        super(describe(errors), null);
        this.errors = List.copyOf(errors);
    }

    /**
     * Returns the errors, in the order they were found.
     *
     * @return the errors, never empty
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
