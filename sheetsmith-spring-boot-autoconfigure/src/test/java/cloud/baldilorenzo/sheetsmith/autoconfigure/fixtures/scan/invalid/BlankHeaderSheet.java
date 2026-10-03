package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalid;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;

@ExcelSheet
public record BlankHeaderSheet(@ExcelColumn(header = " ", order = 1) String name) {
}
