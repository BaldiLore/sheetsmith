package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.fixtures.InvalidTemplates;
import cloud.baldilorenzo.sheetsmith.fixtures.TemplateSheets;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.tuple;

class TemplateValidationTest {

    private static final String SHEET = "@ExcelSheet";
    private static final String TEMPLATE = "@ExcelSheetTemplate";

    private final MetadataExtractor extractor = new MetadataExtractor();

    @Test
    void v21TemplateWithoutExcelSheetTemplate() {
        assertThat(errors(InvalidTemplates.V21NotATemplate.class))
                .singleElement()
                .satisfies(error -> {
                    assertThat(error.code()).isEqualTo("V-21");
                    assertThat(error.type()).isEqualTo(InvalidTemplates.V21NotATemplate.class);
                    assertThat(error.element()).isEqualTo(SHEET);
                    assertThat(error.message()).contains(InvalidTemplates.NotATemplate.class.getName());
                });
    }

    @Test
    void v22PresetSetTogetherWithTemplate() {
        assertThat(errors(InvalidTemplates.V22PresetWithTemplate.class))
                .singleElement()
                .satisfies(error -> {
                    assertThat(error.code()).isEqualTo("V-22");
                    assertThat(error.type()).isEqualTo(InvalidTemplates.V22PresetWithTemplate.class);
                    assertThat(error.element()).isEqualTo(SHEET);
                    assertThat(error.message()).contains("DARK", "remove preset", "declare it in the template");
                });
    }

    @Test
    void v23TemplateThatIsAlsoASheetClass() {
        assertThat(errors(InvalidTemplates.V23TemplateAndSheet.class))
                .extracting(ConfigurationError::code, ConfigurationError::type, ConfigurationError::element)
                .containsExactly(tuple("V-23", InvalidTemplates.V23TemplateAndSheet.class, TEMPLATE));
    }

    @Test
    void v23TemplateThatIsAlsoAStyleSheet() {
        assertThat(templateErrors(InvalidTemplates.V23TemplateAndStyleSheet.class))
                .extracting(ConfigurationError::code, ConfigurationError::type, ConfigurationError::element)
                .containsExactly(tuple("V-23", InvalidTemplates.V23TemplateAndStyleSheet.class, TEMPLATE));
    }

    @Test
    void v24TemplateDeclaringStyles() {
        assertThat(templateErrors(InvalidTemplates.V24StyleOnTemplate.class))
                .singleElement()
                .satisfies(error -> {
                    assertThat(error.code()).isEqualTo("V-24");
                    assertThat(error.type()).isEqualTo(InvalidTemplates.V24StyleOnTemplate.class);
                    assertThat(error.element()).isEqualTo(TEMPLATE);
                    assertThat(error.message()).contains("style sheet", "styleSheets");
                });
    }

    @Test
    void v25TemplateWithColumns() {
        assertThat(templateErrors(InvalidTemplates.V25ColumnOnTemplate.class))
                .extracting(ConfigurationError::code, ConfigurationError::type, ConfigurationError::element)
                .containsExactly(tuple("V-25", InvalidTemplates.V25ColumnOnTemplate.class, TEMPLATE));
    }

    @Test
    void v25TemplateWithInheritedColumns() {
        assertThat(templateErrors(InvalidTemplates.V25InheritedColumnOnTemplate.class))
                .singleElement()
                .satisfies(error -> {
                    assertThat(error.code()).isEqualTo("V-25");
                    assertThat(error.type()).isEqualTo(InvalidTemplates.V25InheritedColumnOnTemplate.class);
                    assertThat(error.element()).isEqualTo(TEMPLATE);
                    assertThat(error.message()).contains("inherited");
                });
    }

    @Test
    void v13InvalidColoursOfTheTemplate() {
        assertThat(templateErrors(InvalidTemplates.V13InvalidColors.class))
                .extracting(ConfigurationError::code, ConfigurationError::type, ConfigurationError::element)
                .containsExactly(
                        tuple("V-13", InvalidTemplates.V13InvalidColors.class, TEMPLATE),
                        tuple("V-13", InvalidTemplates.V13InvalidColors.class, TEMPLATE));
    }

    @Test
    void v09StyleSheetOfTheTemplateWithoutAnnotation() {
        assertThat(templateErrors(InvalidTemplates.V09NotAStyleSheet.class))
                .singleElement()
                .satisfies(error -> {
                    assertThat(error.code()).isEqualTo("V-09");
                    assertThat(error.type()).isEqualTo(InvalidTemplates.V09NotAStyleSheet.class);
                    assertThat(error.element()).isEqualTo(TEMPLATE);
                    assertThat(error.message()).contains(InvalidTemplates.NotAStyleSheet.class.getName());
                });
    }

