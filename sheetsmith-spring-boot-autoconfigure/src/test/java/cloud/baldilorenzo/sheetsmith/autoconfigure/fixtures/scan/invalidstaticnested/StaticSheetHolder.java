package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalidstaticnested;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;

/** Not a sheet class: holds a static nested sheet class. */
public class StaticSheetHolder {

    /** Static nested sheet class with a blank header: fails with V-04. */
    @ExcelSheet
    public static class StaticSheet {

        @ExcelColumn(header = " ", order = 1)
        private String name;
    }
}
