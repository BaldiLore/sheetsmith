package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.annotationtype;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation type annotated with {@code @ExcelSheet}. Validating it would fail with V-02, but annotation types are
 * not validated at startup.
 */
@ExcelSheet
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExportedSheet {
}
