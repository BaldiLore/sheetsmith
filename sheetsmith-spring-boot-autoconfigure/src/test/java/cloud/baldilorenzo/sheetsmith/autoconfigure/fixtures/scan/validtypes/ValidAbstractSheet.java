package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.validtypes;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;

/** Valid top-level abstract sheet class. */
@ExcelSheet
public abstract class ValidAbstractSheet {

    @ExcelColumn(header = "Name", order = 1)
    private String name;
}
