package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.valid;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;

@ExcelSheet
public record ValidSheet(@ExcelColumn(header = "Name", order = 1) String name) {
}
