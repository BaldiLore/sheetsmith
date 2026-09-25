package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalid;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;

import java.util.Date;

@ExcelSheet
public record UnconvertibleSheet(@ExcelColumn(header = "When", order = 1) Date when) {
}
