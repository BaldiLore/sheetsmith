package cloud.baldilorenzo.sheetsmith.fixtures;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyleSheet;
import cloud.baldilorenzo.sheetsmith.style.Toggle;

@ExcelStyleSheet
@ExcelStyle(name = "header", bold = Toggle.TRUE)
@ExcelStyle(name = "money", dataFormat = "#,##0.00")
public class RepeatedStyles {
}
