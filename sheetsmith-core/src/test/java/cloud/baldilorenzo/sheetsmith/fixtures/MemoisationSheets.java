package cloud.baldilorenzo.sheetsmith.fixtures;

import cloud.baldilorenzo.sheetsmith.annotation.BodyStyles;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle;
import cloud.baldilorenzo.sheetsmith.annotation.HeaderStyles;
import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;
import cloud.baldilorenzo.sheetsmith.convert.ConversionContext;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.Toggle;

import java.time.LocalDate;

/** Sheet classes for the memoisation of resolved styles. */
public final class MemoisationSheets {

    private MemoisationSheets() {
    }

    /** Writes dates as dates, numbers as numbers and anything else as text. */
    public static class Mixed implements CellConverter<Object> {
        @Override
        public CellValue convert(Object value, ConversionContext context) {
            if (value instanceof LocalDate date) {
                return CellValue.date(date);
            }
            if (value instanceof Number number) {
                return CellValue.number(number.doubleValue());
            }
            return CellValue.text(value.toString());
        }
    }

    /** Zebra rows, first and last row slots, a first column slot and a column whose value kind varies. */
    @ExcelSheet(header = @HeaderStyles(base = "header"),
            body = @BodyStyles(odd = "odd", even = "even", firstRow = "first", lastRow = "last",
                    firstColumn = "key"))
    @ExcelStyle(name = "header", bold = Toggle.TRUE)
    @ExcelStyle(name = "odd", fillColor = "#F2F2F2")
    @ExcelStyle(name = "even", fillColor = "#FFFFFF")
    @ExcelStyle(name = "first", bold = Toggle.TRUE)
    @ExcelStyle(name = "last", borderTop = Border.DOUBLE)
    @ExcelStyle(name = "key", italic = Toggle.TRUE)
    public record Zebra(
            @ExcelColumn(header = "Code", order = 1) String code,
            @ExcelColumn(header = "Quantity", order = 2) int quantity,
            @ExcelColumn(header = "Day", order = 3) LocalDate day,
            @ExcelColumn(header = "Value", order = 4, converter = Mixed.class) Object value) {
    }
}
