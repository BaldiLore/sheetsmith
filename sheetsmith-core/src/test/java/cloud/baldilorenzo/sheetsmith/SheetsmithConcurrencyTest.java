package cloud.baldilorenzo.sheetsmith;

import cloud.baldilorenzo.sheetsmith.fixtures.ValidSheets;
import cloud.baldilorenzo.sheetsmith.fixtures.WriterSheets;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SheetsmithConcurrencyTest {

    private static final int THREADS = 8;
    private static final int GENERATIONS = 32;

    @Test
    void parallelGenerationsOnOneInstanceProduceIdenticalContent() throws Exception {
        Sheetsmith sheetsmith = Sheetsmith.builder().build();
        List<SheetData<?>> sheets = List.of(
                SheetData.of("People", ValidSheets.Person.class, IntStream.range(0, 200)
                        .mapToObj(i -> new ValidSheets.Person("p" + i, i)).toList()),
                SheetData.of("Products", ValidSheets.Product.class, IntStream.range(0, 200)
                        .mapToObj(i -> new ValidSheets.Product(BigDecimal.valueOf(i), "c" + i, "x")).toList()),
                SheetData.of("Nulls", WriterSheets.Nullable.class, IntStream.range(0, 200)
                        .mapToObj(i -> new WriterSheets.Nullable("n" + i, i, LocalDate.of(2026, 1, 1).plusDays(i)))
                        .toList()));
        String expected = content(sheetsmith.generate(sheets));

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        try {
            List<Future<byte[]>> results = new ArrayList<>();
            for (int i = 0; i < GENERATIONS; i++) {
                results.add(executor.submit(() -> {
                    start.await();
                    return sheetsmith.generate(sheets);
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

    /** Sheet names, cell values and cell formats; the file bytes differ by their creation timestamp. */
    private static String content(byte[] bytes) throws IOException {
        StringBuilder text = new StringBuilder();
        DataFormatter formatter = new DataFormatter();
        try (XSSFWorkbook workbook = SheetsmithTest.read(bytes)) {
            for (Sheet sheet : workbook) {
                text.append('#').append(sheet.getSheetName()).append('\n');
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        text.append(formatter.formatCellValue(cell)).append('|')
                                .append(cell.getCellStyle().getDataFormatString()).append(';');
                    }
                    text.append('\n');
                }
            }
        }
        return text.toString();
    }
}
