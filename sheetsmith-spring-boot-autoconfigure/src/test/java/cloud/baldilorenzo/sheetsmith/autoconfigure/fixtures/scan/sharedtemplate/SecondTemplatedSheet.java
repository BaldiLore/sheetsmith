package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.sharedtemplate;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;

@ExcelSheet(template = SharedInvalidTemplate.class)
public record SecondTemplatedSheet(@ExcelColumn(header = "Name", order = 1) String name) {
}
