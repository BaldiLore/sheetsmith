package cloud.baldilorenzo.sheetsmith.annotation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Compiles small sources with the system Java compiler to check that the mandatory attributes of
 * {@link ExcelColumn} are enforced at compile time.
 */
class ExcelColumnCompilationTest {

    @TempDir
    Path output;

    @Test
    void compilesWhenHeaderAndOrderAreSet() {
        assertThat(compile("@ExcelColumn(header = \"Name\", order = 1)")).isEmpty();
    }

    @Test
    void compilerRejectsColumnWithoutOrder() {
        assertThat(compile("@ExcelColumn(header = \"Name\")"))
                .singleElement().asString().contains("order");
    }

    @Test
    void compilerRejectsColumnWithoutHeader() {
        assertThat(compile("@ExcelColumn(order = 1)"))
                .singleElement().asString().contains("header");
    }

    private List<String> compile(String annotation) {
        String source = """
                import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;

                class Row {
                    %s
                    String name;
                }
                """.formatted(annotation);
        JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///Row.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("tests must run on a JDK").isNotNull();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        List<String> options = List.of(
                "-classpath", System.getProperty("java.class.path"),
                "-d", output.toString(),
                "-proc:none");
        compiler.getTask(null, null, diagnostics, options, null, List.of(file)).call();

        return diagnostics.getDiagnostics().stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                .map(d -> d.getMessage(Locale.ROOT))
                .toList();
    }
}
