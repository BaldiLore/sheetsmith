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
 * Declares a named style: a set of formatting attributes that slots reference by name.
 * <p>
 * Declare named styles on the sheet class, or on a style sheet class annotated with {@link ExcelStyleSheet} to
 * share them between sheet classes. The annotation is repeatable. Style names must not be blank (rule V-16) and
 * must be unique among the styles declared on one class (rule V-07). A style declared on the sheet class replaces
 * a style with the same name coming from a style sheet. Named styles are not inherited from superclasses.
 * <p>
 * A style takes effect through the slots that reference it: {@link ExcelSheet#titleStyle()},
 * {@link ExcelSheet#header()}, {@link ExcelSheet#body()}, {@link ExcelColumn#headerStyle()} and
 * {@link ExcelColumn#styles()}. One style can be referenced by several slots. A reference to a name that does not
 * exist violates rule V-06.
 *
 * <h2 id="unset">Unset attributes</h2>
 * Every attribute other than {@link #name()} has a default meaning "unset": {@code INHERIT} for enum attributes,
 * including the three-state {@link Toggle} used for yes and no attributes, the empty string for colours, font
 * names and formats, and {@link #UNSET} for numeric attributes. An unset attribute overrides nothing.
 * <p>
 * The effective style of a cell is obtained by merging the styles of the cascade attribute by attribute: each
 * level overrides only the attributes it sets and keeps the others. Attributes that no level sets keep the Excel
 * defaults. The cascade is described in the {@link cloud.baldilorenzo.sheetsmith.annotation} package.
 * <p>
 * Within one style, a side-specific border line or border colour, such as {@link #borderTop()} or
 * {@link #borderTopColor()}, wins over the all-sides attribute, {@link #border()} or {@link #borderColor()}.
 *
 * <h2 id="colours">Colours</h2>
 * Every colour attribute of sheetsmith, here, in {@link ExcelSheet#accentColor()} and
 * {@link ExcelSheet#outerBorderColor()}, and in the application defaults, accepts:
 * <ul>
 *   <li>a hexadecimal colour {@code #RRGGBB}, in upper or lower case, for example {@code #1F4E79};</li>
 *   <li>the name of an {@link org.apache.poi.ss.usermodel.IndexedColors} constant, for example {@code DARK_BLUE}
 *       or {@code GREY_25_PERCENT}, in upper case as declared by that enum.</li>
 * </ul>
 * An empty string means unset. Any other value violates rule V-13. Hexadecimal colours are normalised to upper
 * case, so {@code #1f4e79} and {@code #1F4E79} are the same colour and share one cell style; indexed colour names
 * are not normalised. Prefer hexadecimal colours: they are exact, while indexed colours depend on the palette of
 * the application that opens the file.
 *
 * <h2>Fill</h2>
 * When {@link #fillColor()} is set and {@link #fillPattern()} is unset, the fill is solid: a plain background
 * needs {@code fillColor} only. {@link #fillBackgroundColor()} is used only by patterned fills.
 *
 * <h2>Ranges</h2>
 * {@link #rotation()} accepts -90 to 90, or 255; {@link #indent()} accepts 0 to 250; {@link #fontSize()} accepts
 * 1 to 409. Values outside these ranges violate rule V-14; {@link #UNSET} is always accepted.
 *
 * <h2>Formats and protection</h2>
 * {@link #dataFormat()} uses the Excel format syntax described in
 * {@link cloud.baldilorenzo.sheetsmith.SheetsmithDefaults}. {@link #locked()} and {@link #hidden()} take effect
 * only on protected sheets, and sheetsmith does not protect sheets: they matter only if the reader protects the
 * sheet in Excel.
 *
 * <h2>Example</h2>
 * <pre>
 * &#64;ExcelSheet(header = &#64;HeaderStyles(base = "header"), body = &#64;BodyStyles(odd = "zebra"))
 * &#64;ExcelStyle(name = "header", bold = Toggle.TRUE, fillColor = "#1F4E79", fontColor = "#FFFFFF")
 * &#64;ExcelStyle(name = "zebra", fillColor = "#EEF3F8")
 * &#64;ExcelStyle(name = "money", align = Align.RIGHT, dataFormat = "#,##0.00")
 * public record OrderRow(
 *         &#64;ExcelColumn(header = "Order", order = 10) String number,
 *         &#64;ExcelColumn(header = "Amount", order = 20, styles = &#64;ColumnStyles(base = "money"))
 *         BigDecimal amount) {
 * }
 * </pre>
 *
 * @see ExcelStyleSheet
 * @see cloud.baldilorenzo.sheetsmith.annotation
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(ExcelStyles.class)
public @interface ExcelStyle {

    /**
     * Value meaning "unset" for the numeric attributes {@link #rotation()}, {@link #indent()},
     * {@link #fontSize()} and {@link ExcelColumn#width()}.
     * <p>
     * Annotation attributes cannot be null, so a sentinel is needed. {@link Integer#MIN_VALUE} is used instead of
     * {@code -1} because {@code -1} is a valid rotation.
     */
    int UNSET = Integer.MIN_VALUE;

    /**
     * Name of the style, used by slots to reference it.
     * <p>
     * Must not be blank (rule V-16) and must be unique among the styles declared on one class (rule V-07).
     *
     * @return the style name
     */
    String name();

    /**
     * Horizontal alignment of the content.
     *
     * @return the alignment; {@link Align#INHERIT}, the default, means unset
     */
    Align align() default Align.INHERIT;

    /**
     * Vertical alignment of the content.
     *
     * @return the alignment; {@link VerticalAlign#INHERIT}, the default, means unset
     */
    VerticalAlign verticalAlign() default VerticalAlign.INHERIT;

    /**
     * Whether long text wraps on several lines.
     *
     * @return the wrap setting; {@link Toggle#INHERIT}, the default, means unset
     */
    Toggle wrapText() default Toggle.INHERIT;

    /**
     * Whether the font shrinks so that the text fits the cell width.
     *
     * @return the shrink setting; {@link Toggle#INHERIT}, the default, means unset
     */
    Toggle shrinkToFit() default Toggle.INHERIT;

    /**
     * Text rotation in degrees, from -90 to 90, or 255 for vertical stacked text.
     * <p>
     * Values outside the range violate rule V-14.
     *
     * @return the rotation; {@link #UNSET}, the default, means unset
     */
    int rotation() default UNSET;

    /**
     * Indentation level, from 0 to 250.
     * <p>
     * Values outside the range violate rule V-14.
     *
     * @return the indentation; {@link #UNSET}, the default, means unset
     */
    int indent() default UNSET;

    /**
     * Border line of all four sides.
     * <p>
     * A side-specific line set in the same style, such as {@link #borderTop()}, wins over it on that side.
     *
     * @return the border line; {@link Border#INHERIT}, the default, means unset
     */
    Border border() default Border.INHERIT;

    /**
     * Border colour of all four sides, in the colour syntax described in {@link ExcelStyle}.
     * <p>
     * A side-specific colour set in the same style, such as {@link #borderTopColor()}, wins over it on that side.
     * An invalid colour violates rule V-13.
     *
     * @return the colour; empty, the default, means unset
     */
    String borderColor() default "";

    /**
     * Border line of the top side. Wins over {@link #border()} within this style.
     *
     * @return the border line; {@link Border#INHERIT}, the default, means unset
     */
    Border borderTop() default Border.INHERIT;

    /**
     * Border line of the bottom side. Wins over {@link #border()} within this style.
     *
     * @return the border line; {@link Border#INHERIT}, the default, means unset
     */
    Border borderBottom() default Border.INHERIT;

    /**
     * Border line of the left side. Wins over {@link #border()} within this style.
     *
     * @return the border line; {@link Border#INHERIT}, the default, means unset
     */
    Border borderLeft() default Border.INHERIT;

    /**
     * Border line of the right side. Wins over {@link #border()} within this style.
     *
     * @return the border line; {@link Border#INHERIT}, the default, means unset
     */
    Border borderRight() default Border.INHERIT;

    /**
     * Border colour of the top side. Wins over {@link #borderColor()} within this style. An invalid colour
     * violates rule V-13.
     *
     * @return the colour; empty, the default, means unset
     */
    String borderTopColor() default "";

    /**
     * Border colour of the bottom side. Wins over {@link #borderColor()} within this style. An invalid colour
     * violates rule V-13.
     *
     * @return the colour; empty, the default, means unset
     */
    String borderBottomColor() default "";

    /**
     * Border colour of the left side. Wins over {@link #borderColor()} within this style. An invalid colour
     * violates rule V-13.
     *
     * @return the colour; empty, the default, means unset
     */
    String borderLeftColor() default "";

    /**
     * Border colour of the right side. Wins over {@link #borderColor()} within this style. An invalid colour
     * violates rule V-13.
     *
     * @return the colour; empty, the default, means unset
     */
    String borderRightColor() default "";

    /**
     * Foreground fill colour: the background colour of the cell.
     * <p>
     * When the effective style has a fill colour and no {@link #fillPattern()}, the fill is solid. An invalid
     * colour violates rule V-13.
     *
     * @return the colour; empty, the default, means unset
     */
    String fillColor() default "";

    /**
     * Second fill colour, used only by patterned fills. An invalid colour violates rule V-13.
     *
     * @return the colour; empty, the default, means unset
     */
    String fillBackgroundColor() default "";

    /**
     * Fill pattern. A plain background needs only {@link #fillColor()}: without a pattern, a fill colour gives a
     * solid fill.
     *
     * @return the pattern; {@link Fill#INHERIT}, the default, means unset
     */
    Fill fillPattern() default Fill.INHERIT;

    /**
     * Font name, for example {@code Arial}.
     *
     * @return the font name; empty, the default, means unset
     */
    String fontName() default "";

    /**
     * Font size in points, from 1 to 409.
     * <p>
     * Values outside the range violate rule V-14.
     *
     * @return the size; {@link #UNSET}, the default, means unset
     */
    int fontSize() default UNSET;

    /**
     * Whether the font is bold.
     *
     * @return the bold setting; {@link Toggle#INHERIT}, the default, means unset
     */
    Toggle bold() default Toggle.INHERIT;

    /**
     * Whether the font is italic.
     *
     * @return the italic setting; {@link Toggle#INHERIT}, the default, means unset
     */
    Toggle italic() default Toggle.INHERIT;

    /**
     * Whether the text is struck out.
     *
     * @return the strikeout setting; {@link Toggle#INHERIT}, the default, means unset
     */
    Toggle strikeout() default Toggle.INHERIT;

    /**
     * Underline of the text.
     *
     * @return the underline; {@link Underline#INHERIT}, the default, means unset
     */
    Underline underline() default Underline.INHERIT;

    /**
     * Font colour. An invalid colour violates rule V-13.
     *
     * @return the colour; empty, the default, means unset
     */
    String fontColor() default "";

    /**
     * Superscript or subscript.
     *
     * @return the script; {@link Script#INHERIT}, the default, means unset
     */
    Script script() default Script.INHERIT;

    /**
     * Excel format of the cells the style applies to, for example {@code #,##0.00} or {@code dd/mm/yyyy}.
     * <p>
     * The format uses the Excel format syntax described in {@link cloud.baldilorenzo.sheetsmith.SheetsmithDefaults},
     * not the syntax of {@link java.time.format.DateTimeFormatter}. On data cells, {@link ExcelColumn#format()}
     * wins over it; when no level of the cascade sets a format, the application default for the kind of value
     * applies.
     *
     * @return the format; empty, the default, means unset
     */
    String dataFormat() default "";

    /**
     * Whether the cell is locked. Effective only when the sheet is protected, and sheetsmith does not protect
     * sheets.
     *
     * @return the locked setting; {@link Toggle#INHERIT}, the default, means unset
     */
    Toggle locked() default Toggle.INHERIT;

    /**
     * Whether the formula of the cell is hidden. Effective only when the sheet is protected, and sheetsmith does
     * not protect sheets.
     *
     * @return the hidden setting; {@link Toggle#INHERIT}, the default, means unset
     */
    Toggle hidden() default Toggle.INHERIT;

    /**
     * Whether the cell has the Excel quote prefix, which marks its value as text.
     *
     * @return the quote prefix setting; {@link Toggle#INHERIT}, the default, means unset
     */
    Toggle quotePrefix() default Toggle.INHERIT;
}
