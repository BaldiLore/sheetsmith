package cloud.baldilorenzo.sheetsmith.fixtures;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle;
import cloud.baldilorenzo.sheetsmith.style.Toggle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Sheet classes for the automatic column sizing fallback and the title layout. */
public final class LayoutSheets {

    private LayoutSheets() {
    }

    @ExcelSheet
    public record DayColumn(@ExcelColumn(header = "Day", order = 1) LocalDate day) {
    }

    @ExcelSheet
    public record AmountColumn(
            @ExcelColumn(header = "Amt", order = 1, format = "#,##0.00") BigDecimal amount) {
    }

    @ExcelSheet
    public record MomentColumn(@ExcelColumn(header = "At", order = 1) LocalDateTime at) {
    }

    @ExcelSheet
    public record FlagColumn(@ExcelColumn(header = "Ok", order = 1) boolean ok) {
    }

    @ExcelSheet(title = "A very long report title that spans every column")
    public record TitledShortColumns(
            @ExcelColumn(header = "A", order = 1) String a,
            @ExcelColumn(header = "B", order = 2) String b) {
    }

    @ExcelSheet
    public record NotesColumn(@ExcelColumn(header = "Notes", order = 1) String notes) {
    }

    @ExcelSheet(title = "Summary", titleStyle = "title")
    @ExcelStyle(name = "title", bold = Toggle.TRUE, fontSize = 16)
    public record SingleColumnWithTitle(@ExcelColumn(header = "Value", order = 1) String value) {
    }
}
