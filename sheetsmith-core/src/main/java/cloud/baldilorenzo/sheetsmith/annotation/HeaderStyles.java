package cloud.baldilorenzo.sheetsmith.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Header slots: the named styles of the header row.
 * <p>
 * Usable only as the value of {@link ExcelSheet#header()}. Every attribute is the name of a named style declared
 * with {@link ExcelStyle}; empty means no style. A name that does not exist violates rule V-06.
 * <p>
 * Header cells are styled by the header cascade, from the least to the most specific level:
 * <ol>
 *   <li>the preset header style, if any;</li>
 *   <li>{@link #base()};</li>
 *   <li>the frame edges of {@link ExcelSheet#outerBorder()};</li>
 *   <li>{@link #lastColumn()}, then {@link #firstColumn()};</li>
 *   <li>the {@link ExcelColumn#headerStyle()} of the column.</li>
 * </ol>
 * The rules that follow from this order:
 * <ul>
 *   <li>header slots apply only to header cells, never to data cells, and body and column slots never apply to
 *       header cells;</li>
 *   <li>with one column, its header cell is both first and last, and {@code firstColumn} wins over
 *       {@code lastColumn};</li>
 *   <li>the frame sits below {@code firstColumn}, {@code lastColumn} and the header style of the column, so a
 *       style at those levels that sets a border side overrides the frame on that side;</li>
 *   <li>the header style of a column is the most specific level.</li>
 * </ul>
 *
 * @see BodyStyles
 * @see ColumnStyles
 * @see cloud.baldilorenzo.sheetsmith.annotation
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface HeaderStyles {

    /**
     * Style of every header cell. Level 2 of the header cascade, just above the preset.
     *
     * @return the style name; empty, the default, means no style
     */
    String base() default "";

    /**
     * Style of the header cell of the first column. Level 4 of the header cascade, applied after
     * {@link #lastColumn()}, so it wins when the table has one column.
     *
     * @return the style name; empty, the default, means no style
     */
    String firstColumn() default "";

    /**
     * Style of the header cell of the last column. Level 4 of the header cascade, applied before
     * {@link #firstColumn()}.
     *
     * @return the style name; empty, the default, means no style
     */
    String lastColumn() default "";
}
