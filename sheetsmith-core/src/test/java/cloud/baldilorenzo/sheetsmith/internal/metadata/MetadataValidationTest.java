package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.fixtures.InvalidSheets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.tuple;

class MetadataValidationTest {

    private final MetadataExtractor extractor = new MetadataExtractor();

    @Test
    void v01ClassWithoutExcelSheet() {
        assertThat(errors(InvalidSheets.V01MissingSheet.class))
                .extracting(ConfigurationError::code, ConfigurationError::type, ConfigurationError::element)
                .containsExactly(tuple("V-01", InvalidSheets.V01MissingSheet.class, ""));
    }

    @Test
    void v02ClassWithoutColumns() {
        assertThat(errors(InvalidSheets.V02NoColumns.class))
                .extracting(ConfigurationError::code).containsExactly("V-02");
    }

    @Test
    void v03DuplicateOrder() {
        assertThat(errors(InvalidSheets.V03DuplicateOrder.class))
                .singleElement()
                .satisfies(error -> {
                    assertThat(error.code()).isEqualTo("V-03");
                    assertThat(error.message()).contains("order 1");
                });
    }

    @Test
    void v04BlankHeader() {
        assertThat(errors(InvalidSheets.V04BlankHeader.class))
                .extracting(ConfigurationError::code, ConfigurationError::element)
                .containsExactly(tuple("V-04", "a"));
    }

    @Test
    void v05StaticField() {
        assertThat(errors(InvalidSheets.V05StaticField.class))
                .extracting(ConfigurationError::code, ConfigurationError::element)
                .containsExactly(tuple("V-05", "constant"));
    }

    @Test
    void v06UnknownStyleInSheetAndColumnSlots() {
        assertThat(errors(InvalidSheets.V06UnknownStyle.class))
                .extracting(ConfigurationError::code, ConfigurationError::element, ConfigurationError::message)
                .containsExactlyInAnyOrder(
                        tuple("V-06", "@ExcelSheet", "style 'missing' not found (referenced by header.base)"),
                        tuple("V-06", "a", "style 'money' not found (referenced by styles.even)"));
    }

    @Test
    void v07DuplicateStyleOnTheClass() {
        assertThat(errors(InvalidSheets.V07DuplicateStyle.class))
                .extracting(ConfigurationError::code, ConfigurationError::element)
                .containsExactly(tuple("V-07", "@ExcelStyle(money)"));
    }

    @Test
    void v08SameStyleInTwoStyleSheets() {
        assertThat(errors(InvalidSheets.V08StyleSheetConflict.class))
                .singleElement()
                .satisfies(error -> {
                    assertThat(error.code()).isEqualTo("V-08");
                    assertThat(error.message()).contains("'shared'",
                            InvalidSheets.FirstStyleSheet.class.getName(),
                            InvalidSheets.SecondStyleSheet.class.getName());
                });
    }

    @Test
    void v09StyleSheetWithoutAnnotation() {
        assertThat(errors(InvalidSheets.V09NotAStyleSheet.class))
                .singleElement()
                .satisfies(error -> {
                    assertThat(error.code()).isEqualTo("V-09");
                    assertThat(error.message()).contains(InvalidSheets.NotAStyleSheet.class.getName());
                });
    }

    @Test
    void v13InvalidColours() {
        assertThat(errors(InvalidSheets.V13InvalidColor.class))
                .extracting(ConfigurationError::code, ConfigurationError::element)
                .containsExactlyInAnyOrder(
                        tuple("V-13", "@ExcelSheet"),
                        tuple("V-13", "@ExcelSheet"),
                        tuple("V-13", "@ExcelStyle(bad)"),
                        tuple("V-13", "@ExcelStyle(bad)"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"#1f4e79", "#1F4E79", "DARK_BLUE", "WHITE"})
    void validColours(String colour) {
        assertThat(MetadataValidator.isValidColor(colour)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1F4E79", "#1F4E7", "#1F4E799", "dark_blue", "blue", "#GGGGGG"})
    void invalidColours(String colour) {
        assertThat(MetadataValidator.isValidColor(colour)).isFalse();
    }

    @Test
    void v14NumericAttributesOutOfRange() {
        assertThat(errors(InvalidSheets.V14OutOfRange.class))
                .extracting(ConfigurationError::code, ConfigurationError::element, ConfigurationError::message)
                .containsExactlyInAnyOrder(
                        tuple("V-14", "@ExcelStyle(bad)", "rotation 91 is out of range -90 to 90, or 255"),
                        tuple("V-14", "@ExcelStyle(bad)", "indent 251 is out of range 0 to 250"),
                        tuple("V-14", "@ExcelStyle(bad)", "fontSize 0 is out of range 1 to 409"),
                        tuple("V-14", "a", "width 256 is out of range 1 to 255"));
    }

    @Test
    void v15TitleStyleWithoutTitle() {
        assertThat(errors(InvalidSheets.V15TitleStyleWithoutTitle.class))
                .extracting(ConfigurationError::code).containsExactly("V-15");
    }

    @Test
    void v16BlankStyleName() {
        assertThat(errors(InvalidSheets.V16BlankStyleName.class))
                .extracting(ConfigurationError::code, ConfigurationError::element)
                .containsExactly(tuple("V-16", "@ExcelStyle(#1)"));
    }

    @Test
    void allErrorsOfAClassAreReportedTogether() {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> extractor.extract(InvalidSheets.ManyErrors.class));

        assertThat(exception.errors()).extracting(ConfigurationError::code)
                .containsExactlyInAnyOrder("V-04", "V-03", "V-14", "V-13", "V-06");
        assertThat(exception.getMessage().lines()).hasSize(5);
        assertThat(exception.getMessage())
                .contains("[V-04] " + InvalidSheets.ManyErrors.class.getName() + ".a: header is blank");
    }

    private List<ConfigurationError> errors(Class<?> type) {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> extractor.extract(type));
        assertThat(exception).as("extraction of %s", type.getSimpleName()).isNotNull();
        return exception.errors();
    }
}
