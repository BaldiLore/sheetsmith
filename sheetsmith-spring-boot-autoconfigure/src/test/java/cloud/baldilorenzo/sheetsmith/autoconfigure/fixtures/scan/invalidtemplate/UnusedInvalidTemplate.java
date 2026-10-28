package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalidtemplate;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheetTemplate;

/** Invalid template that no sheet class references. */
@ExcelSheetTemplate(accentColor = "blue")
public final class UnusedInvalidTemplate {

    private UnusedInvalidTemplate() {
    }
}
