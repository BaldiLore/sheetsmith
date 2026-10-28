package cloud.baldilorenzo.sheetsmith.annotation;

import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import cloud.baldilorenzo.sheetsmith.style.Toggle;

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
 * <p>
 * Since 1.1.0, a sheet class can take its configuration from a sheet template, referenced with
 * {@link #template()}: a class annotated with {@link ExcelSheetTemplate} that declares once the preset, the accent
 * colour, the style sheets, the title style, the layout options, the frame and the slots shared by several sheet
 * classes. Every attribute set on the sheet class wins over the template, except the preset, which only the
 * template can choose (rule V-22). The precedence is described in {@link ExcelSheetTemplate}.
 *
 * <pre>
 * &#64;ExcelSheet(
 *         title = "Invoice 2026/0042",
 *         titleStyle = "title",
 *         preset = TablePreset.LIGHT,
 *         accentColor = "#1F4E79",
 *         autoFilter = Toggle.TRUE,
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
 * @see ExcelSheetTemplate
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
     * exist among the styles available to the class (rule V-06). When set, it wins over the title style of the
     * template; the title style of the template is ignored when the sheet has no title.
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
     * <p>
     * A sheet class that references a template cannot set a preset: the preset of the template applies, and a
     * preset other than {@link TablePreset#INHERIT} together with {@link #template()} violates rule V-22. Remove
     * it, or declare it in the template.
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
     * preset is not {@link TablePreset#NONE}. When set, it wins over the accent colour of the template.
     *
     * @return the accent colour; empty, the default, uses the accent colour of the template, if any, otherwise the
     *         application default accent colour, set with
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
     * same name from a style sheet, and replaces it entirely: the two are not merged. In the same way, the styles
     * of these style sheets win over the styles with the same name of the style sheets of the template; a style
     * sheet listed both here and in the template is not a conflict.
     *
     * @return the style sheet classes; empty by default
     */
    Class<?>[] styleSheets() default {};

    /**
     * Whether the rows up to and including the header stay visible while scrolling.
     * <p>
     * When the sheet has a title, the title row is frozen too. When set, it wins over the template. The type
     * changed in 1.1.0 from {@code boolean} to {@link Toggle}: write {@link Toggle#TRUE} and {@link Toggle#FALSE}
     * instead of {@code true} and {@code false}.
     *
     * @return {@link Toggle#INHERIT}, the default, to use the choice of the template; when neither the sheet class
     *         nor the template decides, the header is frozen
     */
    Toggle freezeHeader() default Toggle.INHERIT;

    /**
     * Whether an auto-filter is added to the table.
     * <p>
     * The filter covers all the columns, from the header row to the last data row; with no data rows, it covers
     * the header row only. When set, it wins over the template. The type changed in 1.1.0 from {@code boolean} to
     * {@link Toggle}: write {@link Toggle#TRUE} and {@link Toggle#FALSE} instead of {@code true} and
     * {@code false}.
     *
     * @return {@link Toggle#INHERIT}, the default, to use the choice of the template; when neither the sheet class
     *         nor the template decides, no auto-filter is added
     */
    Toggle autoFilter() default Toggle.INHERIT;

    /**
     * Whether columns without an explicit width are sized to their content.
     * <p>
     * Columns with {@link ExcelColumn#width()} set are never auto-sized. Sizing measures the text with the fonts
     * installed on the machine, and can fail on servers or containers without installed fonts. When it fails, the
     * width falls back to an estimate: the length in characters of the longest header or data value of the column
     * as Excel displays it, with its format applied, plus 2, at most 255. The time spent sizing grows with the
     * number of rows: for large sheets, disable it and set explicit widths.
     * <p>
     * When set, it wins over the template. The type changed in 1.1.0 from {@code boolean} to {@link Toggle}: write
     * {@link Toggle#TRUE} and {@link Toggle#FALSE} instead of {@code true} and {@code false}.
     *
     * @return {@link Toggle#INHERIT}, the default, to use the choice of the template; when neither the sheet class
     *         nor the template decides, columns are auto-sized
     */
    Toggle autoSizeColumns() default Toggle.INHERIT;

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
     * <p>
     * When set, it wins over the frame line of the template.
     *
     * @return the frame line; {@link Border#INHERIT}, the default, uses the frame line of the template, if any,
     *         otherwise means no frame
     * @see cloud.baldilorenzo.sheetsmith.annotation
     */
    Border outerBorder() default Border.INHERIT;

    /**
     * Colour of the frame drawn by {@link #outerBorder()}.
     * <p>
     * The colour is written as {@code #RRGGBB} or as the name of an
     * {@link org.apache.poi.ss.usermodel.IndexedColors} constant; hexadecimal colours are normalised to upper
     * case. Any other non-empty value violates rule V-13. When set, it wins over the frame colour of the template.
     *
     * @return the frame colour; empty, the default, uses the frame colour of the template, if any, otherwise the
     *         automatic colour, usually black
     * @see ExcelStyle
     */
    String outerBorderColor() default "";

    /**
     * Styles of the header row.
     * <p>
     * Every slot set here wins over the same slot of the template; the slots left empty keep the style named by
     * the template.
     *
     * @return the header slots; by default no slot is set
     * @see HeaderStyles
     */
    HeaderStyles header() default @HeaderStyles;

    /**
     * Styles of the data rows, at table level.
     * <p>
     * Every slot set here wins over the same slot of the template; the slots left empty keep the style named by
     * the template.
     *
     * @return the body slots; by default no slot is set
     * @see BodyStyles
     */
    BodyStyles body() default @BodyStyles;

    /**
     * Sheet template from which this class takes the configuration it does not set itself.
     * <p>
     * The class must be annotated with {@link ExcelSheetTemplate} (rule V-21). Every attribute set on this
     * annotation wins over the template, except {@link #preset()}, which must stay unset (rule V-22). The template
     * is referenced by class only, and a template cannot reference another template.
     *
     * @return the template class; {@link ExcelSheetTemplate.None}, the default, means no template
     * @see ExcelSheetTemplate
     * @since 1.1.0
     */
    Class<?> template() default ExcelSheetTemplate.None.class;
}