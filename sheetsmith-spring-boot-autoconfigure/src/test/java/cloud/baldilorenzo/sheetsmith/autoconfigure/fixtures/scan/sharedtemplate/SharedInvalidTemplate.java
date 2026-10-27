package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.sharedtemplate;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheetTemplate;

/** Invalid template referenced by two sheet classes. */
@ExcelSheetTemplate(accentColor = "blue")
public final class SharedInvalidTemplate {

    private SharedInvalidTemplate() {
    }
}
