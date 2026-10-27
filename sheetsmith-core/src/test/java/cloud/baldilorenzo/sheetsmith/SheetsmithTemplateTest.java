package cloud.baldilorenzo.sheetsmith;

import cloud.baldilorenzo.sheetsmith.fixtures.InvalidSheets;
import cloud.baldilorenzo.sheetsmith.fixtures.InvalidTemplates;
import cloud.baldilorenzo.sheetsmith.fixtures.TemplateSheets;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.tuple;

class SheetsmithTemplateTest {

    private final Sheetsmith sheetsmith = Sheetsmith.builder().build();

    @Test
    void validateAcceptsAValidTemplate() {
        assertThatCode(() -> sheetsmith.validate(TemplateSheets.FullTemplate.class)).doesNotThrowAnyException();
    }

    @Test
    void validateReportsTheErrorsOfAnInvalidTemplateOnItsOwn() {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> sheetsmith.validate(InvalidTemplates.V13InvalidColors.class));

        assertThat(exception.errors())
                .extracting(ConfigurationError::code, ConfigurationError::type, ConfigurationError::element)
                .containsExactly(
                        tuple("V-13", InvalidTemplates.V13InvalidColors.class, "@ExcelSheetTemplate"),
                        tuple("V-13", InvalidTemplates.V13InvalidColors.class, "@ExcelSheetTemplate"));
    }

    @Test
    void validateReportsAClassWithNeitherAnnotation() {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> sheetsmith.validate(InvalidSheets.V01MissingSheet.class));

        assertThat(exception.errors())
                .extracting(ConfigurationError::code, ConfigurationError::type)
                .containsExactly(tuple("V-01", InvalidSheets.V01MissingSheet.class));
    }

    @Test
    void validateChecksASheetClassTogetherWithItsTemplate() {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> sheetsmith.validate(InvalidTemplates.UsesTemplateWithInvalidColors.class));

        assertThat(exception.errors()).extracting(ConfigurationError::code, ConfigurationError::type)
                .containsExactly(
                        tuple("V-13", InvalidTemplates.V13InvalidColors.class),
                        tuple("V-13", InvalidTemplates.V13InvalidColors.class));
    }

    @Test
    void validateChecksAClassAnnotatedAsSheetAndTemplateAsASheetClass() {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> sheetsmith.validate(InvalidTemplates.V23TemplateAndSheet.class));

        assertThat(exception.errors()).extracting(ConfigurationError::code).containsExactly("V-23");
    }

    @Test
    void generateRejectsATemplate() {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> sheetsmith.generate(List.of(SheetData.of("T", TemplateSheets.FullTemplate.class, List.of()))));

        assertThat(exception.errors()).extracting(ConfigurationError::code, ConfigurationError::type)
                .containsExactly(tuple("V-01", TemplateSheets.FullTemplate.class));
    }

    @Test
    void generateReportsTheErrorsOfATemplateSharedByTwoSheetClassesOnce() {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> sheetsmith.generate(List.of(
                        SheetData.of("Invoices", InvalidTemplates.UsesTemplateWithInvalidColors.class, List.of()),
                        SheetData.of("Purchases", InvalidTemplates.AlsoUsesTemplateWithInvalidColors.class,
                                List.of()))));

        assertThat(exception.errors())
                .extracting(ConfigurationError::code, ConfigurationError::type, ConfigurationError::element)
                .containsExactly(
                        tuple("V-13", InvalidTemplates.V13InvalidColors.class, "@ExcelSheetTemplate"),
                        tuple("V-13", InvalidTemplates.V13InvalidColors.class, "@ExcelSheetTemplate"),
                        tuple("V-04", InvalidTemplates.AlsoUsesTemplateWithInvalidColors.class, "a"));
    }

    @Test
    void generateReportsTheErrorsOfAStyleSheetSharedByTwoSheetClassesOnce() {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> sheetsmith.generate(List.of(
                        SheetData.of("First", InvalidSheets.FirstUsesInvalidStyleSheet.class, List.of()),
                        SheetData.of("Second", InvalidSheets.SecondUsesInvalidStyleSheet.class, List.of()))));

        assertThat(exception.errors())
                .extracting(ConfigurationError::code, ConfigurationError::type, ConfigurationError::element)
                .containsExactly(tuple("V-13", InvalidSheets.InvalidSharedStyleSheet.class, "@ExcelStyle(bad)"));
    }

    @Test
    void generateWritesTwoSheetClassesThatReferenceTheSameTemplate() throws IOException {
        byte[] file = sheetsmith.generate(List.of(
                SheetData.of("Report", TemplateSheets.InheritsEverything.class, List.of()),
                SheetData.of("Purchases", TemplateSheets.OwnAccent.class, List.of())));

        try (XSSFWorkbook workbook = SheetsmithTest.read(file)) {
            assertThat(workbook.getSheet("Report").getCTWorksheet().isSetAutoFilter()).isTrue();
            assertThat(workbook.getSheet("Purchases").getCTWorksheet().isSetAutoFilter()).isTrue();
        }
    }
}