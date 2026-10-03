package cloud.baldilorenzo.sheetsmith.internal.write;

import cloud.baldilorenzo.sheetsmith.DocumentProperties;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties;

import java.util.Objects;

/**
 * Creates the workbook that a generation writes into.
 */
@FunctionalInterface
public interface WorkbookSupplier {

    /** Creates an in-memory {@link XSSFWorkbook}, with the document properties that Apache POI sets. */
    WorkbookSupplier XSSF = XSSFWorkbook::new;

    /**
     * Creates a new, empty workbook.
     *
     * @return the workbook
     */
    Workbook create();

    /**
     * Returns a supplier of in-memory {@link XSSFWorkbook}s whose author and application are the given ones,
     * replacing the values that Apache POI sets. An empty value leaves the property out of the file.
     *
     * @param properties the document properties, not null
     * @return the supplier
     */
    static WorkbookSupplier xssf(DocumentProperties properties) {
        Objects.requireNonNull(properties, "properties");
        return () -> {
            XSSFWorkbook workbook = new XSSFWorkbook();
            POIXMLProperties documentProperties = workbook.getProperties();
            // an empty creator is removed by POI itself
            documentProperties.getCoreProperties().setCreator(properties.author());
            CTProperties extended = documentProperties.getExtendedProperties().getUnderlyingProperties();
            if (properties.application().isEmpty()) {
                if (extended.isSetApplication()) {
                    extended.unsetApplication();
                }
            } else {
                extended.setApplication(properties.application());
            }
            return workbook;
        };
    }
}