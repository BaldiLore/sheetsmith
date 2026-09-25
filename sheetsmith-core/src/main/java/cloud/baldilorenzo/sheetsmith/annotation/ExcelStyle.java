package cloud.baldilorenzo.sheetsmith.annotation;

import cloud.baldilorenzo.sheetsmith.style.Align;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.Fill;
import cloud.baldilorenzo.sheetsmith.style.Script;
import cloud.baldilorenzo.sheetsmith.style.Toggle;
import cloud.baldilorenzo.sheetsmith.style.Underline;
import cloud.baldilorenzo.sheetsmith.style.VerticalAlign;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a named style on an exported class or on a style sheet class annotated with {@link ExcelStyleSheet}.
 * <p>
 * Styles are referenced by name from the slot annotations. An attribute left to its default is not set and keeps
 * the value of the level below. Colours are written as {@code #RRGGBB} or as the name of an
 * {@link org.apache.poi.ss.usermodel.IndexedColors} constant. Within one style, a side-specific border or border
 * colour wins over the all-sides attribute.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(ExcelStyles.class)
public @interface ExcelStyle {

    /**
     * Sentinel value meaning "not set" for numeric attributes. {@code -1} is not used because it is a valid
     * rotation.
     */
    int UNSET = Integer.MIN_VALUE;

    /**
     * Name of the style, not blank and unique in its scope.
     *
     * @return the style name
     */
    String name();

    /**
     * Horizontal alignment.
     *
     * @return the alignment
     */
    Align align() default Align.INHERIT;

    /**
     * Vertical alignment.
     *
     * @return the alignment
     */
    VerticalAlign verticalAlign() default VerticalAlign.INHERIT;

    /**
     * Whether text wraps on several lines.
     *
     * @return the wrap setting
     */
    Toggle wrapText() default Toggle.INHERIT;

    /**
     * Whether text shrinks to fit the cell width.
     *
     * @return the shrink setting
     */
    Toggle shrinkToFit() default Toggle.INHERIT;

    /**
     * Text rotation in degrees, from -90 to 90, or 255 for vertical text.
     *
     * @return the rotation, or {@link #UNSET}
     */
    int rotation() default UNSET;

    /**
     * Indentation level, from 0 to 250.
     *
     * @return the indentation, or {@link #UNSET}
     */
    int indent() default UNSET;

    /**
     * Border line of all four sides.
     *
     * @return the border line
     */
    Border border() default Border.INHERIT;

    /**
     * Border colour of all four sides.
     *
     * @return the colour; empty means not set
     */
    String borderColor() default "";

    /**
     * Border line of the top side, overriding {@link #border()}.
     *
     * @return the border line
     */
    Border borderTop() default Border.INHERIT;

    /**
     * Border line of the bottom side, overriding {@link #border()}.
     *
     * @return the border line
     */
    Border borderBottom() default Border.INHERIT;

    /**
     * Border line of the left side, overriding {@link #border()}.
     *
     * @return the border line
     */
    Border borderLeft() default Border.INHERIT;

    /**
     * Border line of the right side, overriding {@link #border()}.
     *
     * @return the border line
     */
    Border borderRight() default Border.INHERIT;

    /**
     * Border colour of the top side, overriding {@link #borderColor()}.
     *
     * @return the colour; empty means not set
     */
    String borderTopColor() default "";

    /**
     * Border colour of the bottom side, overriding {@link #borderColor()}.
     *
     * @return the colour; empty means not set
     */
    String borderBottomColor() default "";

    /**
     * Border colour of the left side, overriding {@link #borderColor()}.
     *
     * @return the colour; empty means not set
     */
    String borderLeftColor() default "";

    /**
     * Border colour of the right side, overriding {@link #borderColor()}.
     *
     * @return the colour; empty means not set
     */
    String borderRightColor() default "";

    /**
     * Foreground fill colour. When set without a {@link #fillPattern()}, the fill is solid.
     *
     * @return the colour; empty means not set
     */
    String fillColor() default "";

    /**
     * Background colour, used by patterned fills.
     *
     * @return the colour; empty means not set
     */
    String fillBackgroundColor() default "";

    /**
     * Fill pattern.
     *
     * @return the pattern
     */
    Fill fillPattern() default Fill.INHERIT;

    /**
     * Font name, for example {@code Calibri}.
     *
     * @return the font name; empty means not set
     */
    String fontName() default "";

    /**
     * Font size in points, from 1 to 409.
     *
     * @return the size, or {@link #UNSET}
     */
    int fontSize() default UNSET;

    /**
     * Bold font.
     *
     * @return the bold setting
     */
    Toggle bold() default Toggle.INHERIT;

    /**
     * Italic font.
     *
     * @return the italic setting
     */
    Toggle italic() default Toggle.INHERIT;

    /**
     * Strikeout font.
     *
     * @return the strikeout setting
     */
    Toggle strikeout() default Toggle.INHERIT;

    /**
     * Underline style.
     *
     * @return the underline
     */
    Underline underline() default Underline.INHERIT;

    /**
     * Font colour.
     *
     * @return the colour; empty means not set
     */
    String fontColor() default "";

    /**
     * Superscript or subscript.
     *
     * @return the script
     */
    Script script() default Script.INHERIT;

    /**
     * Excel data format, in Excel syntax, for example {@code dd/mm/yyyy}.
     *
     * @return the format; empty means not set
     */
    String dataFormat() default "";

    /**
     * Whether the cell is locked when the sheet is protected.
     *
     * @return the locked setting
     */
    Toggle locked() default Toggle.INHERIT;

    /**
     * Whether the formula of the cell is hidden when the sheet is protected.
     *
     * @return the hidden setting
     */
    Toggle hidden() default Toggle.INHERIT;

    /**
     * Whether the cell value is prefixed with a quote, so that it is always shown as text.
     *
     * @return the quote prefix setting
     */
    Toggle quotePrefix() default Toggle.INHERIT;
}
