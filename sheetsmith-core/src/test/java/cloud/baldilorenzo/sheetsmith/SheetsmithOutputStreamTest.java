package cloud.baldilorenzo.sheetsmith;

import cloud.baldilorenzo.sheetsmith.fixtures.PresetSheets;
import cloud.baldilorenzo.sheetsmith.fixtures.ValidSheets;
import cloud.baldilorenzo.sheetsmith.fixtures.WriterSheets;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class SheetsmithOutputStreamTest {

    private static final int THREADS = 8;
    private static final int GENERATIONS = 32;

    private final Sheetsmith sheetsmith = Sheetsmith.builder()
            .defaults(new SheetsmithDefaults("dd/mm/yyyy", "dd/mm/yyyy hh:mm", "#,##0.00", TablePreset.MEDIUM,
                    "#1F4E79"))
            .build();

    @Test
    void streamOutputHasTheSameContentAsTheByteArrayOutput() throws IOException {
        List<SheetData<?>> sheets = sheets();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        sheetsmith.generate(sheets, out);

        assertThat(content(out.toByteArray())).isEqualTo(content(sheetsmith.generate(sheets)));
    }

    @Test
    void streamIsNotClosed() {
        RecordingStream out = new RecordingStream();

        sheetsmith.generate(sheets(), out);

        assertThat(out.closed).isFalse();
        assertThat(out.size()).isPositive();
    }

    @Test
    void streamIsFlushedAfterTheFileIsWritten() {
        RecordingStream out = new RecordingStream();

        sheetsmith.generate(sheets(), out);

        assertThat(out.sizeAtLastFlush).isPositive().isEqualTo(out.size());
    }

    @Test
    void configurationErrorWritesNothingToTheStream() {
        RecordingStream out = new RecordingStream();

        assertThatExceptionOfType(SheetsmithConfigurationException.class).isThrownBy(() -> sheetsmith.generate(
                List.of(SheetData.of("Invalid/name", ValidSheets.Person.class, List.of())), out));

        assertThat(out.size()).isZero();
    }

    @Test
    void generationErrorWritesNothingToTheStream() {
        RecordingStream out = new RecordingStream();
        List<ValidSheets.Person> people = Arrays.asList(new ValidSheets.Person("ada", 36), null);

        assertThatExceptionOfType(SheetsmithGenerationException.class).isThrownBy(() -> sheetsmith.generate(
                List.of(SheetData.of("People", ValidSheets.Person.class, people)), out));

        assertThat(out.size()).isZero();
    }

    @Test
    void failureOfTheStreamIsReportedAsUncheckedIoExceptionWithTheOriginalCause() {
        IOException failure = new IOException("disk full");
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw failure;
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                throw failure;
            }
        };

        UncheckedIOException exception = catchThrowableOfType(UncheckedIOException.class,
                () -> sheetsmith.generate(sheets(), out));

        assertThat(exception).hasCause(failure);
    }

    @Test
    void failureOfTheStreamLaterInTheFileIsReportedWithTheOriginalCause() {
        IOException failure = new IOException("disk full");
        OutputStream failing = new OutputStream() {
            private int written;

            @Override
            public void write(int b) throws IOException {
                write(new byte[] {(byte) b}, 0, 1);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                if (written + len > 2048) {
                    throw failure;
                }
                written += len;
            }
        };

        UncheckedIOException exception = catchThrowableOfType(UncheckedIOException.class,
                () -> sheetsmith.generate(sheets(), failing));

        assertThat(exception).hasCause(failure);
    }

    @Test
    void nullStreamIsRejected() {
        assertThatNullPointerException().isThrownBy(() -> sheetsmith.generate(sheets(), null));
    }

    @Test
    void parallelGenerationsToSeparateStreamsProduceValidIdenticalFiles() throws Exception {
        List<SheetData<?>> sheets = sheets();
        String expected = content(sheetsmith.generate(sheets));

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        try {
            List<Future<byte[]>> results = new ArrayList<>();
            for (int i = 0; i < GENERATIONS; i++) {
                results.add(executor.submit(() -> {
                    start.await();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    sheetsmith.generate(sheets, out);
                    return out.toByteArray();
                }));
            }
            start.countDown();

            for (Future<byte[]> result : results) {
                assertThat(content(result.get())).isEqualTo(expected);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static List<SheetData<?>> sheets() {
        return List.of(
                SheetData.of("Invoices", PresetSheets.Invoice.class, IntStream.rangeClosed(1, 20)
                        .mapToObj(i -> new PresetSheets.Invoice("INV-" + i, "Customer " + i,
                                LocalDate.of(2026, 1, i), BigDecimal.valueOf(i * 100L), i % 2 == 0))
                        .toList()),
                SheetData.of("People", ValidSheets.Person.class, IntStream.range(0, 50)
                        .mapToObj(i -> new ValidSheets.Person("p" + i, i)).toList()),
                SheetData.of("Nulls", WriterSheets.Nullable.class, IntStream.range(0, 30)
                        .mapToObj(i -> new WriterSheets.Nullable(i % 3 == 0 ? null : "n" + i, i,
                                LocalDate.of(2026, 1, 1).plusDays(i)))
                        .toList()));
    }

    /**
     * Sheet names, merged regions, and for every cell its value, style index and main style attributes. The file
     * bytes are not compared, because the file metadata contains a creation timestamp.
     */
    private static String content(byte[] bytes) throws IOException {
        StringBuilder text = new StringBuilder();
        DataFormatter formatter = new DataFormatter();
        try (XSSFWorkbook workbook = SheetsmithTest.read(bytes)) {
            text.append("styles=").append(workbook.getNumCellStyles()).append('\n');
            for (Sheet sheet : workbook) {
                text.append('#').append(sheet.getSheetName()).append(' ').append(sheet.getMergedRegions())
                        .append('\n');
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        XSSFCellStyle style = (XSSFCellStyle) cell.getCellStyle();
                        text.append(formatter.formatCellValue(cell)).append('|')
                                .append(style.getIndex()).append('|')
                                .append(style.getDataFormatString()).append('|')
                                .append(style.getFont().getBold()).append('|')
                                .append(argb(style.getFillForegroundColorColor())).append('|')
                                .append(style.getBorderTop()).append(style.getBorderBottom())
                                .append(style.getBorderLeft()).append(style.getBorderRight()).append(';');
                    }
                    text.append('\n');
                }
            }
        }
        return text.toString();
    }

    private static String argb(XSSFColor color) {
        return color == null ? "none" : color.getARGBHex();
    }

    /** Records whether the stream is closed and how much content it held at the last flush. */
    private static final class RecordingStream extends ByteArrayOutputStream {

        private boolean closed;
        private int sizeAtLastFlush = -1;

        @Override
        public void flush() {
            sizeAtLastFlush = size();
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
