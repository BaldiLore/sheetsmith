package cloud.baldilorenzo.sheetsmith.style;

/**
 * Vertical alignment of the content of a cell.
 * <p>
 * Mirrors {@link org.apache.poi.ss.usermodel.VerticalAlignment}, plus {@link #INHERIT}.
 */
public enum VerticalAlign {

    /** Not set at this level: the value of the level below is kept. */
    INHERIT,

    /** Aligned to the top of the cell. */
    TOP,

    /** Centred vertically. */
    CENTER,

    /** Aligned to the bottom of the cell. */
    BOTTOM,

    /** Justified vertically. */
    JUSTIFY,

    /** Distributed vertically. */
    DISTRIBUTED
}
