package cloud.baldilorenzo.sheetsmith.internal.write;

import cloud.baldilorenzo.sheetsmith.SheetsmithGenerationException;
import cloud.baldilorenzo.sheetsmith.fixtures.WriterSheets;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static cloud.baldilorenzo.sheetsmith.internal.write.WriterSupport.sheet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class WorkbookReleaseTest {

    private static final List<WriterSheets.Plain> ROWS =
            List.of(new WriterSheets.Plain("first"), new WriterSheets.Plain("second"));

    @Test
    void xssfWorkbookIsNotClosedSoItIsNotSerialisedTwice() throws IOException {
        RecordingXssfWorkbook workbook = new RecordingXssfWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writer(workbook).write(List.of(sheet("Plain", WriterSheets.Plain.class, ROWS)), out);

        assertThat(workbook.closes).isZero();
        try (XSSFWorkbook read = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            XSSFSheet sheet = read.getSheet("Plain");
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Value");
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("first");
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("second");
        }
    }

    @Test
    void xssfWorkbookIsReleasedWithoutClosingAlsoOnGenerationError() {
        RecordingXssfWorkbook workbook = new RecordingXssfWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<WriterSheets.Plain> rows = Arrays.asList(new WriterSheets.Plain("first"), null);

        SheetsmithGenerationException exception = catchThrowableOfType(SheetsmithGenerationException.class,
                () -> writer(workbook).write(List.of(sheet("Plain", WriterSheets.Plain.class, rows)), out));

        assertThat(exception).isNotNull();
        assertThat(exception.sheetName()).isEqualTo("Plain");
        assertThat(exception.rowIndex()).isEqualTo(2);
        assertThat(workbook.closes).isZero();
        assertThat(out.size()).isZero();
    }

    @Test
    void nonOoxmlWorkbookIsClosed() throws IOException {
        RecordingSxssfWorkbook workbook = new RecordingSxssfWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writer(workbook).write(List.of(sheet("Plain", WriterSheets.Plain.class, ROWS)), out);

        assertThat(out.size()).isPositive();
        assertThat(workbook.closes).isEqualTo(1);
    }

    private static WorkbookWriter writer(Workbook workbook) {
        return new WorkbookWriter(() -> workbook, new SheetWriter(WriterSupport.STANDARD));
    }

    /** An OOXML workbook that records calls to {@code close()}. */
    private static final class RecordingXssfWorkbook extends XSSFWorkbook {

        private int closes;

        @Override
        public void close() throws IOException {
            closes++;
            super.close();
        }
    }

    /** A workbook that is not an OOXML document itself and records calls to {@code close()}. */
    private static final class RecordingSxssfWorkbook extends SXSSFWorkbook {

        private int closes;

        @Override
        public void close() throws IOException {
            closes++;
            super.close();
        }
    }
}
