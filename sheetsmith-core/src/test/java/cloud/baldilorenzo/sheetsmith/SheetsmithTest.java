package cloud.baldilorenzo.sheetsmith;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellConverterFactory;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;
import cloud.baldilorenzo.sheetsmith.fixtures.InvalidSheets;
import cloud.baldilorenzo.sheetsmith.fixtures.ValidSheets;
import cloud.baldilorenzo.sheetsmith.fixtures.WriterSheets;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.tuple;

class SheetsmithTest {

    private final Sheetsmith sheetsmith = Sheetsmith.builder().build();

    @Test
    void defaultInstanceGeneratesAValidXlsxFile() throws IOException {
        byte[] bytes = sheetsmith.generate(List.of(SheetData.of("People", ValidSheets.Person.class,
                List.of(new ValidSheets.Person("ada", 36), new ValidSheets.Person("alan", 41)))));

        assertThat(bytes).startsWith(0x50, 0x4B);
        try (XSSFWorkbook workbook = read(bytes)) {
            Row row = workbook.getSheet("People").getRow(2);
            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("ALAN");
            assertThat(row.getCell(1).getNumericCellValue()).isEqualTo(41);
        }
    }

    @Test
    void sheetsOfDifferentTypesAreWrittenInListOrder() throws IOException {
        byte[] bytes = sheetsmith.generate(List.of(
                SheetData.of("Products", ValidSheets.Product.class, List.of()),
                SheetData.of("People", ValidSheets.Person.class, List.of(new ValidSheets.Person("ada", 36))),
                SheetData.of("More people", ValidSheets.Person.class, List.of())));

        try (XSSFWorkbook workbook = read(bytes)) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(3);
            assertThat(workbook.getSheetName(0)).isEqualTo("Products");
            assertThat(workbook.getSheetName(1)).isEqualTo("People");
            assertThat(workbook.getSheetName(2)).isEqualTo("More people");
            assertThat(workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue()).isEqualTo("Code");
        }
    }

    @Test
    void customConvertersAreRegistered() throws IOException {
        Sheetsmith custom = Sheetsmith.builder()
                .converter(Date.class, (value, context) -> CellValue.text("date:" + value.getTime()))
                .build();

        byte[] bytes = custom.generate(List.of(SheetData.of("Dates", WriterSheets.V10Unsupported.class,
                List.of(new WriterSheets.V10Unsupported(new Date(1000))))));

        try (XSSFWorkbook workbook = read(bytes)) {
            assertThat(workbook.getSheet("Dates").getRow(1).getCell(0).getStringCellValue()).isEqualTo("date:1000");
        }
    }

    @Test
    void registeringTwoConvertersForTheSameTypeIsRejected() {
        CellConverter<Object> converter = (value, context) -> CellValue.blank();
        Sheetsmith.Builder builder = Sheetsmith.builder().converter(Integer.class, converter);

        assertThatIllegalArgumentException().isThrownBy(() -> builder.converter(Integer.class, converter))
                .withMessageContaining("java.lang.Integer");
        assertThatIllegalArgumentException().isThrownBy(() -> builder.converter(int.class, converter));
    }

    @Test
    void customConverterFactoryCreatesFieldConverters() throws IOException {
        AtomicInteger created = new AtomicInteger();
        Sheetsmith custom = Sheetsmith.builder().converterFactory(new CellConverterFactory() {
            @Override
            public <C extends CellConverter<?>> C create(Class<C> converterClass) {
                created.incrementAndGet();
                return converterClass.cast(new WriterSheets.Upper());
            }
        }).build();

        byte[] bytes = custom.generate(List.of(SheetData.of("Shared", WriterSheets.SharedFieldConverter.class,
                List.of(new WriterSheets.SharedFieldConverter("a", "b")))));

        assertThat(created).hasValue(1);
        try (XSSFWorkbook workbook = read(bytes)) {
            assertThat(workbook.getSheet("Shared").getRow(1).getCell(1).getStringCellValue()).isEqualTo("B");
        }
    }

    @Test
    void defaultsAreApplied() throws IOException {
        Sheetsmith custom = Sheetsmith.builder()
                .defaults(new SheetsmithDefaults("dd/mm/yyyy", "dd/mm/yyyy hh:mm", "0.00", TablePreset.NONE, "RED"))
                .build();

        byte[] bytes = custom.generate(List.of(SheetData.of("N", WriterSheets.Nullable.class,
                List.of(new WriterSheets.Nullable("a", 3, LocalDate.of(2026, 1, 2))))));

        try (XSSFWorkbook workbook = read(bytes)) {
            Row row = workbook.getSheet("N").getRow(1);
            assertThat(row.getCell(1).getCellStyle().getDataFormatString()).isEqualTo("0.00");
            assertThat(row.getCell(2).getCellStyle().getDataFormatString()).isEqualTo("dd/mm/yyyy");
        }
    }

    @Test
    void v18EmptySheetList() {
        assertThat(configurationErrors(List.of())).extracting(ConfigurationError::code, ConfigurationError::type)
                .containsExactly(tuple("V-18", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abcdefghijabcdefghijabcdefghij12", "a\\b", "a/b", "a?b", "a*b", "a[b", "a]b",
            "a:b", "'quoted", "quoted'"})
    void v19InvalidSheetNames(String name) {
        assertThat(configurationErrors(List.of(SheetData.of(name, ValidSheets.Person.class, List.of()))))
                .extracting(ConfigurationError::code, ConfigurationError::element)
                .containsExactly(tuple("V-19", "sheets[0]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "abcdefghijabcdefghijabcdefghij1", "it's fine", "Sheet (1) - 2026.01"})
    void validSheetNames(String name) {
        assertThatCode(() -> sheetsmith.generate(List.of(SheetData.of(name, ValidSheets.Person.class, List.of()))))
                .doesNotThrowAnyException();
    }

    @Test
    void v20SheetNamesMustBeUniqueIgnoringCase() {
        assertThat(configurationErrors(List.of(
                SheetData.of("People", ValidSheets.Person.class, List.of()),
                SheetData.of("PEOPLE", ValidSheets.Person.class, List.of()))))
                .extracting(ConfigurationError::code, ConfigurationError::element)
                .containsExactly(tuple("V-20", "sheets[1]"));
    }

    @Test
    void inputAndClassErrorsAreReportedTogether() {
        assertThat(configurationErrors(List.of(
                SheetData.of("bad:name", InvalidSheets.V04BlankHeader.class, List.of()),
                SheetData.of("Dates", WriterSheets.V10Unsupported.class, List.of()),
                SheetData.of("dates", ValidSheets.Person.class, List.of()))))
                .extracting(ConfigurationError::code)
                .containsExactly("V-19", "V-20", "V-04", "V-10");
    }

    @Test
    void validateReportsExtractionAndBindingErrorsTogether() {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> sheetsmith.validate(InvalidSheets.ExtractionAndBindingErrors.class));

        assertThat(exception.errors()).extracting(ConfigurationError::code, ConfigurationError::element)
                .containsExactly(tuple("V-04", "header"), tuple("V-10", "when"));
    }

    @Test
    void validateAcceptsValidClasses() {
        assertThatCode(() -> sheetsmith.validate(ValidSheets.Invoice.class)).doesNotThrowAnyException();
    }

    @Test
    void validateReportsClassesWithoutExcelSheet() {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> sheetsmith.validate(InvalidSheets.V01MissingSheet.class));

        assertThat(exception.errors()).extracting(ConfigurationError::code).containsExactly("V-01");
    }

    @Test
    void invalidClassFailsAtEveryGeneration() {
        List<SheetData<?>> sheets = List.of(SheetData.of("S", WriterSheets.V10Unsupported.class, List.of()));

        for (int i = 0; i < 2; i++) {
            assertThat(catchThrowableOfType(SheetsmithConfigurationException.class,
                    () -> sheetsmith.generate(sheets)).errors()).extracting(ConfigurationError::code)
                    .containsExactly("V-10");
        }
    }

    private List<ConfigurationError> configurationErrors(List<SheetData<?>> sheets) {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> sheetsmith.generate(sheets));
        assertThat(exception).isNotNull();
        return exception.errors();
    }

    static XSSFWorkbook read(byte[] bytes) {
        try {
            return new XSSFWorkbook(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
