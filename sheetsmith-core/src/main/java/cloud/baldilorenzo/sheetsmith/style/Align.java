package cloud.baldilorenzo.sheetsmith.style;

/**
 * Horizontal alignment of the content of a cell, the value of
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle#align()}.
 * <p>
 * Mirrors {@link org.apache.poi.ss.usermodel.HorizontalAlignment}: every constant other than {@link #INHERIT} has
 * the same name as a constant of that enum, and every constant of that enum has a counterpart here.
 * {@link #INHERIT}, the default of the style attribute, means that the attribute is not set.
 *
 * @since 1.0.0
 */
public enum Align {

    /** Not set: keeps the value of the lower cascade level. */
    INHERIT,

    /** Excel default alignment: text to the left, numbers and dates to the right. */
    GENERAL,

    /** Content aligned to the left edge of the cell. */
    LEFT,

    /** Content centred horizontally in the cell. */
    CENTER,

    /** Content aligned to the right edge of the cell. */
    RIGHT,

    /** Content repeated to fill the width of the cell. */
    FILL,

    /** Text wrapped and spaced so that each line reaches both edges of the cell. */
    JUSTIFY,

    /** Content centred across adjacent cells that use this alignment, without merging them. */
    CENTER_SELECTION,

    /** Text wrapped, with the words of each line spread evenly across the width of the cell. */
    DISTRIBUTED
}
