package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.metaannotated;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.annotationtype.ExportedSheet;

/**
 * Only meta-annotated with {@code @ExcelSheet}, through {@link ExportedSheet}. Validating it would fail with V-01,
 * but only types annotated directly are validated at startup.
 */
@ExportedSheet
public record MetaAnnotatedSheet(@ExcelColumn(header = "Name", order = 1) String name) {
}