    @Test
    void v08SameStyleInTwoStyleSheetsOfTheTemplate() {
        assertThat(templateErrors(InvalidTemplates.V08StyleSheetConflict.class))
                .singleElement()
                .satisfies(error -> {
                    assertThat(error.code()).isEqualTo("V-08");
                    assertThat(error.type()).isEqualTo(InvalidTemplates.V08StyleSheetConflict.class);
                    assertThat(error.element()).isEqualTo(TEMPLATE);
                    assertThat(error.message()).contains("'shared'",
                            InvalidTemplates.FirstStyleSheet.class.getName(),
                            InvalidTemplates.SecondStyleSheet.class.getName());
                });
    }

    @Test
    void v06UnknownStylesOfTheTemplateResolvedOnItsOwnStyleSheets() {
        List<ConfigurationError> errors = templateErrors(InvalidTemplates.V06UnknownStyles.class);

        assertThat(errors).hasSize(11)
                .allSatisfy(error -> {
                    assertThat(error.code()).isEqualTo("V-06");
                    assertThat(error.type()).isEqualTo(InvalidTemplates.V06UnknownStyles.class);
                    assertThat(error.element()).isEqualTo(TEMPLATE);
                });
        assertThat(errors).extracting(ConfigurationError::message).contains(
                "style 'missing' not found (referenced by titleStyle)",
                "style 'missing' not found (referenced by header.lastColumn)",
                "style 'missing' not found (referenced by body.odd)");
    }

    @Test
    void templateMustBeValidOnItsOwnEvenWhenTheSheetDefinesTheMissingStyles() {
        assertThat(errors(InvalidTemplates.DefinesStyleMissingFromTemplate.class))
                .hasSize(11)
                .extracting(ConfigurationError::code, ConfigurationError::type)
                .containsOnly(tuple("V-06", InvalidTemplates.V06UnknownStyles.class));
    }

    @Test
    void sheetReferencingAnInvalidTemplateReportsOnlyTheErrorsOfTheTemplate() {
        assertThat(errors(InvalidTemplates.UsesTemplateWithUnknownStyles.class))
                .hasSize(11)
                .extracting(ConfigurationError::code, ConfigurationError::type)
                .containsOnly(tuple("V-06", InvalidTemplates.V06UnknownStyles.class));
        assertThat(errors(InvalidTemplates.UsesTemplateWithInvalidColors.class))
                .extracting(ConfigurationError::code, ConfigurationError::type)
                .containsExactly(
                        tuple("V-13", InvalidTemplates.V13InvalidColors.class),
                        tuple("V-13", InvalidTemplates.V13InvalidColors.class));
    }

    @Test
    void styleSheetConflictOfTheTemplateIsReportedOnceOnTheTemplate() {
        assertThat(errors(InvalidTemplates.UsesTemplateWithStyleSheetConflict.class))
                .extracting(ConfigurationError::code, ConfigurationError::type, ConfigurationError::element)
                .containsExactly(tuple("V-08", InvalidTemplates.V08StyleSheetConflict.class, TEMPLATE));
    }

    @Test
    void errorsOfTheSheetAndOfItsTemplateAreReportedTogether() {
        assertThat(errors(InvalidTemplates.OwnAndTemplateErrors.class))
                .extracting(ConfigurationError::code, ConfigurationError::type)
                .containsExactlyInAnyOrder(
                        tuple("V-24", InvalidTemplates.V24StyleOnTemplate.class),
                        tuple("V-06", InvalidTemplates.OwnAndTemplateErrors.class),
                        tuple("V-04", InvalidTemplates.OwnAndTemplateErrors.class));
    }

    @Test
    void validTemplatesHaveNoErrorsOnTheirOwn() {
        assertThat(extractor.inspectTemplate(TemplateSheets.FullTemplate.class).errors()).isEmpty();
        assertThat(extractor.inspectTemplate(TemplateSheets.BlankTemplate.class).errors()).isEmpty();
    }

    @Test
    void inspectingAClassWithoutExcelSheetTemplateAsATemplateReportsV01() {
        assertThat(extractor.inspectTemplate(TemplateSheets.WithoutTemplate.class).errors())
                .extracting(ConfigurationError::code, ConfigurationError::type, ConfigurationError::element)
                .containsExactly(tuple("V-01", TemplateSheets.WithoutTemplate.class, ""));
    }

    @Test
    void templateCannotBeExtractedAsASheet() {
        assertThat(errors(TemplateSheets.FullTemplate.class))
                .singleElement()
                .satisfies(error -> {
                    assertThat(error.code()).isEqualTo("V-01");
                    assertThat(error.message()).contains("@ExcelSheetTemplate", "@ExcelSheet(template = ...)");
                });
    }

    private List<ConfigurationError> errors(Class<?> type) {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> extractor.extract(type));
        assertThat(exception).as("extraction of %s", type.getSimpleName()).isNotNull();
        return exception.errors();
    }

    private List<ConfigurationError> templateErrors(Class<?> type) {
        List<ConfigurationError> errors = extractor.inspectTemplate(type).errors();
        assertThat(errors).as("inspection of %s", type.getSimpleName()).isNotEmpty();
        return errors;
    }
}
