package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle;
import cloud.baldilorenzo.sheetsmith.internal.style.ColorUtils;
import cloud.baldilorenzo.sheetsmith.internal.style.StyleAttributes;
import cloud.baldilorenzo.sheetsmith.style.Toggle;

import java.util.Objects;

/**
 * A named style, with its attributes.
 *
 * @param name       the style name
 * @param attributes the style attributes
 */
public record StyleDefinition(String name, StyleAttributes attributes) {

    /**
     * Creates a style definition.
     *
     * @param name       the style name, not null
     * @param attributes the style attributes, not null
     */
    public StyleDefinition {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(attributes, "attributes");
    }

    /**
     * Reads a style declaration. Sentinels and {@code INHERIT} become unset attributes; all-sides borders and
     * border colours are expanded to the sides that do not set their own value; hexadecimal colours are normalised
     * to upper case. The declaration is not validated.
     *
     * @param style the declaration
     * @return the style definition
     */
    public static StyleDefinition from(ExcelStyle style) {
        StyleAttributes attributes = StyleAttributes.builder()
                .align(orNull(style.align()))
                .verticalAlign(orNull(style.verticalAlign()))
                .wrapText(toBoolean(style.wrapText()))
                .shrinkToFit(toBoolean(style.shrinkToFit()))
                .rotation(orNull(style.rotation()))
                .indent(orNull(style.indent()))
                .borderTop(orNull(side(style.borderTop(), style.border())))
                .borderBottom(orNull(side(style.borderBottom(), style.border())))
                .borderLeft(orNull(side(style.borderLeft(), style.border())))
                .borderRight(orNull(side(style.borderRight(), style.border())))
                .borderTopColor(color(side(style.borderTopColor(), style.borderColor())))
                .borderBottomColor(color(side(style.borderBottomColor(), style.borderColor())))
                .borderLeftColor(color(side(style.borderLeftColor(), style.borderColor())))
                .borderRightColor(color(side(style.borderRightColor(), style.borderColor())))
                .fillColor(color(style.fillColor()))
                .fillBackgroundColor(color(style.fillBackgroundColor()))
                .fillPattern(orNull(style.fillPattern()))
                .fontName(orNull(style.fontName()))
                .fontSize(orNull(style.fontSize()))
                .bold(toBoolean(style.bold()))
                .italic(toBoolean(style.italic()))
                .strikeout(toBoolean(style.strikeout()))
                .underline(orNull(style.underline()))
                .fontColor(color(style.fontColor()))
                .script(orNull(style.script()))
                .dataFormat(orNull(style.dataFormat()))
                .locked(toBoolean(style.locked()))
                .hidden(toBoolean(style.hidden()))
                .quotePrefix(toBoolean(style.quotePrefix()))
                .build();
        return new StyleDefinition(style.name(), attributes);
    }

    private static <E extends Enum<E>> E side(E side, E all) {
        return "INHERIT".equals(side.name()) ? all : side;
    }

    private static String side(String side, String all) {
        return side.isEmpty() ? all : side;
    }

    private static <E extends Enum<E>> E orNull(E value) {
        return "INHERIT".equals(value.name()) ? null : value;
    }

    private static String orNull(String value) {
        return value.isEmpty() ? null : value;
    }

    private static String color(String value) {
        return ColorUtils.normalize(orNull(value));
    }

    private static Integer orNull(int value) {
        return value == ExcelStyle.UNSET ? null : value;
    }

    private static Boolean toBoolean(Toggle toggle) {
        switch (toggle) {
            case TRUE:
                return Boolean.TRUE;
            case FALSE:
                return Boolean.FALSE;
            default:
                return null;
        }
    }
}
