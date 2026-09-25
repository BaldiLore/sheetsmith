package cloud.baldilorenzo.sheetsmith.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class that only holds shared {@link ExcelStyle} declarations.
 * <p>
 * Only classes carrying this annotation can be referenced in {@link ExcelSheet#styleSheets()}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelStyleSheet {
}
