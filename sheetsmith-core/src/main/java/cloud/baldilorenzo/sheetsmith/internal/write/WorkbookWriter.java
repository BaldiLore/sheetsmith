package cloud.baldilorenzo.sheetsmith.internal.write;

import cloud.baldilorenzo.sheetsmith.SheetsmithDefaults;
import cloud.baldilorenzo.sheetsmith.SheetsmithGenerationException;
import cloud.baldilorenzo.sheetsmith.internal.style.StyleCache;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/**
 * Writes sheets, in list order, into a new workbook and serialises it. Immutable and thread-safe: every call uses
 * its own workbook and style cache.
 */
public final class WorkbookWriter {

    private final WorkbookSupplier workbooks;
    private final SheetWriter sheetWriter;

    /**
     * Creates a writer.
     *
     * @param workbooks creates the workbook of each generation
     * @param defaults  the default formats, preset and accent colour
     */
    public WorkbookWriter(WorkbookSupplier workbooks, SheetsmithDefaults defaults) {
        this(workbooks, new SheetWriter(defaults));
    }

    WorkbookWriter(WorkbookSupplier workbooks, SheetWriter sheetWriter) {
        this.workbooks = Objects.requireNonNull(workbooks, "workbooks");
        this.sheetWriter = Objects.requireNonNull(sheetWriter, "sheetWriter");
    }

    /**
     * Writes the sheets and serialises the workbook. The workbook is always closed; the stream is not.
     *
     * @param sheets the sheets, in order
     * @param out    receives the xlsx content
     * @throws SheetsmithGenerationException if a sheet cannot be written
     * @throws IOException                   if the stream cannot be written
     */
    public void write(List<WritableSheet> sheets, OutputStream out) throws IOException {
        Objects.requireNonNull(sheets, "sheets");
        Objects.requireNonNull(out, "out");
        try (Workbook workbook = workbooks.create()) {
            StyleCache styles = new StyleCache(workbook);
            for (WritableSheet sheet : sheets) {
                sheetWriter.write(workbook, styles, sheet);
            }
            workbook.write(out);
        }
    }
}
