package cloud.baldilorenzo.sheetsmith.annotation;

import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as exportable to an Excel sheet and configures the sheet.
 * <p>
 * Mandatory on every class whose instances are exported.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelSheet {

    /**
     * Title shown above the table, in a cell merged across all columns.
     *
     * @return the title; empty means no title row
     */
    String title() default "";

    /**
     * Name of the style applied to the title. Can be set only when {@link #title()} is set.
     *
     * @return the style name; empty means no style
     */
    String titleStyle() default "";

    /**
     * Ready-made table style.
     *
     * @return the preset; {@link TablePreset#INHERIT} uses the application default
     */
    TablePreset preset() default TablePreset.INHERIT;

    /**
     * Accent colour from which the preset derives its colours, as {@code #RRGGBB} or the name of an
     * {@link org.apache.poi.ss.usermodel.IndexedColors} constant.
     *
     * @return the accent colour; empty uses the application default
     */
    String accentColor() default "";

    /**
     * Style sheet classes, annotated with {@link ExcelStyleSheet}, whose styles can be referenced by this class.
     *
     * @return the shared style sheets
     */
    Class<?>[] styleSheets() default {};

    /**
     * Whether the rows up to and including the header stay visible while scrolling.
     *
     * @return true to freeze the header
     */
    boolean freezeHeader() default true;

    /**
     * Whether an auto-filter covers the header and the data rows.
     *
     * @return true to add an auto-filter
     */
    boolean autoFilter() default false;

    /**
     * Whether columns without an explicit width are sized to their content.
     *
     * @return true to auto-size columns
     */
    boolean autoSizeColumns() default true;

    /**
     * Line of the frame drawn around the header and the data rows. The title is outside the frame.
     *
     * @return the frame line; {@link Border#INHERIT} means no frame
     */
    Border outerBorder() default Border.INHERIT;

    /**
     * Colour of the frame, as {@code #RRGGBB} or the name of an
     * {@link org.apache.poi.ss.usermodel.IndexedColors} constant.
     *
     * @return the frame colour; empty means automatic
     */
    String outerBorderColor() default "";

    /**
     * Styles of the header row.
     *
     * @return the header slots
     */
    HeaderStyles header() default @HeaderStyles;

    /**
     * Styles of the data rows at table level.
     *
     * @return the data slots
     */
    BodyStyles body() default @BodyStyles;
}
