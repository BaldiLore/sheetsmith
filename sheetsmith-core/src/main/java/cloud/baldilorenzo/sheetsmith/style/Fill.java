package cloud.baldilorenzo.sheetsmith.style;

/**
 * Fill pattern of a cell.
 * <p>
 * Mirrors {@link org.apache.poi.ss.usermodel.FillPatternType}, plus {@link #INHERIT}.
 */
public enum Fill {

    /** Not set at this level: the value of the level below is kept. */
    INHERIT,

    /** No fill. */
    NO_FILL,

    /** Solid fill with the foreground colour. */
    SOLID_FOREGROUND,

    /** Fine dots. */
    FINE_DOTS,

    /** Alternating bars. */
    ALT_BARS,

    /** Sparse dots. */
    SPARSE_DOTS,

    /** Thick horizontal bands. */
    THICK_HORZ_BANDS,

    /** Thick vertical bands. */
    THICK_VERT_BANDS,

    /** Thick backward diagonals. */
    THICK_BACKWARD_DIAG,

    /** Thick forward diagonals. */
    THICK_FORWARD_DIAG,

    /** Big spots. */
    BIG_SPOTS,

    /** Bricks. */
    BRICKS,

    /** Thin horizontal bands. */
    THIN_HORZ_BANDS,

    /** Thin vertical bands. */
    THIN_VERT_BANDS,

    /** Thin backward diagonals. */
    THIN_BACKWARD_DIAG,

    /** Thin forward diagonals. */
    THIN_FORWARD_DIAG,

    /** Squares. */
    SQUARES,

    /** Diamonds. */
    DIAMONDS,

    /** Less dots. */
    LESS_DOTS,

    /** Least dots. */
    LEAST_DOTS
}
