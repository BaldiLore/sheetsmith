package cloud.baldilorenzo.sheetsmith;

/**
 * Base class of the exceptions thrown by sheetsmith.
 */
public abstract sealed class SheetsmithException extends RuntimeException
        permits SheetsmithConfigurationException, SheetsmithGenerationException {

    private static final long serialVersionUID = 1L;

    SheetsmithException(String message, Throwable cause) {
        super(message, cause);
    }
}
