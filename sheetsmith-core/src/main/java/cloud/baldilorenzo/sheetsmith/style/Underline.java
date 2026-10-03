package cloud.baldilorenzo.sheetsmith.style;

/**
 * Underline of the text of a cell, the value of
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle#underline()}.
 * <p>
 * Mirrors {@link org.apache.poi.ss.usermodel.FontUnderline}: every constant other than {@link #INHERIT} has the
 * same name as a constant of that enum, and every constant of that enum has a counterpart here. {@link #INHERIT},
 * the default of the style attribute, means that the attribute is not set.
 *
 * @since 1.0.0
 */
public enum Underline {

    /** Not set: keeps the value of the lower cascade level. */
    INHERIT,

    /** Single line under the text. */
    SINGLE,

    /** Double line under the text. */
    DOUBLE,

    /** Single accounting underline: a line placed lower, spanning the whole width of the cell. */
    SINGLE_ACCOUNTING,

    /** Double accounting underline: two lines placed lower, spanning the whole width of the cell. */
    DOUBLE_ACCOUNTING,

    /** No underline, removing an underline set by a lower cascade level. */
    NONE
}
