package cloud.baldilorenzo.sheetsmith;

/**
 * Base class of the exceptions thrown by sheetsmith. All of them are unchecked.
 * <p>
 * There are two kinds, which call for different reactions:
 * <ul>
 *   <li>{@link SheetsmithConfigurationException}: a sheet class or the input is wrong, for example a missing
 *       annotation, an unknown style name or an invalid sheet name. It is a programming error, to be fixed in the
 *       code. It is thrown before anything is written, and can be caught early with
 *       {@link Sheetsmith#validate(Class)} in tests or at startup.</li>
 *   <li>{@link SheetsmithGenerationException}: something went wrong while writing the data, for example a null
 *       element or a failing converter. It usually means unexpected data at runtime, and it names the sheet, the
 *       row and the field involved.</li>
 * </ul>
 *
 * @since 1.0.0
 */
public abstract sealed class SheetsmithException extends RuntimeException
        permits SheetsmithConfigurationException, SheetsmithGenerationException {

    private static final long serialVersionUID = 1L;

    SheetsmithException(String message, Throwable cause) {
        super(message, cause);
    }
}
