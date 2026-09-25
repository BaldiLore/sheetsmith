package cloud.baldilorenzo.sheetsmith.style;

/**
 * Ready-made table style derived from a single accent colour.
 */
public enum TablePreset {

    /** Not chosen on the class: the application default preset is used. */
    INHERIT,

    /** No preset: only the declared styles apply. */
    NONE,

    /** Light table: accent-coloured header text and borders, lightly tinted odd rows. */
    LIGHT,

    /** Medium table: accent-filled header, bordered cells, tinted odd rows. */
    MEDIUM,

    /** Dark table: dark header and accent-filled alternating rows. */
    DARK
}
