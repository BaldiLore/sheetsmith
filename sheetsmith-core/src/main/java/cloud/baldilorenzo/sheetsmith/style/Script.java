package cloud.baldilorenzo.sheetsmith.style;

/**
 * Vertical position of the text of a cell: normal, superscript or subscript. The value of
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle#script()}.
 * <p>
 * Apache POI has no enum for this attribute: the constants map to
 * {@link org.apache.poi.ss.usermodel.Font#SS_NONE}, {@link org.apache.poi.ss.usermodel.Font#SS_SUPER} and
 * {@link org.apache.poi.ss.usermodel.Font#SS_SUB}. {@link #INHERIT}, the default of the style attribute, means that
 * the attribute is not set.
 *
 * @since 1.0.0
 */
public enum Script {

    /** Not set: keeps the value of the lower cascade level. */
    INHERIT,

    /** Normal text on the baseline, removing a superscript or subscript set by a lower cascade level. */
    NONE,

    /** Superscript: smaller text raised above the baseline. */
    SUPER,

    /** Subscript: smaller text lowered below the baseline. */
    SUB
}
