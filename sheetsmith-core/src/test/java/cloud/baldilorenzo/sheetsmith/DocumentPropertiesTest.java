package cloud.baldilorenzo.sheetsmith;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class DocumentPropertiesTest {

    @ExcelSheet
    record Line(@ExcelColumn(header = "Name", order = 1) String name) {
    }

    @Test
    void standardValues() {
        assertThat(DocumentProperties.standard()).isEqualTo(new DocumentProperties("sheetsmith", "sheetsmith"));
    }

    @Test
    void rejectsNullValues() {
        assertThatNullPointerException().isThrownBy(() -> new DocumentProperties(null, "app"))
                .withMessage("author");
        assertThatNullPointerException().isThrownBy(() -> new DocumentProperties("author", null))
                .withMessage("application");
        assertThatNullPointerException().isThrownBy(() -> Sheetsmith.builder().documentProperties(null));
    }

    @Test
    void standardPropertiesReplaceTheOnesOfApachePoi() throws IOException {
        Written properties = propertiesOf(Sheetsmith.builder().build());

        assertThat(properties.author()).isEqualTo("sheetsmith");
        assertThat(properties.application()).isEqualTo("sheetsmith");
    }

    @Test
    void configuredPropertiesAreWritten() throws IOException {
        Written properties = propertiesOf(Sheetsmith.builder()
                .documentProperties(new DocumentProperties("Example Ltd", "Billing"))
                .build());

        assertThat(properties.author()).isEqualTo("Example Ltd");
        assertThat(properties.application()).isEqualTo("Billing");
    }

    @Test
    void emptyPropertiesAreLeftOut() throws IOException {
        Written properties = propertiesOf(Sheetsmith.builder()
                .documentProperties(new DocumentProperties("", ""))
                .build());

        assertThat(properties.author()).isNull();
        assertThat(properties.application()).isNull();
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