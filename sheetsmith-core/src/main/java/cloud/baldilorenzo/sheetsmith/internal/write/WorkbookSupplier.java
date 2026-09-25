package cloud.baldilorenzo.sheetsmith.internal.write;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Creates the workbook that a generation writes into.
 */
@FunctionalInterface
public interface WorkbookSupplier {

    /** Creates an in-memory {@link XSSFWorkbook}. */
    WorkbookSupplier XSSF = XSSFWorkbook::new;

    /**
     * Creates a new, empty workbook.
     *
     * @return the workbook
     */
    Workbook create();
}
