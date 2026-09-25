package cloud.baldilorenzo.sheetsmith.fixtures;

import cloud.baldilorenzo.sheetsmith.annotation.ColumnStyles;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle;
import cloud.baldilorenzo.sheetsmith.style.Align;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Exported classes used by the preset tests. */
public final class PresetSheets {

    private PresetSheets() {
    }

    @ExcelSheet
    public record InheritPreset(@ExcelColumn(header = "Name", order = 1) String name) {
    }

    @ExcelSheet(preset = TablePreset.NONE)
    public record NoPreset(@ExcelColumn(header = "Name", order = 1) String name) {
    }

    @ExcelSheet(preset = TablePreset.MEDIUM, accentColor = "#FFC000")
    public record MediumWithAccent(@ExcelColumn(header = "Name", order = 1) String name) {
    }

    @ExcelSheet(preset = TablePreset.LIGHT)
    public record LightWithDefaultAccent(@ExcelColumn(header = "Name", order = 1) String name) {
    }

    /** A realistic table used for the sample workbooks; takes preset and accent from the defaults. */
    @ExcelSheet(title = "Quarterly invoices", autoFilter = true)
    @ExcelStyle(name = "money", dataFormat = "#,##0.00", align = Align.RIGHT)
    @ExcelStyle(name = "center", align = Align.CENTER)
    public record Invoice(
            @ExcelColumn(header = "Number", order = 1, styles = @ColumnStyles(base = "center")) String number,
            @ExcelColumn(header = "Customer", order = 2) String customer,
            @ExcelColumn(header = "Issued", order = 3) LocalDate issued,
            @ExcelColumn(header = "Amount", order = 4, styles = @ColumnStyles(base = "money")) BigDecimal amount,
            @ExcelColumn(header = "Paid", order = 5) boolean paid) {
    }
}
