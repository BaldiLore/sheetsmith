package cloud.baldilorenzo.sheetsmith.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Column slots: the named styles of the data cells of a single column.
 * <p>
 * Usable only as the value of {@link ExcelColumn#styles()}. Every attribute is the name of a named style declared
 * with {@link ExcelStyle}; empty means no style. A name that does not exist violates rule V-06.
 * <p>
 * Column slots are the most specific slots of the body cascade. They are applied after every table level (the
 * preset, the body slots of {@link BodyStyles} and the frame), in this order: {@link #base()}, {@link #odd()} or
 * {@link #even()}, {@link #lastRow()}, then {@link #firstRow()}. Only {@link ExcelColumn#format()} comes after
 * them. The rules that follow:
 * <ul>
 *   <li>data rows are numbered from 1, so the first data row is odd;</li>
 *   <li>a column slot overrides every table slot, the row slots included, attribute by attribute;</li>
 *   <li>with one data row, that row is both first and last and {@code firstRow} wins over {@code lastRow};</li>
 *   <li>column slots apply only to data cells, never to the header cell of the column, which is styled by
 *       {@link ExcelColumn#headerStyle()}.</li>
 * </ul>
 *
 * @see BodyStyles
 * @see ExcelColumn#styles()
 * @see cloud.baldilorenzo.sheetsmith.annotation
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ColumnStyles {

    /**
     * Style of every data cell of the column. The first column level of the body cascade.
     *
     * @return the style name; empty, the default, means no style
     */
    String base() default "";

    /**
     * Style of the cells of the column in even data rows: rows 2, 4, 6 and so on. Applied after {@link #base()}.
     *
     * @return the style name; empty, the default, means no style
     */
    String even() default "";

    /**
     * Style of the cells of the column in odd data rows: rows 1, 3, 5 and so on. Applied after {@link #base()}.
     *
     * @return the style name; empty, the default, means no style
     */
    String odd() default "";

    /**
     * Style of the cell of the column in the first data row. Applied after {@link #lastRow()}, so it wins when the
     * table has one data row.
     *
     * @return the style name; empty, the default, means no style
     */
    String firstRow() default "";

    /**
     * Style of the cell of the column in the last data row. Applied after the odd and even slots and before
     * {@link #firstRow()}.
     *
     * @return the style name; empty, the default, means no style
     */
    String lastRow() default "";
}
