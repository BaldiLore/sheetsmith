package cloud.baldilorenzo.sheetsmith.fixtures;

import cloud.baldilorenzo.sheetsmith.annotation.BodyStyles;
import cloud.baldilorenzo.sheetsmith.annotation.ColumnStyles;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyleSheet;
import cloud.baldilorenzo.sheetsmith.annotation.HeaderStyles;
import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;
import cloud.baldilorenzo.sheetsmith.convert.ConversionContext;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import cloud.baldilorenzo.sheetsmith.style.Toggle;

import java.math.BigDecimal;

/** Valid exported classes used by the metadata tests. */
public final class ValidSheets {

    private ValidSheets() {
    }

    @ExcelSheet
    public static class Person {

        @ExcelColumn(header = "Age", order = 20)
        private final int age;

        @ExcelColumn(header = "Name", order = 10)
        private final String name;

        private final String notExported;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
            this.notExported = "hidden";
        }

        public String getName() {
            return name.toUpperCase();
        }

        public int getAge() {
            return age;
        }

        public String getNotExported() {
            return notExported;
        }
    }

    public static class Base {

        @ExcelColumn(header = "Id", order = 1)
        private final long id;

        public Base(long id) {
            this.id = id;
        }
    }

    @ExcelSheet
    public static class Employee extends Base {

        @ExcelColumn(header = "Role", order = 2)
        private final String role;

        public Employee(long id, String role) {
            super(id);
            this.role = role;
        }
    }

    @ExcelSheet
    public record Product(
            @ExcelColumn(header = "Price", order = 2, format = "#,##0.00") BigDecimal price,
            @ExcelColumn(header = "Code", order = 1, width = 12) String code,
            String notExported) {
    }

    @ExcelSheet
    public static class Flags {

        @ExcelColumn(header = "Active", order = 1)
        private final boolean active;

        @ExcelColumn(header = "Verified", order = 2)
        private final Boolean verified;

        @ExcelColumn(header = "Counter", order = 3)
        private final int counter;

        public Flags(boolean active, Boolean verified, int counter) {
            this.active = active;
            this.verified = verified;
            this.counter = counter;
        }

        public boolean isActive() {
            return active;
        }

        public Boolean getVerified() {
            return verified;
        }

        /** Not a getter: its return type is not assignable to the field type, so the field is read. */
        public String getCounter() {
            return "not used";
        }
    }

    @ExcelSheet
    static class PackagePrivate {

        @ExcelColumn(header = "Value", order = 1)
        private final String value;

        PackagePrivate(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static PackagePrivate packagePrivate(String value) {
        return new PackagePrivate(value);
    }

    public static Class<?> packagePrivateType() {
        return PackagePrivate.class;
    }

    @ExcelStyleSheet
    @ExcelStyle(name = "bold", bold = Toggle.TRUE)
    @ExcelStyle(name = "money", dataFormat = "#,##0.00")
    public static class SharedStyles {
    }

    @ExcelStyleSheet
    @ExcelStyle(name = "zebra", fillColor = "#F2F2F2")
    public static class ZebraStyles {
    }

    public static class UpperCaseConverter implements CellConverter<String> {
        @Override
        public CellValue convert(String value, ConversionContext context) {
            return CellValue.text(value.toUpperCase());
        }
    }

    @ExcelSheet(
            title = "Invoices",
            titleStyle = "title",
            preset = TablePreset.MEDIUM,
            accentColor = "#1F4E79",
            styleSheets = {SharedStyles.class, ZebraStyles.class},
            freezeHeader = false,
            autoFilter = true,
            autoSizeColumns = false,
            outerBorder = Border.MEDIUM,
            outerBorderColor = "DARK_BLUE",
            header = @HeaderStyles(base = "bold", firstColumn = "left", lastColumn = "right"),
            body = @BodyStyles(base = "base", even = "zebra", odd = "odd", firstRow = "first", lastRow = "last",
                    firstColumn = "left", lastColumn = "right"))
    @ExcelStyle(name = "title", fontSize = 16)
    @ExcelStyle(name = "money", dataFormat = "0.00", fontColor = "#00AA00")
    @ExcelStyle(name = "left", borderLeft = Border.THIN)
    @ExcelStyle(name = "right", borderRight = Border.THIN)
    @ExcelStyle(name = "base", fontName = "Arial")
    @ExcelStyle(name = "odd", italic = Toggle.TRUE)
    @ExcelStyle(name = "first", bold = Toggle.FALSE)
    @ExcelStyle(name = "last", borderBottom = Border.DOUBLE)
    public static class Invoice {

        @ExcelColumn(header = "Customer", order = 1, headerStyle = "left",
                converter = UpperCaseConverter.class,
                styles = @ColumnStyles(base = "base", even = "zebra", odd = "odd", firstRow = "first",
                        lastRow = "last"))
        private String customer;

        @ExcelColumn(header = "Amount", order = 2, styles = @ColumnStyles(base = "money"))
        private BigDecimal amount;
    }
}
