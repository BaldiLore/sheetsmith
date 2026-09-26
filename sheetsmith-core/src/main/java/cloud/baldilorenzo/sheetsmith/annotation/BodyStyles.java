package cloud.baldilorenzo.sheetsmith.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Body slots: the named styles of the data rows, at table level.
 * <p>
 * Usable only as the value of {@link ExcelSheet#body()}. Every attribute is the name of a named style declared with
 * {@link ExcelStyle}; empty means no style. A name that does not exist violates rule V-06.
 * <p>
 * Data cells are styled by the body cascade, from the least to the most specific level:
 * <ol>
 *   <li>the preset body styles, if any;</li>
 *   <li>{@link #base()};</li>
 *   <li>{@link #odd()} or {@link #even()};</li>
 *   <li>the frame edges of {@link ExcelSheet#outerBorder()};</li>
 *   <li>{@link #lastColumn()}, then {@link #firstColumn()};</li>
 *   <li>{@link #lastRow()}, then {@link #firstRow()};</li>
 *   <li>the column slots of {@link ColumnStyles}: base, odd or even, last row then first row;</li>
 *   <li>the {@link ExcelColumn#format()} of the column.</li>
 * </ol>
 * The rules that follow from this order:
 * <ul>
 *   <li>data rows are numbered from 1, so the first data row is odd;</li>
 *   <li>the row wins over the column: when a row slot and a column slot of this annotation set the same attribute
 *       on one cell, the row slot wins;</li>
 *   <li>the first wins over the last: with one data row, that row is both first and last and {@code firstRow}
 *       wins over {@code lastRow}; with one column, {@code firstColumn} wins over {@code lastColumn};</li>
 *   <li>the frame sits below the first and last row and column slots, so a slot that sets a border side
 *       overrides the frame on that side;</li>
 *   <li>column slots are the most specific level, so a column can always override the table;</li>
 *   <li>body slots apply only to data cells, never to the header or the title.</li>
 * </ul>
 *
 * @see ColumnStyles
 * @see HeaderStyles
 * @see cloud.baldilorenzo.sheetsmith.annotation
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface BodyStyles {

    /**
     * Style of every data cell. Level 2 of the body cascade, just above the preset.
     *
     * @return the style name; empty, the default, means no style
     */
    String base() default "";

    /**
     * Style of the cells of even data rows: rows 2, 4, 6 and so on. Level 3 of the body cascade.
     *
     * @return the style name; empty, the default, means no style
     */
    String even() default "";

    /**
     * Style of the cells of odd data rows: rows 1, 3, 5 and so on. Level 3 of the body cascade.
     *
     * @return the style name; empty, the default, means no style
     */
    String odd() default "";

    /**
     * Style of the cells of the first data row. Level 6 of the body cascade, applied after {@link #lastRow()}, so
     * it wins when the table has one data row.
     *
     * @return the style name; empty, the default, means no style
     */
    String firstRow() default "";

    /**
     * Style of the cells of the last data row. Level 6 of the body cascade, applied before {@link #firstRow()}.
     *
     * @return the style name; empty, the default, means no style
     */
    String lastRow() default "";

    /**
     * Style of the data cells of the first column. Level 5 of the body cascade, applied after
     * {@link #lastColumn()}, so it wins when the table has one column.
     *
     * @return the style name; empty, the default, means no style
     */
    String firstColumn() default "";

    /**
     * Style of the data cells of the last column. Level 5 of the body cascade, applied before
     * {@link #firstColumn()}.
     *
     * @return the style name; empty, the default, means no style
     */
    String lastColumn() default "";
}
