package cloud.baldilorenzo.sheetsmith;

import java.util.Objects;

/**
 * Properties of the generated documents: the author and the application recorded in every xlsx file.
 * <p>
 * Excel shows the author in File, Info, and the operating system shows both among the properties of the file. Set
 * them with {@link Sheetsmith.Builder#documentProperties(DocumentProperties)}. In Spring Boot applications they are
 * bound from the {@code sheetsmith.document.*} properties. When nothing is configured, {@link #standard()} applies
 * and both are {@code sheetsmith}.
 * <p>
 * An empty value leaves the property out of the file, so that it appears blank. Values are written as they are.
 *
 * <pre>{@code
 * Sheetsmith sheetsmith = Sheetsmith.builder()
 *         .documentProperties(new DocumentProperties("Example Ltd", "Billing"))
 *         .build();
 * }</pre>
 *
 * @param author      the author of the documents, not null; empty to leave it out
 * @param application the application that created the documents, not null; empty to leave it out
 * @see Sheetsmith.Builder#documentProperties(DocumentProperties)
 * @since 1.0.0
 */
public record DocumentProperties(String author, String application) {

    private static final DocumentProperties STANDARD = new DocumentProperties("sheetsmith", "sheetsmith");

    /**
     * Creates document properties.
     *
     * @param author      the author, not null; empty to leave it out
     * @param application the application, not null; empty to leave it out
     * @throws NullPointerException if an argument is null
     */
    public DocumentProperties {
        Objects.requireNonNull(author, "author");
        Objects.requireNonNull(application, "application");
    }

    /**
     * Returns the standard document properties: author and application both {@code sheetsmith}.
     *
     * @return the standard document properties
     */
    public static DocumentProperties standard() {
        return STANDARD;
    }
}