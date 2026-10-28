package cloud.baldilorenzo.sheetsmith.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a style sheet: a class that holds named styles shared by several sheet classes.
 * <p>
 * A style sheet carries this annotation and {@link ExcelStyle} declarations; its fields, methods and other
 * annotations play no role. Sheet classes reference it in {@link ExcelSheet#styleSheets()}, and can then use its
 * styles by name as if they were declared on the class. Since 1.1.0, sheet templates can reference style sheets
 * too, in {@link ExcelSheetTemplate#styleSheets()}, and the sheet classes that use the template can then use their
 * styles. The rules are:
 * <ul>
 *   <li>only classes carrying this annotation can be referenced (rule V-09);</li>
 *   <li>style names must be unique within the style sheet itself (rule V-07);</li>
 *   <li>the style sheets referenced by one sheet class must not define the same style name (rule V-08);</li>
 *   <li>a style declared on the sheet class wins over a style with the same name from a style sheet, and replaces
 *       it entirely.</li>
 * </ul>
 * With a template, the styles available to a sheet class come from three levels, from the strongest to the
 * weakest: the styles declared on the sheet class, the styles of its style sheets, and the styles of the style
 * sheets of its template. A style found at a stronger level replaces entirely a style with the same name from a
 * weaker level. Rule V-08 applies only between the style sheets of the same level, and a style sheet referenced
 * by both the sheet class and its template is not a conflict.
 *
 * <pre>
 * &#64;ExcelStyleSheet
 * &#64;ExcelStyle(name = "header", bold = Toggle.TRUE, fillColor = "#1F4E79", fontColor = "#FFFFFF")
 * &#64;ExcelStyle(name = "zebra", fillColor = "#EEF3F8")
 * public final class CorporateStyles {
 *     private CorporateStyles() {
 *     }
 * }
 *
 * &#64;ExcelSheet(styleSheets = CorporateStyles.class,
 *         header = &#64;HeaderStyles(base = "header"),
 *         body = &#64;BodyStyles(odd = "zebra"))
 * public record InvoiceLine(
 *         &#64;ExcelColumn(header = "Description", order = 10) String description) {
 * }
 * </pre>
 *
 * @see ExcelSheet#styleSheets()
 * @see ExcelSheetTemplate#styleSheets()
 * @see ExcelStyle
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelStyleSheet {
}
