package cloud.baldilorenzo.sheetsmith.fixtures;

import cloud.baldilorenzo.sheetsmith.annotation.BodyStyles;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheetTemplate;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyleSheet;
import cloud.baldilorenzo.sheetsmith.annotation.HeaderStyles;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import cloud.baldilorenzo.sheetsmith.style.Toggle;

/** Invalid sheet templates and sheet classes that misuse templates, each named after the rule it violates. */
public final class InvalidTemplates {

    private InvalidTemplates() {
    }

    public static final class NotATemplate {
        private NotATemplate() {
        }
    }

    @ExcelSheet(template = NotATemplate.class)
    public record V21NotATemplate(@ExcelColumn(header = "A", order = 1) String a) {
    }

    @ExcelSheet(template = TemplateSheets.FullTemplate.class, preset = TablePreset.DARK)
    public record V22PresetWithTemplate(@ExcelColumn(header = "A", order = 1) String a) {
    }

    @ExcelSheetTemplate
    @ExcelSheet
    public record V23TemplateAndSheet(@ExcelColumn(header = "A", order = 1) String a) {
    }

    @ExcelSheetTemplate
    @ExcelStyleSheet
    public static final class V23TemplateAndStyleSheet {
        private V23TemplateAndStyleSheet() {
        }
    }

    @ExcelSheetTemplate
    @ExcelStyle(name = "header", bold = Toggle.TRUE)
    public static final class V24StyleOnTemplate {
        private V24StyleOnTemplate() {
        }
    }

    @ExcelSheetTemplate
    public static final class V25ColumnOnTemplate {
        @ExcelColumn(header = "A", order = 1)
        private String a;
    }

    public static class ColumnHolder {
        @ExcelColumn(header = "A", order = 1)
        private String inherited;
    }

    @ExcelSheetTemplate
    public static final class V25InheritedColumnOnTemplate extends ColumnHolder {
    }

    @ExcelSheetTemplate(accentColor = "blue", outerBorderColor = "#12345")
    public static final class V13InvalidColors {
        private V13InvalidColors() {
        }
    }

    public static final class NotAStyleSheet {
        private NotAStyleSheet() {
        }
    }

    @ExcelSheetTemplate(styleSheets = NotAStyleSheet.class)
    public static final class V09NotAStyleSheet {
        private V09NotAStyleSheet() {
        }
    }

    @ExcelStyleSheet
    @ExcelStyle(name = "shared")
    public static final class FirstStyleSheet {
        private FirstStyleSheet() {
        }
    }

    @ExcelStyleSheet
    @ExcelStyle(name = "shared")
    public static final class SecondStyleSheet {
        private SecondStyleSheet() {
        }
    }

    @ExcelSheetTemplate(styleSheets = {FirstStyleSheet.class, SecondStyleSheet.class})
    public static final class V08StyleSheetConflict {
        private V08StyleSheetConflict() {
        }
    }

    /** References a style it does not have from the title style and every slot. */
    @ExcelSheetTemplate(
            titleStyle = "missing",
            header = @HeaderStyles(base = "missing", firstColumn = "missing", lastColumn = "missing"),
            body = @BodyStyles(base = "missing", even = "missing", odd = "missing", firstRow = "missing",
                    lastRow = "missing", firstColumn = "missing", lastColumn = "missing"))
    public static final class V06UnknownStyles {
        private V06UnknownStyles() {
        }
    }

    /** Defines the style that the template misses: the template is still invalid on its own. */
    @ExcelSheet(title = "Report", template = V06UnknownStyles.class)
    @ExcelStyle(name = "missing", bold = Toggle.TRUE)
    public record DefinesStyleMissingFromTemplate(@ExcelColumn(header = "A", order = 1) String a) {
    }

    @ExcelSheet(title = "Report", template = V06UnknownStyles.class)
    public record UsesTemplateWithUnknownStyles(@ExcelColumn(header = "A", order = 1) String a) {
    }

    @ExcelSheet(template = V13InvalidColors.class)
    public record UsesTemplateWithInvalidColors(@ExcelColumn(header = "A", order = 1) String a) {
    }

    @ExcelSheet(template = V08StyleSheetConflict.class, styleSheets = FirstStyleSheet.class)
    public record UsesTemplateWithStyleSheetConflict(@ExcelColumn(header = "A", order = 1) String a) {
    }

    /** Invalid on its own, with an error of the template. */
    @ExcelSheet(template = V24StyleOnTemplate.class, header = @HeaderStyles(base = "unknown"))
    public record OwnAndTemplateErrors(@ExcelColumn(header = " ", order = 1) String a) {
    }
}
