package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.style.Align;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.Fill;
import cloud.baldilorenzo.sheetsmith.style.Script;
import cloud.baldilorenzo.sheetsmith.style.Underline;
import cloud.baldilorenzo.sheetsmith.style.VerticalAlign;

/**
 * Immutable set of style attributes. Every component is nullable: null means unset. Borders and border colours
 * are always per side.
 * <p>
 * Enum components never hold {@code INHERIT}: an inherited attribute is represented by null.
 *
 * @param align               horizontal alignment
 * @param verticalAlign       vertical alignment
 * @param wrapText            text wrapping
 * @param shrinkToFit         shrink to fit
 * @param rotation            text rotation
 * @param indent              indentation level
 * @param borderTop           top border line
 * @param borderBottom        bottom border line
 * @param borderLeft          left border line
 * @param borderRight         right border line
 * @param borderTopColor      top border colour
 * @param borderBottomColor   bottom border colour
 * @param borderLeftColor     left border colour
 * @param borderRightColor    right border colour
 * @param fillColor           foreground fill colour
 * @param fillBackgroundColor background fill colour
 * @param fillPattern         fill pattern
 * @param fontName            font name
 * @param fontSize            font size in points
 * @param bold                bold font
 * @param italic              italic font
 * @param strikeout           strikeout font
 * @param underline           underline style
 * @param fontColor           font colour
 * @param script              superscript or subscript
 * @param dataFormat          Excel data format
 * @param locked              locked cell
 * @param hidden              hidden formula
 * @param quotePrefix         quote prefix
 */
