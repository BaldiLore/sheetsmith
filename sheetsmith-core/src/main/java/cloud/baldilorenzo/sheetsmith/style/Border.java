package cloud.baldilorenzo.sheetsmith.style;

/**
 * Line of a cell border, the value of the border attributes of
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle} and of
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet#outerBorder()}.
 * <p>
 * Mirrors {@link org.apache.poi.ss.usermodel.BorderStyle}: every constant other than {@link #INHERIT} has the same
 * name as a constant of that enum, and every constant of that enum has a counterpart here. {@link #INHERIT}, the
 * default of the attributes, means that the attribute is not set; on
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet#outerBorder()} it means no frame.
 *
 * @since 1.0.0
 */
public enum Border {

    /** Not set: keeps the value of the lower cascade level. */
    INHERIT,

    /** No line, removing a line set by a lower cascade level. */
    NONE,

    /** Thin solid line. */
    THIN,

    /** Medium weight solid line. */
    MEDIUM,

    /** Thin dashed line. */
    DASHED,

    /** Thin dotted line. */
    DOTTED,

    /** Thick solid line. */
    THICK,

    /** Double thin line. */
    DOUBLE,

    /** Hairline, the thinnest line available. */
    HAIR,

    /** Medium weight dashed line. */
    MEDIUM_DASHED,

    /** Thin line of alternating dashes and dots. */
    DASH_DOT,

    /** Medium weight line of alternating dashes and dots. */
    MEDIUM_DASH_DOT,

    /** Thin line of dashes, each followed by two dots. */
    DASH_DOT_DOT,

    /** Medium weight line of dashes, each followed by two dots. */
    MEDIUM_DASH_DOT_DOT,

    /** Medium weight line of slanted dashes and dots. */
    SLANTED_DASH_DOT
}
