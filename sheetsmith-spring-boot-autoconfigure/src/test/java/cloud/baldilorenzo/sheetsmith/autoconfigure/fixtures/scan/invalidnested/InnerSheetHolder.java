package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalidnested;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;

/** Not a sheet class: holds a non-static nested sheet class. */
public class InnerSheetHolder {

    /** Non-static nested sheet class with a blank header: fails with V-04. */
    @ExcelSheet
    public class InnerSheet {

        @ExcelColumn(header = " ", order = 1)
        private String name;
    }
}
