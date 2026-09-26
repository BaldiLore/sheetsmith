package cloud.baldilorenzo.sheetsmith.annotation;

import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a sheet class and configures its sheet: title, preset, shared style sheets, layout options and
 * the header and body slots.
 * <p>
 * Mandatory on every class passed to {@link cloud.baldilorenzo.sheetsmith.SheetData}: a class without it violates
 * rule V-01. The annotation is not inherited, so every exported class carries its own; superclasses that only
 * contribute columns do not need it. The columns are the fields annotated with {@link ExcelColumn}, including
 * those declared on superclasses.
 * <p>
 * Every attribute is optional. With the defaults, the sheet has no title, a frozen header, auto-sized columns, no
 * auto-filter, no frame, and no style other than the application default preset.
 *
 * <pre>
 * &#64;ExcelSheet(
 *         title = "Invoice 2026/0042",
 *         titleStyle = "title",
 *         preset = TablePreset.LIGHT,
 *         accentColor = "#1F4E79",
 *         autoFilter = true,
 *         body = &#64;BodyStyles(lastRow = "total"))
 * &#64;ExcelStyle(name = "title", fontSize = 16, bold = Toggle.TRUE)
 * &#64;ExcelStyle(name = "total", bold = Toggle.TRUE, borderTop = Border.DOUBLE)
 * public record InvoiceLine(
 *         &#64;ExcelColumn(header = "Description", order = 10) String description,
 *         &#64;ExcelColumn(header = "Amount", order = 20, format = "#,##0.00") BigDecimal amount) {
 * }
 * </pre>
 *
 * @see ExcelColumn
 * @see ExcelStyle
 * @see cloud.baldilorenzo.sheetsmith.annotation
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelSheet {

    /**
     * Title shown in a row above the header.
     * <p>
     * The title is written in the first column and merged across all the columns of the table. It is outside the
     * frame drawn by {@link #outerBorder()} and has no role: header and body slots never apply to it. Its style
     * is the preset title style, if any, followed by {@link #titleStyle()}.
     *
     * @return the title; empty, the default, means no title row
     */
    String title() default "";

    /**
     * Name of the named style applied to the title.
     * <p>
     * Allowed only when {@link #title()} is set: a title style without a title violates rule V-15. The name must
     * exist among the styles available to the class (rule V-06).
     *
     * @return the style name; empty, the default, means no style
     */
    String titleStyle() default "";

    /**
     * Ready-made table style, generated from the accent colour.
     * <p>
     * {@link TablePreset#INHERIT}, the default, uses the application default preset, set with
     * {@link cloud.baldilorenzo.sheetsmith.SheetsmithDefaults} or the {@code sheetsmith.preset} property.
     * {@link TablePreset#NONE} applies no preset, whatever the application default. The preset is the lowest
     * level of the style cascade: every declared style overrides it attribute by attribute.
     *
     * @return the preset
     * @see TablePreset
     */
    TablePreset preset() default TablePreset.INHERIT;

    /**
     * Accent colour from which the preset derives its colours.
     * <p>
     * The colour is written as {@code #RRGGBB} or as the name of an
     * {@link org.apache.poi.ss.usermodel.IndexedColors} constant; hexadecimal colours are normalised to upper
     * case. Any other non-empty value violates rule V-13. The accent colour has an effect only when the effective
     * preset is not {@link TablePreset#NONE}.
     *
     * @return the accent colour; empty, the default, uses the application default accent colour, set with
     *         {@link cloud.baldilorenzo.sheetsmith.SheetsmithDefaults} or the {@code sheetsmith.accent-color}
     *         property
     * @see ExcelStyle
     */
    String accentColor() default "";

    /**
     * Style sheet classes whose named styles this class can reference.
     * <p>
     * Every class listed must be annotated with {@link ExcelStyleSheet} (rule V-09). The style sheets of one class
     * must not define the same style name (rule V-08). A style declared on this class wins over a style with the
     * same name from a style sheet, and replaces it entirely: the two are not merged.
     *
     * @return the style sheet classes; empty by default
     */
    Class<?>[] styleSheets() default {};

    /**
     * Whether the rows up to and including the header stay visible while scrolling.
     * <p>
     * When the sheet has a title, the title row is frozen too.
     *
     * @return true, the default, to freeze the header
     */
    boolean freezeHeader() default true;

    /**
     * Whether an auto-filter is added to the table.
     * <p>
     * The filter covers all the columns, from the header row to the last data row; with no data rows, it covers
     * the header row only.
     *
     * @return true to add an auto-filter; false by default
     */
    boolean autoFilter() default false;

    /**
     * Whether columns without an explicit width are sized to their content.
     * <p>
     * Columns with {@link ExcelColumn#width()} set are never auto-sized. Sizing measures the text with the fonts
     * installed on the machine, and can fail on servers or containers without installed fonts. When it fails, the
     * width falls back to an estimate: the length in characters of the longest header or text value of the
     * column, plus 2, at most 255. The time spent sizing grows with the number of rows: for large sheets, disable
     * it and set explicit widths.
     *
     * @return true, the default, to auto-size columns
     */
    boolean autoSizeColumns() default true;

    /**
     * Line of the frame drawn around the header and the data rows.
     * <p>
     * The frame is drawn along the top of the header row, the left side of the first column, the right side of
     * the last column and the bottom of the last data row, or of the header row when there are no data rows. The
     * title is never framed. Any value other than {@link Border#INHERIT}, {@link Border#NONE} included, is applied
     * to those edges.
     * <p>
     * In the style cascade the frame sits above the preset, the base slots and the odd and even slots, and below
     * the first and last column and row slots, the column slots and {@link ExcelColumn#headerStyle()}: a style at
     * those levels that sets a border side overrides the frame on that side.
     *
     * @return the frame line; {@link Border#INHERIT}, the default, means no frame
     * @see cloud.baldilorenzo.sheetsmith.annotation
     */
    Border outerBorder() default Border.INHERIT;

    /**
     * Colour of the frame drawn by {@link #outerBorder()}.
     * <p>
     * The colour is written as {@code #RRGGBB} or as the name of an
     * {@link org.apache.poi.ss.usermodel.IndexedColors} constant; hexadecimal colours are normalised to upper
     * case. Any other non-empty value violates rule V-13.
     *
     * @return the frame colour; empty, the default, means the automatic colour, usually black
     * @see ExcelStyle
     */
    String outerBorderColor() default "";

    /**
     * Styles of the header row.
     *
     * @return the header slots; by default no slot is set
     * @see HeaderStyles
     */
    HeaderStyles header() default @HeaderStyles;

    /**
     * Styles of the data rows, at table level.
     *
     * @return the body slots; by default no slot is set
     * @see BodyStyles
     */
    BodyStyles body() default @BodyStyles;
}
