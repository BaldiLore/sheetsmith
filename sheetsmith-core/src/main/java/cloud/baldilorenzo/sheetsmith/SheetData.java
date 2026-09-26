package cloud.baldilorenzo.sheetsmith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * One sheet of a workbook: its name, its sheet class and the objects to write, one per data row.
 * <p>
 * The sheet class is passed explicitly instead of being taken from the objects, because the element type of a list
 * is erased at runtime and an empty list has no element to inspect. It must be annotated with
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet}: its annotations define the columns and the look of
 * the sheet.
 * <p>
 * Sheet names are checked when the workbook is generated, together with the other configuration errors:
 * <ul>
 *   <li>a name is 1 to 31 characters long, contains none of {@code \ / ? * [ ] :}, and does not start or end with
 *       an apostrophe {@code '} (rule V-19);</li>
 *   <li>the names of one workbook are unique, ignoring case (rule V-20).</li>
 * </ul>
 * Invalid names are rejected, never truncated or sanitised, so names built from data must be cleaned by the
 * caller. Excel also reserves the name {@code History}, which is not rejected but must be avoided.
 * <p>
 * The rows are copied into an unmodifiable list, so later changes to the original list do not affect the sheet.
 * Null elements are kept: each one is reported, when the sheet is generated, as a
 * {@link SheetsmithGenerationException} with its 1-based row index.
 *
 * <pre>{@code
 * List<SheetData<?>> sheets = List.of(
 *         SheetData.of("Customers", CustomerRow.class, customers),
 *         SheetData.of("Invoice lines", InvoiceLine.class, lines));
 *
 * byte[] file = sheetsmith.generate(sheets);
 * }</pre>
 *
 * @param name the sheet name, subject to rules V-19 and V-20
 * @param type the sheet class, annotated with {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet}
 * @param rows the objects, in row order; the first element is data row 1
 * @param <T>  the type of the objects
 * @see Sheetsmith#generate(List)
 * @see SheetsmithConfigurationException
 * @since 1.0.0
 */
public record SheetData<T>(String name, Class<T> type, List<T> rows) {

    /**
     * Creates sheet data. The rows are copied into an unmodifiable list; null elements are kept and reported with
     * their row index when the sheet is generated.
     *
     * @param name the sheet name, not null
     * @param type the sheet class, not null
     * @param rows the objects, not null
     * @throws NullPointerException if an argument is null
     */
    public SheetData {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(rows, "rows");
        rows = Collections.unmodifiableList(new ArrayList<>(rows));
    }

    /**
     * Creates sheet data, accepting a list whose element type is a subtype of the sheet class.
     * <p>
     * For example, a {@code List<PremiumCustomerRow>} can be written with the sheet class {@code CustomerRow}
     * without copying or casting the list. The columns are those of the sheet class passed as {@code type}.
     *
     * @param name the sheet name, not null
     * @param type the sheet class, not null
     * @param rows the objects, not null
     * @param <T>  the type of the sheet class
     * @return the sheet data
     * @throws NullPointerException if an argument is null
     */
    public static <T> SheetData<T> of(String name, Class<T> type, List<? extends T> rows) {
        Objects.requireNonNull(rows, "rows");
        return new SheetData<>(name, type, Collections.unmodifiableList(rows));
    }
}
