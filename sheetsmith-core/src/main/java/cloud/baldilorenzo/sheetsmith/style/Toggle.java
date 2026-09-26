package cloud.baldilorenzo.sheetsmith.style;

/**
 * Three-state boolean for the yes and no attributes of
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle}, such as {@code bold} or {@code wrapText}.
 * <p>
 * A plain {@code boolean} cannot express "not set", which the style cascade needs so that a level can leave an
 * attribute to the levels below. {@link #INHERIT}, the default of the attributes, is that third state.
 *
 * @since 1.0.0
 */
public enum Toggle {

    /** Not set: keeps the value of the lower cascade level. */
    INHERIT,

    /** Enables the attribute, for example makes the text bold. */
    TRUE,

    /** Disables the attribute, overriding a lower cascade level that enables it. */
    FALSE
}
