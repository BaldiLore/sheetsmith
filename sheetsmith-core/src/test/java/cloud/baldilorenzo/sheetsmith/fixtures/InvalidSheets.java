package cloud.baldilorenzo.sheetsmith.fixtures;

import cloud.baldilorenzo.sheetsmith.annotation.BodyStyles;
import cloud.baldilorenzo.sheetsmith.annotation.ColumnStyles;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyleSheet;
import cloud.baldilorenzo.sheetsmith.annotation.HeaderStyles;
import cloud.baldilorenzo.sheetsmith.style.Toggle;

/** Invalid exported classes, each violating the validation rule named in its name. */
public final class InvalidSheets {

    private InvalidSheets() {
    }

    public static class V01MissingSheet {
        @ExcelColumn(header = "A", order = 1)
        private String a;
    }

    @ExcelSheet
    public static class V02NoColumns {
        private String a;
    }

    @ExcelSheet
    public static class V03DuplicateOrder {
        @ExcelColumn(header = "A", order = 1)
        private String a;

        @ExcelColumn(header = "B", order = 1)
        private String b;
    }

    @ExcelSheet
    public static class V04BlankHeader {
        @ExcelColumn(header = "  ", order = 1)
        private String a;
    }

    @ExcelSheet
    public static class V05StaticField {
        @ExcelColumn(header = "Constant", order = 1)
        static String constant = "x";

        @ExcelColumn(header = "A", order = 2)
        private String a;
    }

    @ExcelSheet(header = @HeaderStyles(base = "missing"))
    public static class V06UnknownStyle {
        @ExcelColumn(header = "A", order = 1, styles = @ColumnStyles(even = "money"))
        private String a;
    }

    @ExcelSheet
    @ExcelStyle(name = "money", dataFormat = "0.00")
    @ExcelStyle(name = "money", dataFormat = "0.000")
    public static class V07DuplicateStyle {
        @ExcelColumn(header = "A", order = 1)
        private String a;
    }

    @ExcelStyleSheet
    @ExcelStyle(name = "shared")
    public static class FirstStyleSheet {
    }

    @ExcelStyleSheet
    @ExcelStyle(name = "shared")
    public static class SecondStyleSheet {
    }

    @ExcelSheet(styleSheets = {FirstStyleSheet.class, SecondStyleSheet.class})
    public static class V08StyleSheetConflict {
        @ExcelColumn(header = "A", order = 1)
        private String a;
    }

    @ExcelStyle(name = "orphan")
    public static class NotAStyleSheet {
    }

    @ExcelSheet(styleSheets = NotAStyleSheet.class)
    public static class V09NotAStyleSheet {
        @ExcelColumn(header = "A", order = 1)
        private String a;
    }

    @ExcelSheet(accentColor = "blue", outerBorderColor = "#12345")
    @ExcelStyle(name = "bad", fontColor = "#GG0000", fillColor = "dark_blue")
    public static class V13InvalidColor {
        @ExcelColumn(header = "A", order = 1)
        private String a;
    }

    @ExcelSheet
    @ExcelStyle(name = "bad", rotation = 91, indent = 251, fontSize = 0)
    public static class V14OutOfRange {
        @ExcelColumn(header = "A", order = 1, width = 256)
        private String a;
    }

    @ExcelSheet(titleStyle = "title")
    @ExcelStyle(name = "title", bold = Toggle.TRUE)
    public static class V15TitleStyleWithoutTitle {
        @ExcelColumn(header = "A", order = 1)
        private String a;
    }

    @ExcelSheet
    @ExcelStyle(name = " ")
    public static class V16BlankStyleName {
        @ExcelColumn(header = "A", order = 1)
        private String a;
    }

    /** Violates several rules at once. */
    @ExcelSheet(accentColor = "nope", body = @BodyStyles(odd = "missing"))
    public static class ManyErrors {
        @ExcelColumn(header = "", order = 1)
        private String a;

        @ExcelColumn(header = "B", order = 1, width = 0)
        private String b;
    }
}