public record StyleAttributes(
        Align align,
        VerticalAlign verticalAlign,
        Boolean wrapText,
        Boolean shrinkToFit,
        Integer rotation,
        Integer indent,
        Border borderTop,
        Border borderBottom,
        Border borderLeft,
        Border borderRight,
        String borderTopColor,
        String borderBottomColor,
        String borderLeftColor,
        String borderRightColor,
        String fillColor,
        String fillBackgroundColor,
        Fill fillPattern,
        String fontName,
        Integer fontSize,
        Boolean bold,
        Boolean italic,
        Boolean strikeout,
        Underline underline,
        String fontColor,
        Script script,
        String dataFormat,
        Boolean locked,
        Boolean hidden,
        Boolean quotePrefix) {

    /** Attributes with every component unset. */
    public static final StyleAttributes EMPTY = builder().build();

    /**
     * Creates a set of attributes.
     *
     * @throws IllegalArgumentException if an enum component is {@code INHERIT}
     */
    public StyleAttributes {
        rejectInherit(align, Align.INHERIT);
        rejectInherit(verticalAlign, VerticalAlign.INHERIT);
        rejectInherit(borderTop, Border.INHERIT);
        rejectInherit(borderBottom, Border.INHERIT);
        rejectInherit(borderLeft, Border.INHERIT);
        rejectInherit(borderRight, Border.INHERIT);
        rejectInherit(fillPattern, Fill.INHERIT);
        rejectInherit(underline, Underline.INHERIT);
        rejectInherit(script, Script.INHERIT);
    }

    /**
     * Returns a new builder with every component unset.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a builder initialised with the components of these attributes.
     *
     * @return the builder
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Returns the attributes obtained by laying {@code over} on top of these: every component set in {@code over}
     * replaces the corresponding one, every component unset in {@code over} keeps the value of these attributes.
     *
     * @param over the attributes laid on top
     * @return the merged attributes
     */
    public StyleAttributes merge(StyleAttributes over) {
        if (over == EMPTY || over.equals(EMPTY)) {
            return this;
        }
        if (this.equals(EMPTY)) {
            return over;
        }
        return new StyleAttributes(
                pick(over.align, align),
                pick(over.verticalAlign, verticalAlign),
                pick(over.wrapText, wrapText),
                pick(over.shrinkToFit, shrinkToFit),
                pick(over.rotation, rotation),
                pick(over.indent, indent),
                pick(over.borderTop, borderTop),
                pick(over.borderBottom, borderBottom),
                pick(over.borderLeft, borderLeft),
                pick(over.borderRight, borderRight),
                pick(over.borderTopColor, borderTopColor),
                pick(over.borderBottomColor, borderBottomColor),
                pick(over.borderLeftColor, borderLeftColor),
                pick(over.borderRightColor, borderRightColor),
                pick(over.fillColor, fillColor),
                pick(over.fillBackgroundColor, fillBackgroundColor),
                pick(over.fillPattern, fillPattern),
                pick(over.fontName, fontName),
                pick(over.fontSize, fontSize),
                pick(over.bold, bold),
                pick(over.italic, italic),
                pick(over.strikeout, strikeout),
                pick(over.underline, underline),
                pick(over.fontColor, fontColor),
                pick(over.script, script),
                pick(over.dataFormat, dataFormat),
                pick(over.locked, locked),
                pick(over.hidden, hidden),
                pick(over.quotePrefix, quotePrefix));
    }

    private static <T> T pick(T over, T under) {
        return over != null ? over : under;
    }

    private static <E extends Enum<E>> void rejectInherit(E value, E inherit) {
        if (value == inherit) {
            throw new IllegalArgumentException(
                    inherit.getDeclaringClass().getSimpleName() + ".INHERIT is represented by null");
        }
    }

    /**
     * Mutable builder of {@link StyleAttributes}. Every setter accepts null to unset the component.
     */
    public static final class Builder {

        private Align align;
        private VerticalAlign verticalAlign;
        private Boolean wrapText;
        private Boolean shrinkToFit;
        private Integer rotation;
        private Integer indent;
        private Border borderTop;
        private Border borderBottom;
        private Border borderLeft;
        private Border borderRight;
        private String borderTopColor;
        private String borderBottomColor;
        private String borderLeftColor;
        private String borderRightColor;
        private String fillColor;
        private String fillBackgroundColor;
        private Fill fillPattern;
        private String fontName;
        private Integer fontSize;
        private Boolean bold;
        private Boolean italic;
        private Boolean strikeout;
        private Underline underline;
        private String fontColor;
        private Script script;
        private String dataFormat;
        private Boolean locked;
        private Boolean hidden;
        private Boolean quotePrefix;

        private Builder() {
        }

        private Builder(StyleAttributes source) {
            align = source.align;
            verticalAlign = source.verticalAlign;
            wrapText = source.wrapText;
            shrinkToFit = source.shrinkToFit;
            rotation = source.rotation;
            indent = source.indent;
            borderTop = source.borderTop;
            borderBottom = source.borderBottom;
            borderLeft = source.borderLeft;
            borderRight = source.borderRight;
            borderTopColor = source.borderTopColor;
            borderBottomColor = source.borderBottomColor;
            borderLeftColor = source.borderLeftColor;
            borderRightColor = source.borderRightColor;
            fillColor = source.fillColor;
            fillBackgroundColor = source.fillBackgroundColor;
            fillPattern = source.fillPattern;
            fontName = source.fontName;
            fontSize = source.fontSize;
            bold = source.bold;
            italic = source.italic;
            strikeout = source.strikeout;
            underline = source.underline;
            fontColor = source.fontColor;
            script = source.script;
            dataFormat = source.dataFormat;
            locked = source.locked;
            hidden = source.hidden;
            quotePrefix = source.quotePrefix;
        }

        /**
         * Sets the horizontal alignment.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder align(Align value) {
            align = value;
            return this;
        }

        /**
         * Sets the vertical alignment.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder verticalAlign(VerticalAlign value) {
            verticalAlign = value;
            return this;
        }

        /**
         * Sets text wrapping.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder wrapText(Boolean value) {
            wrapText = value;
            return this;
        }

        /**
         * Sets shrink to fit.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder shrinkToFit(Boolean value) {
            shrinkToFit = value;
            return this;
        }

        /**
         * Sets the text rotation.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder rotation(Integer value) {
            rotation = value;
            return this;
        }

        /**
         * Sets the indentation level.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder indent(Integer value) {
            indent = value;
            return this;
        }

        /**
         * Sets the line of all four borders.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder border(Border value) {
            borderTop = value;
            borderBottom = value;
            borderLeft = value;
            borderRight = value;
            return this;
        }

        /**
         * Sets the colour of all four borders.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder borderColor(String value) {
            borderTopColor = value;
            borderBottomColor = value;
            borderLeftColor = value;
            borderRightColor = value;
            return this;
        }

        /**
         * Sets the top border line.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder borderTop(Border value) {
            borderTop = value;
            return this;
        }

        /**
         * Sets the bottom border line.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder borderBottom(Border value) {
            borderBottom = value;
            return this;
        }

        /**
         * Sets the left border line.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder borderLeft(Border value) {
            borderLeft = value;
            return this;
        }

        /**
         * Sets the right border line.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder borderRight(Border value) {
            borderRight = value;
            return this;
        }

        /**
         * Sets the top border colour.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder borderTopColor(String value) {
            borderTopColor = value;
            return this;
        }

        /**
         * Sets the bottom border colour.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder borderBottomColor(String value) {
            borderBottomColor = value;
            return this;
        }

        /**
         * Sets the left border colour.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder borderLeftColor(String value) {
            borderLeftColor = value;
            return this;
        }

        /**
         * Sets the right border colour.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder borderRightColor(String value) {
            borderRightColor = value;
            return this;
        }

        /**
         * Sets the foreground fill colour.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder fillColor(String value) {
            fillColor = value;
            return this;
        }

        /**
         * Sets the background fill colour.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder fillBackgroundColor(String value) {
            fillBackgroundColor = value;
            return this;
        }

        /**
         * Sets the fill pattern.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder fillPattern(Fill value) {
            fillPattern = value;
            return this;
        }

        /**
         * Sets the font name.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder fontName(String value) {
            fontName = value;
            return this;
        }

        /**
         * Sets the font size.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder fontSize(Integer value) {
            fontSize = value;
            return this;
        }

        /**
         * Sets bold.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder bold(Boolean value) {
            bold = value;
            return this;
        }

        /**
         * Sets italic.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder italic(Boolean value) {
            italic = value;
            return this;
        }

        /**
         * Sets strikeout.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder strikeout(Boolean value) {
            strikeout = value;
            return this;
        }

        /**
         * Sets the underline style.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder underline(Underline value) {
            underline = value;
            return this;
        }

        /**
         * Sets the font colour.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder fontColor(String value) {
            fontColor = value;
            return this;
        }

        /**
         * Sets superscript or subscript.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder script(Script value) {
            script = value;
            return this;
        }

        /**
         * Sets the Excel data format.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder dataFormat(String value) {
            dataFormat = value;
            return this;
        }

        /**
         * Sets the locked flag.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder locked(Boolean value) {
            locked = value;
            return this;
        }

        /**
         * Sets the hidden flag.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder hidden(Boolean value) {
            hidden = value;
            return this;
        }

        /**
         * Sets the quote prefix flag.
         *
         * @param value the value, or null
         * @return this builder
         */
        public Builder quotePrefix(Boolean value) {
            quotePrefix = value;
            return this;
        }

        /**
         * Builds the attributes.
         *
         * @return the attributes
         * @throws IllegalArgumentException if an enum component is {@code INHERIT}
         */
        public StyleAttributes build() {
            return new StyleAttributes(align, verticalAlign, wrapText, shrinkToFit, rotation, indent,
                    borderTop, borderBottom, borderLeft, borderRight,
                    borderTopColor, borderBottomColor, borderLeftColor, borderRightColor,
                    fillColor, fillBackgroundColor, fillPattern,
                    fontName, fontSize, bold, italic, strikeout, underline, fontColor, script,
                    dataFormat, locked, hidden, quotePrefix);
        }
    }
}
