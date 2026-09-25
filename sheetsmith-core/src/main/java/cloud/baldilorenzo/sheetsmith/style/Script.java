package cloud.baldilorenzo.sheetsmith.style;

/**
 * Vertical position of the text of a font: normal, superscript or subscript.
 * <p>
 * Maps to {@link org.apache.poi.ss.usermodel.Font#SS_NONE}, {@link org.apache.poi.ss.usermodel.Font#SS_SUPER}
 * and {@link org.apache.poi.ss.usermodel.Font#SS_SUB}.
 */
public enum Script {

    /** Not set at this level: the value of the level below is kept. */
    INHERIT,

    /** Normal text. */
    NONE,

    /** Superscript. */
    SUPER,

    /** Subscript. */
    SUB
}
