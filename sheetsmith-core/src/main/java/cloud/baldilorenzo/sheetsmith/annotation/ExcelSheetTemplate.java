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
 * Marks a sheet template: a class that declares once the sheet configuration shared by several sheet classes.
 * <p>
 * A style sheet, annotated with {@link ExcelStyleSheet}, shares named styles only. A template shares the rest of
 * the sheet configuration: the preset and its accent colour, the style sheets, the title style, the layout options,
 * the frame and the header and body slots. Its attributes have the same names, types and meaning as the attributes
 * of {@link ExcelSheet}, so moving an attribute from a sheet class to a template is a matter of copy and paste. The
 * title is not part of a template: it is the content of a single report.
 * <p>
 * A template carries this annotation only; its fields, methods and other annotations play no role, except for the
 * ones forbidden by the rules below. Sheet classes reference it by class, with {@link ExcelSheet#template()}.
 * Templates provided by a library and templates written by an application work the same way: a template is any
 * class carrying this annotation, with no registration and no name.
 *
 * <h2>Precedence</h2>
 * For every attribute, from the strongest to the weakest level:
 * <ol>
 *   <li>the sheet class;</li>
 *   <li>the template it references;</li>
 *   <li>the application defaults, set with {@link cloud.baldilorenzo.sheetsmith.SheetsmithDefaults}, for the preset
 *       and the accent colour, or the defaults of {@link ExcelSheet} for the other attributes.</li>
 * </ol>
 * An attribute left unset, that is {@code INHERIT} or empty, passes to the next level. In detail:
 * <ul>
 *   <li>the preset comes from the template only: a sheet class that references a template must leave
 *       {@link ExcelSheet#preset()} unset (rule V-22). A template that leaves it unset uses the application
 *       default preset;</li>
 *   <li>the accent colour is the parameter of the template: the sheet class can override it;</li>
 *   <li>the layout options, the frame, its colour and the title style are taken from the sheet class when set
 *       there, otherwise from the template. The title style of a template is ignored by sheet classes without a
 *       title, so the same template serves sheets with and without a title;</li>
 *   <li>the header and body slots are taken one by one: a slot set on the sheet class wins, the other slots keep
 *       the style named by the template.</li>
 * </ul>
 * The style names chosen this way are resolved among the styles available to the sheet class, from the strongest
 * to the weakest level: the styles declared on the sheet class, the styles of its style sheets, the styles of the
 * style sheets of the template. A style found at a stronger level replaces entirely a style with the same name
 * from a weaker level, so a sheet class can redefine a style used by the template by declaring a style with the
 * same name. The column styles, {@link ExcelColumn#headerStyle()} and {@link ExcelColumn#styles()}, can use the
 * styles of the template style sheets too.
 *
 * <h2>Validation</h2>
 * A template is shared, so it must be valid on its own, whatever the sheet classes that use it. Its errors name
 * the template class, with {@code @ExcelSheetTemplate} as element:
 * <ul>
 *   <li>every style name it references, in {@link #titleStyle()} and in the slots, exists in its own style
 *       sheets (rule V-06);</li>
 *   <li>every class in {@link #styleSheets()} is a style sheet (rule V-09), and those style sheets do not define the
 *       same style name (rule V-08);</li>
 *   <li>{@link #accentColor()} and {@link #outerBorderColor()} are valid colours (rule V-13);</li>
 *   <li>the class is not also a sheet class or a style sheet (rule V-23);</li>
 *   <li>the class declares no {@link ExcelStyle}: shared styles belong to style sheets, which the template
 *       references with {@link #styleSheets()} (rule V-24);</li>
 *   <li>the class has no field annotated with {@link ExcelColumn}, inherited fields included (rule V-25).</li>
 * </ul>
 * A sheet class that references a template is also validated with its effective configuration. Its template must
 * carry this annotation (rule V-21), and its preset must be unset (rule V-22). The errors of an invalid template are
 * reported with those of the sheet class, once, without further errors on the sheet class caused by the template.
 * {@link cloud.baldilorenzo.sheetsmith.Sheetsmith#validate(Class)} accepts a template and validates it on its own,
 * and the startup validation of the Spring Boot integration validates every template found in the scanned
 * packages, even when no sheet class references it.
 *
 * <h2>Example</h2>
 * <pre>
 * &#64;ExcelStyleSheet
 * &#64;ExcelStyle(name = "title", fontSize = 16, bold = Toggle.TRUE)
 * &#64;ExcelStyle(name = "header", bold = Toggle.TRUE)
 * &#64;ExcelStyle(name = "total", bold = Toggle.TRUE, borderTop = Border.DOUBLE)
 * public final class CorporateStyles {
 *     private CorporateStyles() {
 *     }
 * }
 *
 * &#64;ExcelSheetTemplate(
 *         preset = TablePreset.LIGHT,
 *         accentColor = "#1F4E79",
 *         styleSheets = CorporateStyles.class,
 *         titleStyle = "title",
 *         autoFilter = Toggle.TRUE,
 *         outerBorder = Border.THIN,
 *         header = &#64;HeaderStyles(base = "header"),
 *         body = &#64;BodyStyles(lastRow = "total"))
 * public final class CorporateReport {
 *     private CorporateReport() {
 *     }
 * }
 *
 * &#64;ExcelSheet(title = "Invoices 2026", template = CorporateReport.class)
 * public record InvoiceLine(
 *         &#64;ExcelColumn(header = "Description", order = 10) String description,
 *         &#64;ExcelColumn(header = "Amount", order = 20, format = "#,##0.00") BigDecimal amount) {
 * }
 *
 * // Same look, with the accent colour of the purchasing department
 * &#64;ExcelSheet(template = CorporateReport.class, accentColor = "#7F6000")
 * public record PurchaseLine(
 *         &#64;ExcelColumn(header = "Supplier", order = 10) String supplier,
 *         &#64;ExcelColumn(header = "Amount", order = 20, format = "#,##0.00") BigDecimal amount) {
 * }
 * </pre>
 *
 * <h2>Stability</h2>
 * A template published as an API, for example by a library shared by the applications of a company, is a visual
 * contract: changing its look changes every sheet that references it, in every application that upgrades. Treat a
 * change of its attributes, or of the styles of its style sheets, as a change of that API; publish a different
 * template when only some sheets must change.
 *
 * @see ExcelSheet#template()
 * @see ExcelStyleSheet
 * @see cloud.baldilorenzo.sheetsmith.annotation
 * @since 1.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelSheetTemplate {

    /**
     * Ready-made table style of the sheets that reference this template.
     * <p>
     * The sheet classes that reference a template cannot choose a preset (rule V-22).
     * {@link TablePreset#INHERIT}, the default, uses the application default preset.
     *
     * @return the preset
     * @see ExcelSheet#preset()
     */
    TablePreset preset() default TablePreset.INHERIT;

    /**
     * Accent colour from which the preset derives its colours, unless the sheet class sets its own.
     * <p>
     * Same syntax as {@link ExcelSheet#accentColor()}; an invalid value violates rule V-13.
     *
     * @return the accent colour; empty, the default, uses the application default accent colour
     * @see ExcelSheet#accentColor()
     */
    String accentColor() default "";

    /**
     * Style sheet classes whose named styles the template and the sheets that reference it can use.
     * <p>
     * Every class listed must be annotated with {@link ExcelStyleSheet} (rule V-09), and they must not define the
     * same style name (rule V-08). Their styles are the weakest level of the styles available to a sheet class:
     * the styles declared on the sheet class and those of its own style sheets win over them. A style sheet listed
     * both here and on the sheet class is not a conflict.
     *
     * @return the style sheet classes; empty by default
     * @see ExcelSheet#styleSheets()
     */
    Class<?>[] styleSheets() default {};

    /**
     * Name of the named style applied to the title, unless the sheet class sets its own.
     * <p>
     * The name must exist in the style sheets of the template (rule V-06). A sheet class without a title ignores
     * it, without error.
     *
     * @return the style name; empty, the default, means no style
     * @see ExcelSheet#titleStyle()
     */
    String titleStyle() default "";

    /**
     * Whether the rows up to and including the header stay visible while scrolling, unless the sheet class
     * decides.
     *
     * @return {@link Toggle#INHERIT}, the default, to leave the choice to the default of
     *         {@link ExcelSheet#freezeHeader()}: frozen
     * @see ExcelSheet#freezeHeader()
     */
    Toggle freezeHeader() default Toggle.INHERIT;

    /**
     * Whether an auto-filter is added to the table, unless the sheet class decides.
     *
     * @return {@link Toggle#INHERIT}, the default, to leave the choice to the default of
     *         {@link ExcelSheet#autoFilter()}: no auto-filter
     * @see ExcelSheet#autoFilter()
     */
    Toggle autoFilter() default Toggle.INHERIT;

    /**
     * Whether columns without an explicit width are sized to their content, unless the sheet class decides.
     *
     * @return {@link Toggle#INHERIT}, the default, to leave the choice to the default of
     *         {@link ExcelSheet#autoSizeColumns()}: auto-sized
     * @see ExcelSheet#autoSizeColumns()
     */
    Toggle autoSizeColumns() default Toggle.INHERIT;

    /**
     * Line of the frame drawn around the header and the data rows, unless the sheet class sets its own.
     *
     * @return the frame line; {@link Border#INHERIT}, the default, means no frame unless the sheet class sets one
     * @see ExcelSheet#outerBorder()
     */
    Border outerBorder() default Border.INHERIT;

    /**
     * Colour of the frame, unless the sheet class sets its own.
     * <p>
     * Same syntax as {@link ExcelSheet#outerBorderColor()}; an invalid value violates rule V-13.
     *
     * @return the frame colour; empty, the default, means the automatic colour unless the sheet class sets one
     * @see ExcelSheet#outerBorderColor()
     */
    String outerBorderColor() default "";

    /**
     * Styles of the header row. Every slot set on the sheet class wins over the same slot of the template; the
     * names must exist in the style sheets of the template (rule V-06).
     *
     * @return the header slots; by default no slot is set
     * @see ExcelSheet#header()
     */
    HeaderStyles header() default @HeaderStyles;

    /**
     * Styles of the data rows, at table level. Every slot set on the sheet class wins over the same slot of the
     * template; the names must exist in the style sheets of the template (rule V-06).
     *
     * @return the body slots; by default no slot is set
     * @see ExcelSheet#body()
     */
    BodyStyles body() default @BodyStyles;

    /**
     * Marker meaning "no template", the default of {@link ExcelSheet#template()}.
     * <p>
     * Never instantiated and never used directly: leave {@link ExcelSheet#template()} unset instead.
     *
     * @since 1.1.0
     */
    final class None {

        private None() {
        }
    }
}
