package cloud.baldilorenzo.sheetsmith.internal.write;

import cloud.baldilorenzo.sheetsmith.SheetsmithDefaults;
import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;
import cloud.baldilorenzo.sheetsmith.fixtures.WriterSheets;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cloud.baldilorenzo.sheetsmith.internal.write.WriterSupport.sheet;
import static cloud.baldilorenzo.sheetsmith.internal.write.WriterSupport.write;
import static org.assertj.core.api.Assertions.assertThat;

class CellWritingTest {

    private static final WriterSheets.AllTypes ALL_TYPES = new WriterSheets.AllTypes(
            "hello", 'x', 'y', 42, 9_000_000_000L, 1.5, new BigDecimal("12.34"), new BigInteger("123456789"),
            true, Boolean.FALSE, WriterSheets.Color.GREEN, LocalDate.of(2026, 9, 25),
            LocalDateTime.of(2026, 9, 25, 13, 45, 30), new StringBuilder("built"), 2.25);

    @Test
    void builtInTypesAreWrittenWithTheExpectedCellTypeAndFormat() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Types", WriterSheets.AllTypes.class, List.of(ALL_TYPES)))) {
            Row header = workbook.getSheet("Types").getRow(0);
            Row row = workbook.getSheet("Types").getRow(1);

            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Text");
            assertText(row.getCell(0), "hello");
            assertText(row.getCell(1), "x");
            assertText(row.getCell(2), "y");
            assertNumber(row.getCell(3), 42, "General");
            assertNumber(row.getCell(4), 9_000_000_000d, "General");
            assertNumber(row.getCell(5), 1.5, "General");
            assertNumber(row.getCell(6), 12.34, "General");
            assertNumber(row.getCell(7), 123456789, "General");
            assertThat(row.getCell(8).getCellType()).isEqualTo(CellType.BOOLEAN);
            assertThat(row.getCell(8).getBooleanCellValue()).isTrue();
            assertThat(row.getCell(9).getBooleanCellValue()).isFalse();
            assertText(row.getCell(10), "GREEN");
            assertThat(row.getCell(11).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(row.getCell(11).getLocalDateTimeCellValue()).isEqualTo(LocalDate.of(2026, 9, 25).atStartOfDay());
            assertThat(row.getCell(11).getCellStyle().getDataFormatString()).isEqualTo("yyyy-mm-dd");
            assertThat(row.getCell(12).getLocalDateTimeCellValue()).isEqualTo(LocalDateTime.of(2026, 9, 25, 13, 45, 30));
            assertThat(row.getCell(12).getCellStyle().getDataFormatString()).isEqualTo("yyyy-mm-dd hh:mm:ss");
            assertText(row.getCell(13), "built");
            assertNumber(row.getCell(14), 2.25, "0.0");
        }
    }

    @Test
    void defaultFormatsApplyOnlyWhenTheStyleSetsNoFormat() throws IOException {
        SheetsmithDefaults formats = new SheetsmithDefaults("dd/mm/yyyy", "dd/mm/yyyy hh:mm", "#,##0.00",
                TablePreset.NONE, "#4472C4");
        WorkbookWriter writer = new WorkbookWriter(WorkbookSupplier.XSSF, formats);

        try (XSSFWorkbook workbook = write(writer, sheet("Types", WriterSheets.AllTypes.class, List.of(ALL_TYPES)))) {
            Row row = workbook.getSheet("Types").getRow(1);

            assertThat(row.getCell(3).getCellStyle().getDataFormatString()).isEqualTo("#,##0.00");
            assertThat(row.getCell(11).getCellStyle().getDataFormatString()).isEqualTo("dd/mm/yyyy");
            assertThat(row.getCell(12).getCellStyle().getDataFormatString()).isEqualTo("dd/mm/yyyy hh:mm");
            assertThat(row.getCell(14).getCellStyle().getDataFormatString()).isEqualTo("0.0");
            assertThat(row.getCell(0).getCellStyle().getDataFormatString()).isEqualTo("General");
        }
    }

    @Test
    void nullValuesProduceEmptyCellsThatKeepTheirStyle() throws IOException {
        List<WriterSheets.Nullable> rows = List.of(
                new WriterSheets.Nullable(null, null, null),
                new WriterSheets.Nullable("a", 1, LocalDate.of(2026, 1, 1)));

        try (XSSFWorkbook workbook = write(sheet("Nulls", WriterSheets.Nullable.class, rows))) {
            Row row = workbook.getSheet("Nulls").getRow(1);

            for (int j = 0; j < 3; j++) {
                Cell cell = row.getCell(j);
                assertThat(cell).isNotNull();
                assertThat(cell.getCellType()).isEqualTo(CellType.BLANK);
                assertThat(cell.getCellStyle().getBorderTop()).isEqualTo(BorderStyle.THIN);
                assertThat(cell.getCellStyle().getBorderLeft()).isEqualTo(BorderStyle.THIN);
            }
            assertThat(workbook.getSheet("Nulls").getRow(2).getCell(0).getCellStyle().getBorderTop())
                    .isEqualTo(BorderStyle.THIN);
        }
    }

    @Test
    void fieldConvertersReceiveTheConversionContext() throws IOException {
        List<WriterSheets.FieldConverters> rows = List.of(
                new WriterSheets.FieldConverters("a", 1), new WriterSheets.FieldConverters("b", 2));

        try (XSSFWorkbook workbook = write(sheet("Ctx", WriterSheets.FieldConverters.class, rows))) {
            Row second = workbook.getSheet("Ctx").getRow(2);

            assertText(second.getCell(0), "B");
            assertText(second.getCell(1), "Ctx|2|described|FieldConverters|int");
        }
    }

    @Test
    void applicationConverterForASupertypeIsUsed() throws IOException {
        Map<Class<?>, CellConverter<?>> converters = Map.of(
                Number.class, (CellConverter<Number>) (value, context) -> CellValue.text("#" + value));

        try (XSSFWorkbook workbook = write(sheet("App", WriterSheets.Nullable.class,
                List.of(new WriterSheets.Nullable("a", 7, null)), converters))) {
            assertText(workbook.getSheet("App").getRow(1).getCell(1), "#7");
        }
    }

    @Test
    void blankCellValueProducesAStyledEmptyCell() throws IOException {
        Map<Class<?>, CellConverter<?>> converters = Map.of(
                CharSequence.class, (CellConverter<CharSequence>) (value, context) -> CellValue.blank());

        try (XSSFWorkbook workbook = write(sheet("Blank", WriterSheets.Nullable.class,
                List.of(new WriterSheets.Nullable("a", 7, null)), converters))) {
            Cell cell = workbook.getSheet("Blank").getRow(1).getCell(0);
            assertThat(cell.getCellType()).isEqualTo(CellType.BLANK);
            assertThat(cell.getCellStyle().getBorderTop()).isEqualTo(BorderStyle.THIN);
        }
    }

    private static void assertText(Cell cell, String expected) {
        assertThat(cell.getCellType()).isEqualTo(CellType.STRING);
        assertThat(cell.getStringCellValue()).isEqualTo(expected);
    }

    private static void assertNumber(Cell cell, double expected, String format) {
        assertThat(cell.getCellType()).isEqualTo(CellType.NUMERIC);
        assertThat(cell.getNumericCellValue()).isEqualTo(expected);
        assertThat(cell.getCellStyle().getDataFormatString()).isEqualTo(format);
    }
}
