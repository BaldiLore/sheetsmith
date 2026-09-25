package cloud.baldilorenzo.sheetsmith.style;

/**
 * Underline style of a font.
 * <p>
 * Mirrors {@link org.apache.poi.ss.usermodel.FontUnderline}, plus {@link #INHERIT}.
 */
public enum Underline {

    /** Not set at this level: the value of the level below is kept. */
    INHERIT,

    /** Single underline. */
    SINGLE,

    /** Double underline. */
    DOUBLE,

    /** Single accounting underline, spanning the whole cell width. */
    SINGLE_ACCOUNTING,

    /** Double accounting underline, spanning the whole cell width. */
    DOUBLE_ACCOUNTING,

    /** No underline. */
    NONE
}
