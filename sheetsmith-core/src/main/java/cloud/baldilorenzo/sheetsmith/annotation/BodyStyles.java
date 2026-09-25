package cloud.baldilorenzo.sheetsmith.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Style slots of the data rows at table level. Every attribute is the name of an {@link ExcelStyle}; empty means
 * no style. Data rows are numbered from 1, so the first row is odd.
 * <p>
 * Usable only as the value of {@link ExcelSheet#body()}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface BodyStyles {

    /**
     * Style applied to every data cell.
     *
     * @return the style name
     */
    String base() default "";

    /**
     * Style applied to the cells of even data rows.
     *
     * @return the style name
     */
    String even() default "";

    /**
     * Style applied to the cells of odd data rows.
     *
     * @return the style name
     */
    String odd() default "";

    /**
     * Style applied to the cells of the first data row.
     *
     * @return the style name
     */
    String firstRow() default "";

    /**
     * Style applied to the cells of the last data row.
     *
     * @return the style name
     */
    String lastRow() default "";

    /**
     * Style applied to the data cells of the first column.
     *
     * @return the style name
     */
    String firstColumn() default "";

    /**
     * Style applied to the data cells of the last column.
     *
     * @return the style name
     */
    String lastColumn() default "";
}
