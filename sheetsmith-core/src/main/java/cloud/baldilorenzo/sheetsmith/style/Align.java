package cloud.baldilorenzo.sheetsmith.style;

/**
 * Horizontal alignment of the content of a cell.
 * <p>
 * Mirrors {@link org.apache.poi.ss.usermodel.HorizontalAlignment}, plus {@link #INHERIT}.
 */
public enum Align {

    /** Not set at this level: the value of the level below is kept. */
    INHERIT,

    /** Text left, numbers right, as Excel does by default. */
    GENERAL,

    /** Left aligned. */
    LEFT,

    /** Centred. */
    CENTER,

    /** Right aligned. */
    RIGHT,

    /** Content repeated to fill the cell width. */
    FILL,

    /** Justified, wrapping text on several lines. */
    JUSTIFY,

    /** Centred across the selection of adjacent cells. */
    CENTER_SELECTION,

    /** Distributed, with each line spread across the cell width. */
    DISTRIBUTED
}
