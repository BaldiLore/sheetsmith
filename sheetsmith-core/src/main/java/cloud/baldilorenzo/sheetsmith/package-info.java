/**
 * Entry point of sheetsmith: annotation-driven generation of Excel files ({@code .xlsx}), built on Apache POI.
 * <p>
 * The main types are:
 * <ul>
 *   <li>{@link cloud.baldilorenzo.sheetsmith.Sheetsmith}: the generator, created with its builder and shared by the
 *       whole application;</li>
 *   <li>{@link cloud.baldilorenzo.sheetsmith.SheetData}: one sheet to generate, with its name, sheet class and
 *       data;</li>
 *   <li>{@link cloud.baldilorenzo.sheetsmith.SheetsmithDefaults}: the application defaults, and the reference of
 *       the Excel format syntax;</li>
 *   <li>{@link cloud.baldilorenzo.sheetsmith.SheetsmithException} and its two kinds,
 *       {@link cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException}, which lists the validation rules,
 *       and {@link cloud.baldilorenzo.sheetsmith.SheetsmithGenerationException}.</li>
 * </ul>
 * Sheet classes are described with the annotations of {@link cloud.baldilorenzo.sheetsmith.annotation}, whose
 * attributes use the enums of {@link cloud.baldilorenzo.sheetsmith.style}. Values of types that are not written
 * natively are handled by the converters of {@link cloud.baldilorenzo.sheetsmith.convert}.
 * <p>
 * This package has no dependency on Spring and can be used in any Java application.
 */
package cloud.baldilorenzo.sheetsmith;
