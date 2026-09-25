package cloud.baldilorenzo.sheetsmith.internal.write;

import cloud.baldilorenzo.sheetsmith.internal.convert.SheetBinding;

import java.util.List;
import java.util.Objects;

/**
 * A sheet ready to be written: its name, its bound class and its data rows.
 *
 * @param name    the sheet name, already validated
 * @param binding the bound sheet class
 * @param rows    the data rows; may contain null elements, which are reported while writing
 */
public record WritableSheet(String name, SheetBinding binding, List<?> rows) {

    /**
     * Creates a writable sheet.
     *
     * @param name    the sheet name, not null
     * @param binding the bound sheet class, not null
     * @param rows    the data rows, not null
     */
    public WritableSheet {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(binding, "binding");
        Objects.requireNonNull(rows, "rows");
    }
}
