package cloud.baldilorenzo.sheetsmith.autoconfigure;

import cloud.baldilorenzo.sheetsmith.DocumentProperties;
import cloud.baldilorenzo.sheetsmith.SheetData;
import cloud.baldilorenzo.sheetsmith.Sheetsmith;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SheetsmithDocumentPropertiesTest {

    @ExcelSheet
    record Line(@ExcelColumn(header = "Name", order = 1) String name) {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SheetsmithAutoConfiguration.class));

    @Test
    void defaultsAreTheStandardDocumentProperties() {
        runner.run(context -> {
            assertThat(context.getBean(SheetsmithProperties.class).toDocumentProperties())
                    .isEqualTo(DocumentProperties.standard());

            Written properties = propertiesOf(context.getBean(Sheetsmith.class));
            assertThat(properties.author()).isEqualTo("sheetsmith");
            assertThat(properties.application()).isEqualTo("sheetsmith");
        });
    }

    @Test
    void propertiesAreBoundAndWritten() {
        runner.withPropertyValues(
                        "sheetsmith.document.author=Example Ltd",
                        "sheetsmith.document.application=Billing")
                .run(context -> {
                    Written properties = propertiesOf(context.getBean(Sheetsmith.class));
                    assertThat(properties.author()).isEqualTo("Example Ltd");
                    assertThat(properties.application()).isEqualTo("Billing");
                });
    }

    @Test
    void emptyPropertiesLeaveTheValuesOut() {
        runner.withPropertyValues("sheetsmith.document.author=", "sheetsmith.document.application=")
                .run(context -> {
                    assertThat(context.getBean(SheetsmithProperties.class).toDocumentProperties())
                            .isEqualTo(new DocumentProperties("", ""));

                    Written properties = propertiesOf(context.getBean(Sheetsmith.class));
                    assertThat(properties.author()).isNull();
                    assertThat(properties.application()).isNull();
                });
    }

    @Test
    void configurationMetadataDescribesTheDocumentProperties() throws IOException {
        String metadata;
        try (InputStream in = new ClassPathResource("META-INF/spring-configuration-metadata.json").getInputStream()) {
            metadata = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertThat(metadata).contains("\"sheetsmith.document.author\"", "\"sheetsmith.document.application\"",
                "\"defaultValue\": \"sheetsmith\"");
    }

    /** Reads the author and the application of a generated file. */
    private static Written propertiesOf(Sheetsmith sheetsmith) throws IOException {
        byte[] file = sheetsmith.generate(List.of(SheetData.of("Lines", Line.class, List.of(new Line("a")))));
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file))) {
            POIXMLProperties properties = workbook.getProperties();
            return new Written(properties.getCoreProperties().getCreator(),
                    properties.getExtendedProperties().getApplication());
        }
    }

    private record Written(String author, String application) {
    }
}