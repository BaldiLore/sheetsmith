package cloud.baldilorenzo.sheetsmith.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container of repeated {@link ExcelStyle} annotations.
 * <p>
 * The compiler uses it when {@link ExcelStyle} is repeated on a class. It is not meant to be written directly:
 * repeat {@link ExcelStyle} instead.
 *
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelStyles {

    /**
     * Returns the contained styles, in declaration order.
     *
     * @return the styles
     */
    ExcelStyle[] value();
}
