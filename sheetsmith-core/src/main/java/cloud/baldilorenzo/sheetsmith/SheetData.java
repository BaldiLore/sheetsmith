package cloud.baldilorenzo.sheetsmith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * One sheet to generate: its name, the class of its objects and the objects, one per data row.
 *
 * @param name the sheet name: 1 to 31 characters, none of {@code \ / ? * [ ] :}, not starting or ending with
 *             {@code '}, unique in the workbook ignoring case
 * @param type the class of the objects, annotated with {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet}
 * @param rows the objects, in row order
 * @param <T>  the type of the objects
 */
public record SheetData<T>(String name, Class<T> type, List<T> rows) {

    /**
     * Creates sheet data. The rows are copied into an unmodifiable list; null elements are kept and reported with
     * their row index when the sheet is generated.
     *
     * @param name the sheet name, not null
     * @param type the class of the objects, not null
     * @param rows the objects, not null
     */
    public SheetData {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(rows, "rows");
        rows = Collections.unmodifiableList(new ArrayList<>(rows));
    }

    /**
     * Creates sheet data, accepting a list of a subtype of the declared class.
     *
     * @param name the sheet name, not null
     * @param type the class of the objects, not null
     * @param rows the objects, not null
     * @param <T>  the type of the objects
     * @return the sheet data
     */
    public static <T> SheetData<T> of(String name, Class<T> type, List<? extends T> rows) {
        Objects.requireNonNull(rows, "rows");
        return new SheetData<>(name, type, Collections.unmodifiableList(rows));
    }
}
