package cloud.baldilorenzo.sheetsmith.fixtures;

import cloud.baldilorenzo.sheetsmith.annotation.BodyStyles;
import cloud.baldilorenzo.sheetsmith.annotation.ColumnStyles;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheetTemplate;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyleSheet;
import cloud.baldilorenzo.sheetsmith.annotation.HeaderStyles;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import cloud.baldilorenzo.sheetsmith.style.Toggle;

import java.math.BigDecimal;

/** Valid sheet templates, their style sheets and the sheet classes that reference them. */
public final class TemplateSheets {

    private TemplateSheets() {
    }

    @ExcelStyleSheet
    @ExcelStyle(name = "title", fontSize = 16)
    @ExcelStyle(name = "header", bold = Toggle.TRUE)
    @ExcelStyle(name = "headerFirst", borderLeft = Border.THIN)
    @ExcelStyle(name = "headerLast", borderRight = Border.THIN)
    @ExcelStyle(name = "body", fontName = "Arial")
    @ExcelStyle(name = "even", fillColor = "#F2F2F2")
    @ExcelStyle(name = "odd", italic = Toggle.TRUE)
    @ExcelStyle(name = "first", bold = Toggle.FALSE)
    @ExcelStyle(name = "total", borderBottom = Border.DOUBLE)
    @ExcelStyle(name = "firstColumn", borderLeft = Border.DASHED)
    @ExcelStyle(name = "lastColumn", borderRight = Border.DASHED)
    @ExcelStyle(name = "money", dataFormat = "#,##0.00")
    @ExcelStyle(name = "shared", fontColor = "#111111", bold = Toggle.TRUE)
    public static final class TemplateStyles {
        private TemplateStyles() {
        }
    }

    @ExcelStyleSheet
    @ExcelStyle(name = "shared", fontColor = "#222222")
    public static final class SheetStyles {
        private SheetStyles() {
        }
    }

    /** Sets every attribute, with layout options opposite to the defaults. */
    @ExcelSheetTemplate(
            preset = TablePreset.MEDIUM,
            accentColor = "#1f4e79",
            styleSheets = TemplateStyles.class,
            titleStyle = "title",
            freezeHeader = Toggle.FALSE,
            autoFilter = Toggle.TRUE,
            autoSizeColumns = Toggle.FALSE,
            outerBorder = Border.THIN,
            outerBorderColor = "#a1b2c3",
            header = @HeaderStyles(base = "header", firstColumn = "headerFirst", lastColumn = "headerLast"),
            body = @BodyStyles(base = "body", even = "even", odd = "odd", firstRow = "first", lastRow = "total",
                    firstColumn = "firstColumn", lastColumn = "lastColumn"))
    public static final class FullTemplate {
        private FullTemplate() {
        }
    }

    /** Layout options equal to the defaults, set explicitly. */
    @ExcelSheetTemplate(freezeHeader = Toggle.TRUE, autoFilter = Toggle.FALSE, autoSizeColumns = Toggle.TRUE)
    public static final class DefaultOptionsTemplate {
        private DefaultOptionsTemplate() {
        }
    }

    /** Sets nothing: preset and accent colour come from the application defaults. */
    @ExcelSheetTemplate
    public static final class BlankTemplate {
        private BlankTemplate() {
        }
    }

    @ExcelSheetTemplate(styleSheets = TemplateStyles.class, header = @HeaderStyles(base = "shared"))
    public static final class SharedStyleTemplate {
        private SharedStyleTemplate() {
        }
    }

    @ExcelSheet(title = "Report", template = FullTemplate.class)
    public record InheritsEverything(
            @ExcelColumn(header = "Code", order = 1) String code,
            @ExcelColumn(header = "Amount", order = 2) BigDecimal amount) {
    }

    @ExcelSheet(template = FullTemplate.class)
    public record WithoutTitle(@ExcelColumn(header = "Code", order = 1) String code) {
    }

    @ExcelSheet(
            title = "Own",
            template = FullTemplate.class,
            titleStyle = "own",
            accentColor = "#00aa00",
            freezeHeader = Toggle.TRUE,
            autoFilter = Toggle.FALSE,
            autoSizeColumns = Toggle.TRUE,
            outerBorder = Border.MEDIUM,
            outerBorderColor = "DARK_BLUE",
            header = @HeaderStyles(base = "own", firstColumn = "own", lastColumn = "own"),
            body = @BodyStyles(base = "own", even = "own", odd = "own", firstRow = "own", lastRow = "own",
                    firstColumn = "own", lastColumn = "own"))
    @ExcelStyle(name = "own", fontColor = "#00AA00")
    public record OverridesEverything(@ExcelColumn(header = "Code", order = 1) String code) {
    }

    @ExcelSheet(template = DefaultOptionsTemplate.class, freezeHeader = Toggle.FALSE, autoFilter = Toggle.TRUE,
            autoSizeColumns = Toggle.FALSE)
    public record OverridesDefaultOptions(@ExcelColumn(header = "Code", order = 1) String code) {
    }

    @ExcelSheet(title = "Purchases", template = FullTemplate.class, accentColor = "#7F6000")
    public record OwnAccent(@ExcelColumn(header = "Supplier", order = 1) String supplier) {
    }

    @ExcelSheet(template = BlankTemplate.class)
    public record UsesBlankTemplate(@ExcelColumn(header = "Code", order = 1) String code) {
    }

    @ExcelSheet
    public record WithoutTemplate(@ExcelColumn(header = "Code", order = 1) String code) {
    }

    @ExcelSheet(template = FullTemplate.class, header = @HeaderStyles(firstColumn = "own"),
            body = @BodyStyles(odd = "own"))
    @ExcelStyle(name = "own", fontColor = "#00AA00")
    public record OverridesOneSlot(@ExcelColumn(header = "Code", order = 1) String code) {
    }

    @ExcelSheet(template = SharedStyleTemplate.class)
    public record SharedFromTemplate(@ExcelColumn(header = "Code", order = 1) String code) {
    }

    @ExcelSheet(template = SharedStyleTemplate.class, styleSheets = SheetStyles.class)
    public record SharedFromSheetStyleSheet(@ExcelColumn(header = "Code", order = 1) String code) {
    }

    @ExcelSheet(template = SharedStyleTemplate.class, styleSheets = SheetStyles.class)
    @ExcelStyle(name = "shared", fontColor = "#333333")
    public record SharedFromClass(@ExcelColumn(header = "Code", order = 1) String code) {
    }

    /** Redefines on the class the style that a header slot of the template references. */
    @ExcelSheet(template = FullTemplate.class)
    @ExcelStyle(name = "header", fontColor = "#FF0000")
    public record RedefinesTemplateStyle(@ExcelColumn(header = "Code", order = 1) String code) {
    }

    @ExcelSheet(template = FullTemplate.class, styleSheets = TemplateStyles.class)
    public record SameStyleSheetAsTemplate(@ExcelColumn(header = "Code", order = 1) String code) {
    }

    @ExcelSheet(template = FullTemplate.class)
    public record ColumnUsesTemplateStyle(
            @ExcelColumn(header = "Amount", order = 1, headerStyle = "shared",
                    styles = @ColumnStyles(base = "money")) BigDecimal amount) {
    }
}
