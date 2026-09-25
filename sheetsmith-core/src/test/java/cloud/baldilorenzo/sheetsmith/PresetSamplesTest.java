package cloud.baldilorenzo.sheetsmith;

import cloud.baldilorenzo.sheetsmith.fixtures.PresetSheets;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Generates one sample workbook per preset and accent colour in {@code target/preset-samples}, for visual checks in
 * Excel and LibreOffice.
 */
class PresetSamplesTest {

    private static final Path OUTPUT = Path.of("target", "preset-samples");
    private static final List<String> ACCENTS = List.of("#4472C4", "#FFC000", "DARK_RED");

    private static final List<PresetSheets.Invoice> ROWS = IntStream.rangeClosed(1, 12)
            .mapToObj(i -> new PresetSheets.Invoice(
                    "INV-" + (1000 + i),
                    "Customer " + (char) ('A' + i),
                    LocalDate.of(2026, 1, 1).plusDays(7L * i),
                    BigDecimal.valueOf(1234.5 * i).setScale(2, RoundingMode.HALF_UP),
                    i % 3 != 0))
            .toList();

    @ParameterizedTest
    @EnumSource(value = TablePreset.class, names = {"LIGHT", "MEDIUM", "DARK"})
    void writesASampleWorkbookPerAccent(TablePreset preset) throws IOException {
        Files.createDirectories(OUTPUT);

        for (String accent : ACCENTS) {
            SheetsmithDefaults defaults = new SheetsmithDefaults("dd/mm/yyyy", "dd/mm/yyyy hh:mm", "",
                    preset, accent);
            byte[] bytes = Sheetsmith.builder().defaults(defaults).build().generate(List.of(
                    SheetData.of("Invoices", PresetSheets.Invoice.class, ROWS),
                    SheetData.of("Empty", PresetSheets.Invoice.class, List.of())));

            Path file = OUTPUT.resolve(preset.name().toLowerCase(Locale.ROOT) + "-"
                    + accent.replace("#", "").toLowerCase(Locale.ROOT) + ".xlsx");
            Files.write(file, bytes);

            try (XSSFWorkbook workbook = SheetsmithTest.read(Files.readAllBytes(file))) {
                assertThat(workbook.getSheet("Invoices").getLastRowNum()).isEqualTo(ROWS.size() + 1);
            }
        }
    }
}
