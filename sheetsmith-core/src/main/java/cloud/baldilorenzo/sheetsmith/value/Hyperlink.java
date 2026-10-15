package cloud.baldilorenzo.sheetsmith.value;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

/**
 * A clickable link to a web page or an email address, written to a cell.
 * <p>
 * A column whose field is declared as {@code Hyperlink} produces cells that open the address when clicked: the
 * built-in converter of this type returns
 * {@link cloud.baldilorenzo.sheetsmith.convert.CellValue#link(Hyperlink) CellValue.link(value)}. A field or
 * application converter for {@code Hyperlink}, or an application converter for one of its supertypes, takes
 * precedence as for any other type, and the cell depends on what it returns: {@code CellValue.link(value)} keeps the
 * link, any other value produces a plain cell. The converter of any other type can produce a link too, by returning
 * {@code CellValue.link(...)}. A null field produces an empty cell without a link, as for any other type.
 *
 * <h2>Text shown</h2>
 * The cell shows the description, or the address when there is no description. A null, empty or blank description
 * counts as no description and is stored as null; any other description is kept as it is, without trimming. The
 * text shown is subject to the Excel limit of 32,767 characters for a cell: a longer text fails the generation with
 * a {@link cloud.baldilorenzo.sheetsmith.SheetsmithGenerationException}.
 *
 * <h2>Addresses</h2>
 * The address must be an absolute URI with one of the schemes {@code http}, {@code https} or {@code mailto}, in any
 * case. An {@code http} or {@code https} address needs a host, so {@code https:example.com} and
 * {@code http:///path} are rejected; a {@code mailto} address needs a recipient. The address is checked when the
 * link is created, not while the file is written: an invalid address throws an {@link IllegalArgumentException} from
 * the constructor and the factory methods. The address is kept as it is, without normalisation; the link written to
 * the file uses its ASCII form, so non-ASCII characters in the path or the query are percent-encoded.
 *
 * <h2>Appearance</h2>
 * A cell with a link, whichever converter produced it, is underlined and blue ({@code #0563C1}), as in Excel,
 * unless the styles that apply to the cell already decide: an underline set by the style cascade,
 * {@link cloud.baldilorenzo.sheetsmith.style.Underline#NONE} included, and a font colour set by a declared style or
 * by a preset, such as the contrasting colour of the coloured rows of the {@code DARK} preset, win over the defaults
 * of the link.
 *
 * <h2>Example</h2>
 * <pre>
 * &#64;ExcelSheet(title = "Suppliers")
 * public record SupplierRow(
 *         &#64;ExcelColumn(header = "Supplier", order = 10) String name,
 *         &#64;ExcelColumn(header = "Contact", order = 20) Hyperlink contact) {
 * }
 *
 * List&lt;SupplierRow&gt; rows = List.of(
 *         new SupplierRow("Northwind Paper",
 *                 Hyperlink.of("https://www.example.com/suppliers/northwind", "Catalogue")),
 *         new SupplierRow("Blue Harbour Tools", Hyperlink.of("mailto:orders@example.com")));
 * </pre>
 * The first row shows {@code Catalogue} and opens the web page; the second row shows the address and opens a new
 * email.
 *
 * @param address     the address, an absolute {@code http}, {@code https} or {@code mailto} URI; not null
 * @param description the text shown in the cell; null, empty or blank to show the address
 * @since 1.1.0
 */
public record Hyperlink(URI address, String description) {

    /** The allowed schemes, as listed in the error messages. */
    private static final String ALLOWED_SCHEMES = "http, https or mailto";

    /**
     * Creates a link.
     *
     * @param address     the address, an absolute {@code http}, {@code https} or {@code mailto} URI; not null
     * @param description the text shown in the cell; null, empty or blank to show the address
     * @throws NullPointerException     if the address is null
     * @throws IllegalArgumentException if the address is not absolute, has a scheme other than {@code http},
     *                                  {@code https} or {@code mailto}, is an {@code http} or {@code https} address
     *                                  without a host, or is a {@code mailto} address without a recipient
     */
    public Hyperlink {
        Objects.requireNonNull(address, "address");
        validate(address);
        if (description != null && description.isBlank()) {
            description = null;
        }
    }

    /**
     * Creates a link without description: the cell shows the address.
     *
     * @param address the address, an absolute {@code http}, {@code https} or {@code mailto} URI; not null
     * @return the link
     * @throws NullPointerException     if the address is null
     * @throws IllegalArgumentException if the address is not valid, as described in {@link Hyperlink}
     */
    public static Hyperlink of(URI address) {
        return new Hyperlink(address, null);
    }

    /**
     * Creates a link with a description.
     *
     * @param address     the address, an absolute {@code http}, {@code https} or {@code mailto} URI; not null
     * @param description the text shown in the cell; null, empty or blank to show the address
     * @return the link
     * @throws NullPointerException     if the address is null
     * @throws IllegalArgumentException if the address is not valid, as described in {@link Hyperlink}
     */
    public static Hyperlink of(URI address, String description) {
        return new Hyperlink(address, description);
    }

    /**
     * Creates a link without description from the text of its address: the cell shows the address.
     *
     * @param address the address, such as {@code https://www.example.com} or {@code mailto:info@example.com}; not
     *                null
     * @return the link
     * @throws NullPointerException     if the address is null
     * @throws IllegalArgumentException if the address is not a syntactically valid URI, or is not valid as
     *                                  described in {@link Hyperlink}
     */
    public static Hyperlink of(String address) {
        return new Hyperlink(parse(address), null);
    }

    /**
     * Creates a link with a description from the text of its address.
     *
     * @param address     the address, such as {@code https://www.example.com} or {@code mailto:info@example.com};
     *                    not null
     * @param description the text shown in the cell; null, empty or blank to show the address
     * @return the link
     * @throws NullPointerException     if the address is null
     * @throws IllegalArgumentException if the address is not a syntactically valid URI, or is not valid as
     *                                  described in {@link Hyperlink}
     */
    public static Hyperlink of(String address, String description) {
        return new Hyperlink(parse(address), description);
    }

    /**
     * Returns the text shown in the cell: the description, or the address when there is no description.
     *
     * @return the text shown
     */
    public String text() {
        return description != null ? description : address.toString();
    }

    private static URI parse(String address) {
        Objects.requireNonNull(address, "address");
        try {
            return URI.create(address);
        } catch (IllegalArgumentException e) {
            // the message of URI.create already ends with the address
            throw new IllegalArgumentException("invalid hyperlink address: " + e.getMessage(), e);
        }
    }

    private static void validate(URI address) {
        if (!address.isAbsolute()) {
            throw new IllegalArgumentException("hyperlink address " + address + " is not absolute: it needs the "
                    + "scheme " + ALLOWED_SCHEMES);
        }
        switch (address.getScheme().toLowerCase(Locale.ROOT)) {
            case "http":
            case "https":
                if (address.isOpaque() || address.getHost() == null) {
                    throw new IllegalArgumentException("hyperlink address " + address + " has no host");
                }
                break;
            case "mailto":
                if (address.getRawSchemeSpecificPart().isEmpty()) {
                    throw new IllegalArgumentException("hyperlink address " + address + " has no recipient");
                }
                break;
            default:
                throw new IllegalArgumentException("hyperlink address " + address + " has the scheme "
                        + address.getScheme() + ": the allowed schemes are " + ALLOWED_SCHEMES);
        }
    }
}
