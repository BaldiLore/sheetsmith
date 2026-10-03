/**
 * Style enums used as attribute values of the sheetsmith annotations.
 * <p>
 * {@link cloud.baldilorenzo.sheetsmith.style.Align}, {@link cloud.baldilorenzo.sheetsmith.style.VerticalAlign},
 * {@link cloud.baldilorenzo.sheetsmith.style.Border}, {@link cloud.baldilorenzo.sheetsmith.style.Fill} and
 * {@link cloud.baldilorenzo.sheetsmith.style.Underline} mirror an Apache POI enum: they contain the constant
 * {@code INHERIT} plus one constant for each POI constant, with identical names.
 * {@link cloud.baldilorenzo.sheetsmith.style.Script} and {@link cloud.baldilorenzo.sheetsmith.style.Toggle} cover
 * the attributes that POI does not model as an enum. In every one of these enums, {@code INHERIT} is the default of
 * the style attributes and means that the attribute is not set, so that it keeps the value of the lower cascade
 * level.
 * <p>
 * {@link cloud.baldilorenzo.sheetsmith.style.TablePreset} lists the ready-made table styles.
 *
 * @see cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle
 */
package cloud.baldilorenzo.sheetsmith.style;
