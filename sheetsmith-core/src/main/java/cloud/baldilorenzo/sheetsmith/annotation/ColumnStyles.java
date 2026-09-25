package cloud.baldilorenzo.sheetsmith.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Style slots of the data cells of a single column. Every attribute is the name of an {@link ExcelStyle}; empty
 * means no style. Data rows are numbered from 1, so the first row is odd.
 * <p>
 * Usable only as the value of {@link ExcelColumn#styles()}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ColumnStyles {

    /**
     * Style applied to every data cell of the column.
     *
     * @return the style name
     */
    String base() default "";

    /**
     * Style applied to the cells of the column in even data rows.
     *
     * @return the style name
     */
    String even() default "";

    /**
     * Style applied to the cells of the column in odd data rows.
     *
     * @return the style name
     */
    String odd() default "";

    /**
     * Style applied to the cell of the column in the first data row.
     *
     * @return the style name
     */
    String firstRow() default "";

    /**
     * Style applied to the cell of the column in the last data row.
     *
     * @return the style name
     */
    String lastRow() default "";
}
