package cloud.baldilorenzo.sheetsmith.style;

/**
 * Fill pattern of a cell, the value of {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle#fillPattern()}.
 * <p>
 * Mirrors {@link org.apache.poi.ss.usermodel.FillPatternType}: every constant other than {@link #INHERIT} has the
 * same name as a constant of that enum, and every constant of that enum has a counterpart here. {@link #INHERIT},
 * the default of the style attribute, means that the attribute is not set.
 * <p>
 * Patterns draw the fill colour of the style over its background colour. A plain background needs no pattern: a
 * fill colour without a pattern gives a solid fill.
 *
 * @since 1.0.0
 */
public enum Fill {

    /** Not set: keeps the value of the lower cascade level. */
    INHERIT,

    /** No fill: the cell has no background. */
    NO_FILL,

    /** Solid fill in the fill colour. */
    SOLID_FOREGROUND,

    /** Dots covering half of the cell, the Excel "50% grey" pattern. */
    FINE_DOTS,

    /** Dense dots covering three quarters of the cell, the Excel "75% grey" pattern. */
    ALT_BARS,

    /** Sparse dots covering a quarter of the cell, the Excel "25% grey" pattern. */
    SPARSE_DOTS,

    /** Thick horizontal stripes. */
    THICK_HORZ_BANDS,

    /** Thick vertical stripes. */
    THICK_VERT_BANDS,

    /** Thick diagonal stripes running from top left to bottom right. */
    THICK_BACKWARD_DIAG,

    /** Thick diagonal stripes running from bottom left to top right. */
    THICK_FORWARD_DIAG,

    /** Thick horizontal and vertical crosshatch. */
    BIG_SPOTS,

    /** Thick diagonal crosshatch. */
    BRICKS,

    /** Thin horizontal stripes. */
    THIN_HORZ_BANDS,

    /** Thin vertical stripes. */
    THIN_VERT_BANDS,

    /** Thin diagonal stripes running from top left to bottom right. */
    THIN_BACKWARD_DIAG,

    /** Thin diagonal stripes running from bottom left to top right. */
    THIN_FORWARD_DIAG,

    /** Thin horizontal and vertical crosshatch. */
    SQUARES,

    /** Thin diagonal crosshatch. */
    DIAMONDS,

    /** Very sparse dots, the Excel "12.5% grey" pattern. */
    LESS_DOTS,

    /** The sparsest dots, the Excel "6.25% grey" pattern. */
    LEAST_DOTS
}
