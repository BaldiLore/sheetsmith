/**
 * Converters: turn field values into values that sheetsmith writes to a cell.
 * <p>
 * The main types are:
 * <ul>
 *   <li>{@link cloud.baldilorenzo.sheetsmith.convert.CellConverter}: converts a value of one type; describes the
 *       converter contract, field and application converters, and the resolution order;</li>
 *   <li>{@link cloud.baldilorenzo.sheetsmith.convert.CellValue}: the value written to a cell, and the reference of
 *       the known Excel limitations;</li>
 *   <li>{@link cloud.baldilorenzo.sheetsmith.convert.ConversionContext}: where a value is being written;</li>
 *   <li>{@link cloud.baldilorenzo.sheetsmith.convert.CellConverterFactory}: creates the field converters.</li>
 * </ul>
 * Text, numbers, booleans, enums, dates, common identifiers, a few optional values and links
 * ({@link cloud.baldilorenzo.sheetsmith.value.Hyperlink}) have built-in converters, listed in
 * {@link cloud.baldilorenzo.sheetsmith.convert.CellConverter}; any other type needs a converter.
 */
package cloud.baldilorenzo.sheetsmith.convert;
