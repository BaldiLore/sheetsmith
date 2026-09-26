/**
 * Spring Boot auto-configuration of sheetsmith.
 * <p>
 * With the sheetsmith starter on the classpath, a {@link cloud.baldilorenzo.sheetsmith.Sheetsmith} bean is available
 * without any configuration: inject it and call {@code generate}. The main types are:
 * <ul>
 *   <li>{@link cloud.baldilorenzo.sheetsmith.autoconfigure.SheetsmithAutoConfiguration}: registers the
 *       {@code Sheetsmith} bean, backs off when the application defines its own, and registers every converter bean
 *       as an application converter;</li>
 *   <li>{@link cloud.baldilorenzo.sheetsmith.autoconfigure.SheetsmithProperties}: the {@code sheetsmith.*}
 *       properties, for default formats, preset, accent colour and startup validation;</li>
 *   <li>{@link cloud.baldilorenzo.sheetsmith.autoconfigure.SpringConverterFactory}: creates field converters from
 *       the application context;</li>
 *   <li>{@link cloud.baldilorenzo.sheetsmith.autoconfigure.SheetsmithStartupValidator}: validates the sheet
 *       classes of the configured packages at startup.</li>
 * </ul>
 */
package cloud.baldilorenzo.sheetsmith.autoconfigure;
