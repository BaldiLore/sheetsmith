package cloud.baldilorenzo.sheetsmith.style;

/**
 * Line style of a cell border.
 * <p>
 * Mirrors {@link org.apache.poi.ss.usermodel.BorderStyle}, plus {@link #INHERIT}.
 */
public enum Border {

    /** Not set at this level: the value of the level below is kept. */
    INHERIT,

    /** No border. */
    NONE,

    /** Thin line. */
    THIN,

    /** Medium line. */
    MEDIUM,

    /** Dashed line. */
    DASHED,

    /** Dotted line. */
    DOTTED,

    /** Thick line. */
    THICK,

    /** Double line. */
    DOUBLE,

    /** Hair line, the thinnest available. */
    HAIR,

    /** Medium dashed line. */
    MEDIUM_DASHED,

    /** Dash-dot line. */
    DASH_DOT,

    /** Medium dash-dot line. */
    MEDIUM_DASH_DOT,

    /** Dash-dot-dot line. */
    DASH_DOT_DOT,

    /** Medium dash-dot-dot line. */
    MEDIUM_DASH_DOT_DOT,

    /** Slanted dash-dot line. */
    SLANTED_DASH_DOT
}
