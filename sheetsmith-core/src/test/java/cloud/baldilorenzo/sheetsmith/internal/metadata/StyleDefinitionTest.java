package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle;
import cloud.baldilorenzo.sheetsmith.internal.style.StyleAttributes;
import cloud.baldilorenzo.sheetsmith.style.Align;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.Fill;
import cloud.baldilorenzo.sheetsmith.style.Script;
import cloud.baldilorenzo.sheetsmith.style.Toggle;
import cloud.baldilorenzo.sheetsmith.style.Underline;
import cloud.baldilorenzo.sheetsmith.style.VerticalAlign;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StyleDefinitionTest {

    @ExcelStyle(name = "empty")
    @ExcelStyle(name = "sides", border = Border.THIN, borderTop = Border.THICK,
            borderColor = "#000000", borderLeftColor = "RED")
    @ExcelStyle(name = "full", align = Align.CENTER, verticalAlign = VerticalAlign.TOP, wrapText = Toggle.TRUE,
            shrinkToFit = Toggle.FALSE, rotation = -1, indent = 0, fillColor = "#FFFFFF",
            fillBackgroundColor = "BLACK", fillPattern = Fill.BRICKS, fontName = "Arial", fontSize = 11,
            bold = Toggle.TRUE, italic = Toggle.FALSE, strikeout = Toggle.TRUE, underline = Underline.DOUBLE,
            fontColor = "#FF0000", script = Script.SUPER, dataFormat = "0.0", locked = Toggle.FALSE,
            hidden = Toggle.TRUE, quotePrefix = Toggle.TRUE)
    @ExcelStyle(name = "lowerCase", borderColor = "#a1b2c3", fillColor = "#ffcc00",
            fillBackgroundColor = "#00ff00", fontColor = "#abcdef")
    private static final class Styles {
    }

    @Test
    void defaultsBecomeUnsetAttributes() {
        StyleDefinition definition = StyleDefinition.from(style("empty"));

        assertThat(definition.name()).isEqualTo("empty");
        assertThat(definition.attributes()).isEqualTo(StyleAttributes.EMPTY);
    }

    @Test
    void allSidesBordersAreExpandedAndSideSpecificValuesWin() {
        StyleAttributes attributes = StyleDefinition.from(style("sides")).attributes();

        assertThat(attributes).isEqualTo(StyleAttributes.builder()
                .borderTop(Border.THICK).borderBottom(Border.THIN).borderLeft(Border.THIN).borderRight(Border.THIN)
                .borderTopColor("#000000").borderBottomColor("#000000").borderLeftColor("RED")
                .borderRightColor("#000000")
                .build());
    }

    @Test
    void everyAttributeIsRead() {
        StyleAttributes attributes = StyleDefinition.from(style("full")).attributes();

        assertThat(attributes).isEqualTo(StyleAttributes.builder()
                .align(Align.CENTER).verticalAlign(VerticalAlign.TOP).wrapText(true).shrinkToFit(false)
                .rotation(-1).indent(0).fillColor("#FFFFFF").fillBackgroundColor("BLACK").fillPattern(Fill.BRICKS)
                .fontName("Arial").fontSize(11).bold(true).italic(false).strikeout(true)
                .underline(Underline.DOUBLE).fontColor("#FF0000").script(Script.SUPER).dataFormat("0.0")
                .locked(false).hidden(true).quotePrefix(true)
                .build());
    }

    @Test
    void hexadecimalColoursAreNormalisedToUpperCase() {
        StyleAttributes attributes = StyleDefinition.from(style("lowerCase")).attributes();

        assertThat(attributes).isEqualTo(StyleAttributes.builder()
                .borderColor("#A1B2C3").fillColor("#FFCC00").fillBackgroundColor("#00FF00").fontColor("#ABCDEF")
                .build());
    }

    private static ExcelStyle style(String name) {
        for (ExcelStyle style : Styles.class.getAnnotationsByType(ExcelStyle.class)) {
            if (style.name().equals(name)) {
                return style;
            }
        }
        throw new IllegalArgumentException(name);
    }
}
