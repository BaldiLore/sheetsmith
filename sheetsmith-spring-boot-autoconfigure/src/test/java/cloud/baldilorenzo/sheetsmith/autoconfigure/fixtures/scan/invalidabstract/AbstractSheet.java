package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalidabstract;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;

/** Abstract sheet class with a blank header: found by the startup validation, fails with V-04. */
@ExcelSheet
public abstract class AbstractSheet {

    @ExcelColumn(header = " ", order = 1)
    private String name;
}
