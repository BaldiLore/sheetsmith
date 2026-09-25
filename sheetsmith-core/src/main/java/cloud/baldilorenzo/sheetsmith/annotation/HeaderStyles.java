package cloud.baldilorenzo.sheetsmith.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Style slots of the header row. Every attribute is the name of an {@link ExcelStyle}; empty means no style.
 * <p>
 * Usable only as the value of {@link ExcelSheet#header()}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface HeaderStyles {

    /**
     * Style applied to every header cell.
     *
     * @return the style name
     */
    String base() default "";

    /**
     * Style applied to the header cell of the first column.
     *
     * @return the style name
     */
    String firstColumn() default "";

    /**
     * Style applied to the header cell of the last column.
     *
     * @return the style name
     */
    String lastColumn() default "";
}
