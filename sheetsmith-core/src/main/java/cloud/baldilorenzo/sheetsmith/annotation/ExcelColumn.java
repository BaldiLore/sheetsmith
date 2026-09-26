package cloud.baldilorenzo.sheetsmith.annotation;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exports a field of a sheet class as a column.
 * <p>
 * Export is opt-in: only fields carrying this annotation become columns, all other fields are ignored. Columns are
 * collected from the sheet class and from its superclasses, then sorted by {@link #order()}. A sheet class needs
 * at least one column (rule V-02), and the annotation is not allowed on static fields (rule V-05). On a record
 * component the annotation applies to the component field. {@link #header()} and {@link #order()} have no
 * default, so the compiler rejects a column without them.
 *
 * <h2>Value access</h2>
 * The value of a column is read:
 * <ol>
 *   <li>for a record, through the component accessor;</li>
 *   <li>for a class, through a public no-argument getter, {@code getX()}, or {@code isX()} for {@code boolean} and
 *       {@code Boolean} fields, declared on the class or inherited, whose return type is assignable to the field
 *       type;</li>
 *   <li>otherwise, directly from the field, even when it is private.</li>
 * </ol>
 * In a modular application, sheetsmith needs reflective access to the packages that contain the sheet classes: open
 * them to the library, for example with {@code opens com.example.export to cloud.baldilorenzo.sheetsmith;} in
 * {@code module-info.java}. A value that cannot be accessed violates rule V-17, whose message names the package to
 * open. A getter that throws while a sheet is generated causes a
 * {@link cloud.baldilorenzo.sheetsmith.SheetsmithGenerationException}.
 *
 * <pre>
 * &#64;ExcelSheet
 * &#64;ExcelStyle(name = "money", align = Align.RIGHT)
 * public record CustomerRow(
 *         &#64;ExcelColumn(header = "Code", order = 10, width = 12) String code,
 *         &#64;ExcelColumn(header = "Name", order = 20) String name,
 *         &#64;ExcelColumn(header = "Customer since", order = 30, format = "dd/mm/yyyy") LocalDate since,
 *         &#64;ExcelColumn(header = "Revenue", order = 40, format = "#,##0.00",
 *                 converter = MoneyConverter.class, styles = &#64;ColumnStyles(base = "money")) Money revenue) {
 * }
 * </pre>
 *
 * @see ExcelSheet
 * @see cloud.baldilorenzo.sheetsmith.annotation
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelColumn {

    /**
     * Text of the header cell of the column.
     * <p>
     * Must not be blank (rule V-04). The text is written as it is: header texts are not translated.
     *
     * @return the header text
     */
    String header();

    /**
     * Position of the column: columns appear left to right in ascending order.
     * <p>
     * Values must be unique within the sheet class, columns inherited from superclasses included (rule V-03), but
     * need not be consecutive: numbering in steps of 10 leaves room to insert columns later. The order is
     * mandatory because reflection does not return fields in declaration order.
     *
     * @return the order value
     */
    int order();

    /**
     * Width of the column, in characters, from 1 to 255.
     * <p>
     * Values outside the range violate rule V-14. A column with an explicit width keeps it and is excluded from
     * the automatic sizing of {@link ExcelSheet#autoSizeColumns()}.
     *
     * @return the width; {@link ExcelStyle#UNSET}, the default, leaves the width to automatic sizing or, when that
     *         is disabled, to the spreadsheet application
     */
    int width() default ExcelStyle.UNSET;

    /**
     * Excel format of the data cells of this column, for example {@code #,##0.00} or {@code dd/mm/yyyy}.
     * <p>
     * The format uses the Excel format syntax described in {@link cloud.baldilorenzo.sheetsmith.SheetsmithDefaults},
     * not the syntax of {@link java.time.format.DateTimeFormatter}. It is the last level of the body cascade, so it
     * wins over the {@link ExcelStyle#dataFormat()} of every style applied to the cell and over the application
     * default formats. It does not apply to the header cell.
     *
     * @return the format; empty, the default, leaves the format to the styles and the application defaults
     */
    String format() default "";

    /**
     * Field converter used for the values of this column, instead of the application or built-in converter for
     * the field type.
     * <p>
     * The converter must handle a type assignable from the field type, primitive types counting as their wrappers:
     * a converter for an incompatible type violates rule V-11. The check is made when the handled type can be
     * determined from the generic declaration of the converter class. The converter must be creatable by the
     * {@link cloud.baldilorenzo.sheetsmith.convert.CellConverterFactory} of the generator, otherwise rule V-12 is
     * violated:
     * <ul>
     *   <li>without Spring, it must be a public class with a public no-argument constructor;</li>
     *   <li>with the Spring Boot auto-configuration, the bean of that class is used when exactly one exists,
     *       otherwise a new instance is created with dependency injection.</li>
     * </ul>
     * One instance is created per converter class and generator, and shared by all the columns that declare it.
     *
     * @return the converter class; {@link CellConverter.None}, the default, means no field converter
     * @see CellConverter
     */
    Class<? extends CellConverter<?>> converter() default CellConverter.None.class;

    /**
     * Name of the named style applied to the header cell of this column.
     * <p>
     * It is the last level of the header cascade: it wins over the preset, the header slots and the frame. The
     * name must exist among the styles available to the class (rule V-06).
     *
     * @return the style name; empty, the default, means no style
     * @see HeaderStyles
     */
    String headerStyle() default "";

    /**
     * Column slots: the styles of the data cells of this column.
     * <p>
     * Column slots are the most specific slots of the body cascade: they come after every table level, so they
     * override the preset, the body slots and the frame. Only {@link #format()} is applied after them.
     *
     * @return the column slots; by default no slot is set
     * @see ColumnStyles
     */
    ColumnStyles styles() default @ColumnStyles;
}
