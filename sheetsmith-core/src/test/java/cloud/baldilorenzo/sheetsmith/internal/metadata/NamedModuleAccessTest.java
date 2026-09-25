package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Exported classes living in a named module that exports its package without opening it: fields can only be read
 * through public getters.
 */
class NamedModuleAccessTest {

    private static final String MODULE = "fixture.closed";

    @TempDir
    static Path workDir;

    private static ClassLoader loader;

    @BeforeAll
    static void compileAndLoadModule() throws Exception {
        Path sources = workDir.resolve("src");
        Path classes = workDir.resolve("classes");
        Path pkg = Files.createDirectories(sources.resolve("fixture/closed"));
        Files.writeString(sources.resolve("module-info.java"), "module " + MODULE + " { exports fixture.closed; }");
        Files.writeString(pkg.resolve("Hidden.java"), """
                package fixture.closed;

                import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
                import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;

                @ExcelSheet
                public class Hidden {
                    @ExcelColumn(header = "Secret", order = 1)
                    private String secret = "s";
                }
                """);
        Files.writeString(pkg.resolve("WithGetter.java"), """
                package fixture.closed;

                import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
                import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;

                @ExcelSheet
                public class WithGetter {
                    @ExcelColumn(header = "Value", order = 1)
                    private String value = "v";

                    public String getValue() {
                        return value;
                    }
                }
                """);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int result = compiler.run(null, output, output,
                "-d", classes.toString(),
                "-classpath", System.getProperty("java.class.path"),
                "--add-reads", MODULE + "=ALL-UNNAMED",
                "-proc:none",
                sources.resolve("module-info.java").toString(),
                pkg.resolve("Hidden.java").toString(),
                pkg.resolve("WithGetter.java").toString());
        assertThat(result).as(output.toString()).isZero();

        Configuration configuration = ModuleLayer.boot().configuration()
                .resolve(ModuleFinder.of(classes), ModuleFinder.of(), Set.of(MODULE));
        ModuleLayer layer = ModuleLayer.defineModulesWithOneLoader(configuration, List.of(ModuleLayer.boot()),
                NamedModuleAccessTest.class.getClassLoader()).layer();
        loader = layer.findLoader(MODULE);
    }

    @Test
    void v17FieldOfAPackageThatIsNotOpenIsNotAccessible() throws Exception {
        Class<?> hidden = loader.loadClass("fixture.closed.Hidden");

        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> new MetadataExtractor().extract(hidden));

        assertThat(exception.errors()).singleElement().satisfies(error -> {
            assertThat(error).extracting(ConfigurationError::code, ConfigurationError::element)
                    .containsExactly("V-17", "secret");
            assertThat(error.message()).contains("opens fixture.closed;");
        });
    }

    @Test
    void publicGetterOfAnExportedPackageIsAccessible() throws Exception {
        Class<?> withGetter = loader.loadClass("fixture.closed.WithGetter");

        SheetMetadata metadata = new MetadataExtractor().extract(withGetter);

        Object instance = withGetter.getConstructor().newInstance();
        assertThat(metadata.columns().get(0).accessor().get(instance)).isEqualTo("v");
    }
}
