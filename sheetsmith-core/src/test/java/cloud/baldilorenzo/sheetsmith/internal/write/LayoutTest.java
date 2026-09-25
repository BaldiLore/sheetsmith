package cloud.baldilorenzo.sheetsmith.internal.write;

import cloud.baldilorenzo.sheetsmith.fixtures.WriterSheets;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static cloud.baldilorenzo.sheetsmith.internal.write.WriterSupport.sheet;
import static cloud.baldilorenzo.sheetsmith.internal.write.WriterSupport.write;
import static org.assertj.core.api.Assertions.assertThat;

class LayoutTest {

    private static final List<WriterSheets.Layout> ROWS = List.of(
            new WriterSheets.Layout("A-1", "short", 1),
            new WriterSheets.Layout("A-2", "a much longer description", 2),
            new WriterSheets.Layout("A-3", "mid length", 3));

    @Test
    void titleIsMergedAcrossAllColumnsAndEveryMergedCellIsStyled() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Report", WriterSheets.Layout.class, ROWS))) {
            XSSFSheet sheet = workbook.getSheet("Report");
            Row title = sheet.getRow(0);

            assertThat(title.getCell(0).getStringCellValue()).isEqualTo("Report");
            assertThat(sheet.getMergedRegions()).containsExactly(new CellRangeAddress(0, 0, 0, 2));
            for (int j = 0; j < 3; j++) {
                XSSFCellStyle style = (XSSFCellStyle) title.getCell(j).getCellStyle();
                assertThat(style.getFont().getBold()).isTrue();
                assertThat(style.getFont().getFontHeightInPoints()).isEqualTo((short) 14);
            }
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Code");
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("A-1");
        }
    }

    @Test
    void headerRowIsFrozenBelowTheTitle() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Report", WriterSheets.Layout.class, ROWS))) {
            PaneInformation pane = workbook.getSheet("Report").getPaneInformation();

            assertThat(pane.isFreezePane()).isTrue();
            assertThat(pane.getHorizontalSplitPosition()).isEqualTo((short) 2);
            assertThat(pane.getVerticalSplitPosition()).isZero();
        }
    }

    @Test
    void withoutTitleOnlyTheHeaderIsFrozenAndNothingIsMerged() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Types", WriterSheets.Nullable.class, List.of()))) {
            XSSFSheet sheet = workbook.getSheet("Types");

            assertThat(sheet.getPaneInformation().getHorizontalSplitPosition()).isEqualTo((short) 1);
            assertThat(sheet.getMergedRegions()).isEmpty();
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Name");
        }
    }

    @Test
    void disabledOptionsAddNoPaneAndNoFilter() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Plain", WriterSheets.Plain.class,
                List.of(new WriterSheets.Plain("v"))))) {
            XSSFSheet sheet = workbook.getSheet("Plain");

            assertThat(sheet.getPaneInformation()).isNull();
            assertThat(sheet.getCTWorksheet().isSetAutoFilter()).isFalse();
            assertThat(sheet.getColumnWidth(0)).isEqualTo(sheet.getDefaultColumnWidth() * 256);
        }
    }

    @Test
    void autoFilterCoversHeaderAndDataRows() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Report", WriterSheets.Layout.class, ROWS))) {
            assertThat(workbook.getSheet("Report").getCTWorksheet().getAutoFilter().getRef()).isEqualTo("A2:C5");
        }
    }

    @Test
    void autoFilterCoversOnlyTheHeaderWithoutData() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Report", WriterSheets.Layout.class, List.of()))) {
            XSSFSheet sheet = workbook.getSheet("Report");

            assertThat(sheet.getCTWorksheet().getAutoFilter().getRef()).isEqualTo("A2:C2");
            assertThat(sheet.getLastRowNum()).isEqualTo(1);
        }
    }

    @Test
    void explicitWidthIsKeptAndAutoSizeAppliesToTheOtherColumns() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Report", WriterSheets.Layout.class, ROWS))) {
            XSSFSheet sheet = workbook.getSheet("Report");

            assertThat(sheet.getColumnWidth(0)).isEqualTo(20 * 256);
            assertThat(sheet.getColumnWidth(1)).isGreaterThan(sheet.getColumnWidth(2));
        }
    }

    @Test
    void autoSizeFailureFallsBackToTheLongestHeaderOrTextPlusTwo() throws IOException {
        SheetWriter failingSizer = new SheetWriter(WriterSupport.STANDARD, (sheet, column) -> {
            throw new InternalError("no fonts");
        });
        WorkbookWriter writer = new WorkbookWriter(WorkbookSupplier.XSSF, failingSizer);

        try (XSSFWorkbook workbook = write(writer, sheet("Report", WriterSheets.Layout.class, ROWS))) {
            XSSFSheet sheet = workbook.getSheet("Report");

            assertThat(sheet.getColumnWidth(0)).isEqualTo(20 * 256);
            assertThat(sheet.getColumnWidth(1)).isEqualTo(("a much longer description".length() + 2) * 256);
            assertThat(sheet.getColumnWidth(2)).isEqualTo(("Qty".length() + 2) * 256);
        }
    }

    @Test
    void autoSizeFallbackIsCappedAt255Characters() throws IOException {
        SheetWriter failingSizer = new SheetWriter(WriterSupport.STANDARD, (sheet, column) -> {
            throw new UnsatisfiedLinkError("libfontmanager");
        });
        WorkbookWriter writer = new WorkbookWriter(WorkbookSupplier.XSSF, failingSizer);
        List<WriterSheets.Layout> rows = List.of(new WriterSheets.Layout("c", "y".repeat(400), 1));

        try (XSSFWorkbook workbook = write(writer, sheet("Report", WriterSheets.Layout.class, rows))) {
            assertThat(workbook.getSheet("Report").getColumnWidth(1)).isEqualTo(255 * 256);
        }
    }

    @Test
    void sheetsAreWrittenInListOrder() throws IOException {
        try (XSSFWorkbook workbook = write(
                sheet("Second", WriterSheets.Plain.class, List.of(new WriterSheets.Plain("v"))),
                sheet("First", WriterSheets.Layout.class, ROWS))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
            assertThat(workbook.getSheetName(0)).isEqualTo("Second");
            assertThat(workbook.getSheetName(1)).isEqualTo("First");
        }
    }
}
