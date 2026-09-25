package cloud.baldilorenzo.sheetsmith.style;

/**
 * Three-state boolean for style attributes: set to true, set to false, or not set.
 */
public enum Toggle {

    /** Not set at this level: the value of the level below is kept. */
    INHERIT,

    /** Enabled. */
    TRUE,

    /** Disabled. */
    FALSE
}
