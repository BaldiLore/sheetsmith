package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalidinterface;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;

/** Interface annotated with {@code @ExcelSheet}: it has no column, so it fails with V-02. */
@ExcelSheet
public interface InterfaceSheet {
}
