package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalid;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;

import java.util.Date;

/** Not annotated with {@code @ExcelSheet}: ignored by the startup validation. */
public record NotExported(@ExcelColumn(header = "When", order = 1) Date when) {
}
