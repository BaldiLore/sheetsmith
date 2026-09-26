package cloud.baldilorenzo.sheetsmith.style;

/**
 * Ready-made table style, generated from one accent colour.
 * <p>
 * A preset styles the title, the header and the data rows of a table. Lighter and darker tones are derived from
 * the accent colour automatically, and text on a filled background is white or black, whichever contrasts better.
 * In every preset the title is bold, 14 pt, with dark accent text.
 * <p>
 * A sheet class chooses its preset and accent colour with
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet#preset()} and
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet#accentColor()}; the application defaults, set with
 * {@link cloud.baldilorenzo.sheetsmith.SheetsmithDefaults}, apply to classes that choose none. The preset is the
 * lowest level of the style cascade: every declared style overrides it attribute by attribute, so a preset can be
 * adjusted with ordinary named styles.
 *
 * @since 1.0.0
 */
public enum TablePreset {

    /**
     * Uses the application default preset, {@link #NONE} unless configured otherwise. Valid only on a sheet class,
     * not as the application default itself.
     */
    INHERIT,

    /** Applies no preset: only the declared styles apply. */
    NONE,

    /**
     * Light table: header with bold dark accent text and a medium accent line below it; a thin light accent line
     * below each data row; very light accent fill on odd rows and no fill on even rows.
     */
    LIGHT,

    /**
     * Medium table: header with accent fill and bold contrasting text; a thin light accent grid around every data
     * cell; light accent fill on odd rows and no fill on even rows.
     */
    MEDIUM,

    /**
     * Dark table: header with dark accent fill and bold contrasting text; no lines; accent fill with contrasting
     * text on odd rows, and darker accent fill with contrasting text on even rows.
     */
    DARK
}
