package cloud.baldilorenzo.sheetsmith.annotation;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exports a field as a column. Only annotated fields are exported.
 * <p>
 * On a record component the annotation propagates to the component field.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelColumn {

    /**
     * Text of the header cell. Must not be blank.
     *
     * @return the header text
     */
    String header();

    /**
     * Position of the column: columns appear left to right in ascending order. Values must be unique within
     * the class but need not be consecutive.
     *
     * @return the order value
     */
    int order();

    /**
     * Width of the column in characters, from 1 to 255.
     *
     * @return the width; {@link ExcelStyle#UNSET} means automatic
     */
    int width() default ExcelStyle.UNSET;

    /**
     * Excel data format of the data cells of this column, for example {@code #,##0.00}.
     *
     * @return the format; empty means none
     */
    String format() default "";

    /**
     * Converter used for the values of this field, instead of the one registered for its type.
     *
     * @return the converter class; {@link CellConverter.None} means none
     */
    Class<? extends CellConverter<?>> converter() default CellConverter.None.class;

    /**
     * Name of the style applied to the header cell of this column.
     *
     * @return the style name; empty means no style
     */
    String headerStyle() default "";

    /**
     * Styles of the data cells of this column.
     *
     * @return the column slots
     */
    ColumnStyles styles() default @ColumnStyles;
}
