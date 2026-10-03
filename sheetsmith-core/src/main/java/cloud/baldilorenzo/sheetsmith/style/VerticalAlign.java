package cloud.baldilorenzo.sheetsmith.style;

/**
 * Vertical alignment of the content of a cell, the value of
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle#verticalAlign()}.
 * <p>
 * Mirrors {@link org.apache.poi.ss.usermodel.VerticalAlignment}: every constant other than {@link #INHERIT} has
 * the same name as a constant of that enum, and every constant of that enum has a counterpart here.
 * {@link #INHERIT}, the default of the style attribute, means that the attribute is not set.
 *
 * @since 1.0.0
 */
public enum VerticalAlign {

    /** Not set: keeps the value of the lower cascade level. */
    INHERIT,

    /** Content aligned to the top edge of the cell. */
    TOP,

    /** Content centred vertically in the cell. */
    CENTER,

    /** Content aligned to the bottom edge of the cell, the Excel default. */
    BOTTOM,

    /** Lines of wrapped text spaced so that they fill the height of the cell. */
    JUSTIFY,

    /** Lines of wrapped text spread evenly across the height of the cell. */
    DISTRIBUTED
}
