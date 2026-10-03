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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/** Exported classes used by the converter and writer tests. */
public final class WriterSheets {

    private WriterSheets() {
    }

    public enum Color { RED, GREEN }

    @ExcelSheet
    public record AllTypes(
            @ExcelColumn(header = "Text", order = 1) String text,
            @ExcelColumn(header = "Char", order = 2) char letter,
            @ExcelColumn(header = "Boxed char", order = 3) Character boxedLetter,
            @ExcelColumn(header = "Int", order = 4) int count,
            @ExcelColumn(header = "Long", order = 5) long big,
            @ExcelColumn(header = "Double", order = 6) double ratio,
            @ExcelColumn(header = "Decimal", order = 7) BigDecimal amount,
            @ExcelColumn(header = "Integer", order = 8) BigInteger huge,
            @ExcelColumn(header = "Flag", order = 9) boolean flag,
            @ExcelColumn(header = "Boxed flag", order = 10) Boolean boxedFlag,
            @ExcelColumn(header = "Enum", order = 11) Color color,
            @ExcelColumn(header = "Date", order = 12) LocalDate day,
            @ExcelColumn(header = "Date-time", order = 13) LocalDateTime moment,
            @ExcelColumn(header = "Builder", order = 14) StringBuilder builder,
            @ExcelColumn(header = "Formatted", order = 15, format = "0.0") double formatted) {
    }

    @ExcelSheet(body = @BodyStyles(base = "grid"))
    @ExcelStyle(name = "grid", border = Border.THIN)
    public record Nullable(
            @ExcelColumn(header = "Name", order = 1) String name,
            @ExcelColumn(header = "Count", order = 2) Integer count,
            @ExcelColumn(header = "Day", order = 3) LocalDate day) {
    }

    @ExcelSheet(title = "Report", titleStyle = "title", autoFilter = true,
            header = @HeaderStyles(base = "header"))
    @ExcelStyle(name = "title", bold = Toggle.TRUE, fontSize = 14)
    @ExcelStyle(name = "header", fillColor = "#DDDDDD")
    public record Layout(
            @ExcelColumn(header = "Code", order = 1, width = 20) String code,
            @ExcelColumn(header = "Description", order = 2) String description,
            @ExcelColumn(header = "Qty", order = 3) int quantity) {
    }

    @ExcelSheet(freezeHeader = false, autoSizeColumns = false)
    public record Plain(@ExcelColumn(header = "Value", order = 1) String value) {
    }

    public static class Upper implements CellConverter<String> {
        @Override
        public CellValue convert(String value, ConversionContext context) {
            return CellValue.text(value.toUpperCase());
        }
    }

    public static class Failing implements CellConverter<String> {
        @Override
        public CellValue convert(String value, ConversionContext context) {
            throw new IllegalStateException("cannot convert " + value);
        }
    }

    public static class ReturnsNull implements CellConverter<String> {
        @Override
        public CellValue convert(String value, ConversionContext context) {
            return null;
        }
    }

    /** Describes where it was called, to check the conversion context. */
    public static class Describe implements CellConverter<Object> {
        @Override
        public CellValue convert(Object value, ConversionContext context) {
            return CellValue.text(context.sheetName() + "|" + context.rowIndex() + "|" + context.fieldName() + "|"
                    + context.sourceType().getSimpleName() + "|" + context.valueType().getSimpleName());
        }
    }

    @ExcelSheet
    public record FieldConverters(
            @ExcelColumn(header = "Upper", order = 1, converter = Upper.class) String upper,
            @ExcelColumn(header = "Context", order = 2, converter = Describe.class) int described) {
    }

    @ExcelSheet
    public record FailingConverter(@ExcelColumn(header = "A", order = 1, converter = Failing.class) String a) {
    }

    @ExcelSheet
    public record NullConverter(@ExcelColumn(header = "A", order = 1, converter = ReturnsNull.class) String a) {
    }

    @ExcelSheet
    public static class FailingGetter {
        @ExcelColumn(header = "A", order = 1)
        private String a;

        public String getA() {
            throw new IllegalStateException("getter failed");
        }
    }

    // ---- binding ----

    /** Handles any type: its type argument cannot be determined from the class declaration. */
    public static class ToText<T> implements CellConverter<T> {
        @Override
        public CellValue convert(T value, ConversionContext context) {
            return CellValue.text(String.valueOf(value));
        }
    }

    public static class IntegerConverter implements CellConverter<Integer> {
        @Override
        public CellValue convert(Integer value, ConversionContext context) {
            return CellValue.number(value);
        }
    }

    public static class NoDefaultConstructor implements CellConverter<String> {
        public NoDefaultConstructor(String prefix) {
        }

        @Override
        public CellValue convert(String value, ConversionContext context) {
            return CellValue.text(value);
        }
    }

    public interface Labelled {
    }

    public interface Coded {
    }

    public static class Tag implements Labelled, Coded {
        @Override
        public String toString() {
            return "tag";
        }
    }

    @ExcelSheet
    public record V10Unsupported(@ExcelColumn(header = "When", order = 1) Date when) {
    }

    @ExcelSheet
    public record V10Ambiguous(@ExcelColumn(header = "Tag", order = 1) Tag tag) {
    }

    @ExcelSheet
    public record V11Incompatible(
            @ExcelColumn(header = "A", order = 1, converter = IntegerConverter.class) String a) {
    }

    @ExcelSheet
    public record V12Uncreatable(
            @ExcelColumn(header = "A", order = 1, converter = NoDefaultConstructor.class) String a) {
    }

    @ExcelSheet
    public record SharedFieldConverter(
            @ExcelColumn(header = "A", order = 1, converter = Upper.class) String a,
            @ExcelColumn(header = "B", order = 2, converter = Upper.class) String b) {
    }

    @ExcelSheet
    public record ManyBindingErrors(
            @ExcelColumn(header = "A", order = 1) Date a,
            @ExcelColumn(header = "B", order = 2, converter = IntegerConverter.class) String b,
            @ExcelColumn(header = "C", order = 3, converter = NoDefaultConstructor.class) String c) {
    }
}
