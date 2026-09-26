package cloud.baldilorenzo.sheetsmith.internal.write;

import cloud.baldilorenzo.sheetsmith.SheetData;
import cloud.baldilorenzo.sheetsmith.Sheetsmith;
import cloud.baldilorenzo.sheetsmith.SheetsmithDefaults;
import cloud.baldilorenzo.sheetsmith.fixtures.MemoisationSheets;
import cloud.baldilorenzo.sheetsmith.fixtures.PresetSheets;
import cloud.baldilorenzo.sheetsmith.fixtures.WriterSheets;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class StyleMemoisationTest {

    /**
     * Cell styles of the recorded workbook, measured with the implementation that resolved the style of every
     * cell without memoisation.
     */
    private static final int RECORDED_CELL_STYLES = 13;

    private final Sheetsmith sheetsmith = Sheetsmith.builder().build();

    @Test
    void cellsWithTheSameColumnRoleAndValueKindShareOneCellStyle() throws IOException {
        // rows 2, 4 and 6 are even and neither first nor last; rows 3, 5 and 7 are odd and neither first nor last
        try (XSSFWorkbook workbook = generate(zebra(LocalDate.of(2026, 1, 1), 1, 2, 3, 4, 5, 6, 7))) {
            Sheet sheet = workbook.getSheet("Zebra");
            for (int column = 0; column < 4; column++) {
                assertThat(styleIndex(sheet, 4, column)).isEqualTo(styleIndex(sheet, 2, column))
                        .isEqualTo(styleIndex(sheet, 6, column));
                assertThat(styleIndex(sheet, 5, column)).isEqualTo(styleIndex(sheet, 3, column))
                        .isEqualTo(styleIndex(sheet, 7, column));
                assertThat(styleIndex(sheet, 3, column)).isNotEqualTo(styleIndex(sheet, 2, column));
            }
            assertThat(styleIndex(sheet, 1, 0)).isNotEqualTo(styleIndex(sheet, 3, 0));
            assertThat(styleIndex(sheet, 8, 0)).isNotEqualTo(styleIndex(sheet, 2, 0));
        }
    }

    @Test
    void cellsWithTheSameColumnAndRoleButDifferentValueKindsGetTheirOwnDefaultFormat() throws IOException {
        LocalDate day = LocalDate.of(2026, 3, 1);
        // rows 2 and 6 hold a date, row 4 a number: the three rows have the same role
        try (XSSFWorkbook workbook = generate(zebra("x", day, "x", 42, "x", day, "x", "x"))) {
            Sheet sheet = workbook.getSheet("Zebra");
            assertThat(styleIndex(sheet, 2, 3)).isEqualTo(styleIndex(sheet, 6, 3))
                    .isNotEqualTo(styleIndex(sheet, 4, 3));
            assertThat(dataFormat(sheet, 2, 3)).isEqualTo("yyyy-mm-dd");
            assertThat(dataFormat(sheet, 4, 3)).isEqualTo("General");
        }
    }

    @Test
    void cellStyleCountOfARecordedWorkbookIsUnchanged() throws IOException {
        Sheetsmith withDefaults = Sheetsmith.builder()
                .defaults(new SheetsmithDefaults("dd/mm/yyyy", "dd/mm/yyyy hh:mm", "#,##0.00", TablePreset.MEDIUM,
                        "#1F4E79"))
                .build();
        List<PresetSheets.Invoice> invoices = IntStream.rangeClosed(1, 25)
                .mapToObj(i -> new PresetSheets.Invoice("INV-" + i, "Customer " + i, LocalDate.of(2026, 1, i),
                        i % 5 == 0 ? null : BigDecimal.valueOf(i * 100L), i % 2 == 0))
                .toList();
        List<WriterSheets.Nullable> nullables = IntStream.rangeClosed(1, 9)
                .mapToObj(i -> new WriterSheets.Nullable(i % 3 == 0 ? null : "n" + i, i % 4 == 0 ? null : i,
                        i % 2 == 0 ? null : LocalDate.of(2026, 2, i)))
                .toList();

        byte[] bytes = withDefaults.generate(List.of(
                SheetData.of("Invoices", PresetSheets.Invoice.class, invoices),
                SheetData.of("Nullable", WriterSheets.Nullable.class, nullables)));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(workbook.getNumCellStyles()).isEqualTo(RECORDED_CELL_STYLES);
        }
    }

    private static List<MemoisationSheets.Zebra> zebra(Object... values) {
        return IntStream.range(0, values.length)
                .mapToObj(i -> new MemoisationSheets.Zebra("c" + i, i, LocalDate.of(2026, 1, 1).plusDays(i),
                        values[i]))
                .toList();
    }

    private XSSFWorkbook generate(List<MemoisationSheets.Zebra> rows) throws IOException {
        byte[] bytes = sheetsmith.generate(List.of(SheetData.of("Zebra", MemoisationSheets.Zebra.class, rows)));
        return new XSSFWorkbook(new ByteArrayInputStream(bytes));
    }

    /** Style index of a data cell; data row 1 is sheet row 1, below the header. */
    private static short styleIndex(Sheet sheet, int dataRow, int column) {
        return sheet.getRow(dataRow).getCell(column).getCellStyle().getIndex();
    }

    private static String dataFormat(Sheet sheet, int dataRow, int column) {
        return sheet.getRow(dataRow).getCell(column).getCellStyle().getDataFormatString();
    }
}
