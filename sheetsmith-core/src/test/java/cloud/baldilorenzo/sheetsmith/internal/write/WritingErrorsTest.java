package cloud.baldilorenzo.sheetsmith.internal.write;

import cloud.baldilorenzo.sheetsmith.SheetsmithGenerationException;
import cloud.baldilorenzo.sheetsmith.fixtures.WriterSheets;
import cloud.baldilorenzo.sheetsmith.value.Hyperlink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cloud.baldilorenzo.sheetsmith.internal.write.WriterSupport.sheet;
import static cloud.baldilorenzo.sheetsmith.internal.write.WriterSupport.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class WritingErrorsTest {

    @Test
    void nullElementIsReportedWithSheetAndRow() {
        List<WriterSheets.Plain> rows = Arrays.asList(new WriterSheets.Plain("a"), null);

        SheetsmithGenerationException exception = failure(sheet("Data", WriterSheets.Plain.class, rows));

        assertThat(exception.sheetName()).isEqualTo("Data");
        assertThat(exception.rowIndex()).isEqualTo(2);
        assertThat(exception.fieldName()).isEmpty();
        assertThat(exception).hasMessage("null element in the data list (sheet 'Data', row 2)");
    }

    @Test
    void converterFailureIsReportedWithSheetRowFieldAndCause() {
        List<WriterSheets.FailingConverter> rows = List.of(
                new WriterSheets.FailingConverter("ok"), new WriterSheets.FailingConverter("bad"));

        SheetsmithGenerationException exception = failure(sheet("Data", WriterSheets.FailingConverter.class, rows));

        assertThat(exception.sheetName()).isEqualTo("Data");
        assertThat(exception.rowIndex()).isEqualTo(1);
        assertThat(exception.fieldName()).contains("a");
        assertThat(exception.getCause()).isInstanceOf(IllegalStateException.class).hasMessage("cannot convert ok");
        assertThat(exception.getMessage()).contains("sheet 'Data'", "row 1", "field 'a'");
    }

    @Test
    void converterReturningNullIsReported() {
        SheetsmithGenerationException exception = failure(sheet("Data", WriterSheets.NullConverter.class,
                List.of(new WriterSheets.NullConverter("x"))));

        assertThat(exception.fieldName()).contains("a");
        assertThat(exception.getMessage()).contains("CellValue.blank()");
    }

    @Test
    void getterFailureIsReportedWithSheetRowFieldAndCause() {
        SheetsmithGenerationException exception = failure(sheet("Data", WriterSheets.FailingGetter.class,
                List.of(new WriterSheets.FailingGetter())));

        assertThat(exception.rowIndex()).isEqualTo(1);
        assertThat(exception.fieldName()).contains("a");
        assertThat(exception.getCause()).hasMessage("getter failed");
    }

    @Test
    void textLongerThanTheExcelLimitIsReported() {
        String text = "x".repeat(SheetWriter.MAX_TEXT_LENGTH + 1);

        SheetsmithGenerationException exception = failure(sheet("Data", WriterSheets.Plain.class,
                List.of(new WriterSheets.Plain("ok"), new WriterSheets.Plain(text))));

        assertThat(exception.rowIndex()).isEqualTo(2);
        assertThat(exception.fieldName()).contains("value");
        assertThat(exception.getMessage()).contains("32767");
    }

    @Test
    void textAtTheExcelLimitIsWritten() throws IOException {
        String text = "x".repeat(SheetWriter.MAX_TEXT_LENGTH);

        try (XSSFWorkbook workbook = write(sheet("Data", WriterSheets.Plain.class,
                List.of(new WriterSheets.Plain(text))))) {
            assertThat(workbook.getSheet("Data").getRow(1).getCell(0).getStringCellValue())
                    .hasSize(SheetWriter.MAX_TEXT_LENGTH);
        }
    }

    @Test
    void linkDescriptionLongerThanTheExcelLimitIsReported() {
        String description = "x".repeat(SheetWriter.MAX_TEXT_LENGTH + 1);

        SheetsmithGenerationException exception = failure(sheet("Links", WriterSheets.Links.class, List.of(
                new WriterSheets.Links("ok", Hyperlink.of("https://example.com")),
                new WriterSheets.Links("long", Hyperlink.of("https://example.com", description)))));

        assertThat(exception.sheetName()).isEqualTo("Links");
        assertThat(exception.rowIndex()).isEqualTo(2);
        assertThat(exception.fieldName()).contains("link");
        assertThat(exception).hasMessage("text of 32768 characters exceeds the Excel limit of 32767 "
                + "(sheet 'Links', row 2, field 'link')");
    }

    @Test
    void linkDescriptionAtTheExcelLimitIsWritten() throws IOException {
        String description = "x".repeat(SheetWriter.MAX_TEXT_LENGTH);

        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.Links.class,
                List.of(new WriterSheets.Links("long", Hyperlink.of("https://example.com", description)))))) {
            Cell cell = workbook.getSheet("Links").getRow(1).getCell(1);

            assertThat(cell.getStringCellValue()).hasSize(SheetWriter.MAX_TEXT_LENGTH);
            assertThat(cell.getHyperlink()).isNotNull();
        }
    }

    @Test
    void linkAddressLongerThanTheExcelLimitIsReportedWithoutDescription() {
        String address = "https://example.com/" + "x".repeat(SheetWriter.MAX_TEXT_LENGTH);

        SheetsmithGenerationException exception = failure(sheet("Links", WriterSheets.Links.class,
                List.of(new WriterSheets.Links("long", Hyperlink.of(address)))));

        assertThat(exception.rowIndex()).isEqualTo(1);
        assertThat(exception.fieldName()).contains("link");
        assertThat(exception.getMessage()).contains("32767", "sheet 'Links'", "row 1", "field 'link'");
    }

    @Test
    void linkFromAConverterOfAnotherTypeHasTheSameTextLimit() {
        WriterSheets.OrderNumber order = new WriterSheets.OrderNumber("x".repeat(SheetWriter.MAX_TEXT_LENGTH + 1));

        SheetsmithGenerationException exception = failure(sheet("Orders", WriterSheets.Orders.class,
                List.of(new WriterSheets.Orders("Northwind Paper", order)), CellWritingTest.ORDER_LINKS));

        assertThat(exception.rowIndex()).isEqualTo(1);
        assertThat(exception.fieldName()).contains("order");
        assertThat(exception).hasMessage("text of 32768 characters exceeds the Excel limit of 32767 "
                + "(sheet 'Orders', row 1, field 'order')");
    }

    private static final Hyperlink LINK = Hyperlink.of("https://example.com/items", "Item");

    @Test
    void sheetWithExactlyTheMaximumNumberOfLinksIsWritten() throws IOException {
        try (XSSFWorkbook workbook = write(manyLinks("Links", SheetWriter.MAX_HYPERLINKS))) {
            assertThat(workbook.getSheet("Links").getHyperlinkList()).hasSize(SheetWriter.MAX_HYPERLINKS);
        }
    }

    @Test
    void linkAboveTheExcelLimitIsReportedWithSheetRowAndField() {
        SheetsmithGenerationException exception = failure(manyLinks("Links", SheetWriter.MAX_HYPERLINKS + 1));

        assertThat(exception.sheetName()).isEqualTo("Links");
        assertThat(exception.rowIndex()).isEqualTo(SheetWriter.MAX_HYPERLINKS + 1);
        assertThat(exception.fieldName()).contains("link");
        assertThat(exception).hasMessage("hyperlink 65531 exceeds the Excel limit of 65530 hyperlinks in a sheet "
                + "(sheet 'Links', row 65531, field 'link')");
    }

    @Test
    void linksAreCountedOverAllTheColumnsOfTheSheet() {
        // two links per row: the 65,531st link is the first one of row 32,766
        List<WriterSheets.TwoLinkColumns> rows = Collections.nCopies(32_766,
                new WriterSheets.TwoLinkColumns(LINK, LINK));

        SheetsmithGenerationException exception = failure(sheet("Links", WriterSheets.TwoLinkColumns.class, rows));

        assertThat(exception.rowIndex()).isEqualTo(32_766);
        assertThat(exception.fieldName()).contains("first");
        assertThat(exception.getMessage()).contains("65530");
    }

    @Test
    void linksAreCountedPerSheet() throws IOException {
        try (XSSFWorkbook workbook = write(manyLinks("First", SheetWriter.MAX_HYPERLINKS),
                manyLinks("Second", SheetWriter.MAX_HYPERLINKS))) {
            assertThat(workbook.getSheet("First").getHyperlinkList()).hasSize(SheetWriter.MAX_HYPERLINKS);
            assertThat(workbook.getSheet("Second").getHyperlinkList()).hasSize(SheetWriter.MAX_HYPERLINKS);
        }
    }

    @Test
    void nullValuesAndLinksWrittenAsTextAreNotCounted() throws IOException {
        List<WriterSheets.LinksAndText> rows = new ArrayList<>(Collections.nCopies(SheetWriter.MAX_HYPERLINKS,
                new WriterSheets.LinksAndText(LINK, LINK)));
        rows.addAll(Collections.nCopies(10, new WriterSheets.LinksAndText(null, LINK)));

        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.LinksAndText.class, rows))) {
            assertThat(workbook.getSheet("Links").getHyperlinkList()).hasSize(SheetWriter.MAX_HYPERLINKS);
        }
    }

    private static WritableSheet manyLinks(String name, int count) {
        return sheet(name, WriterSheets.ManyLinks.class, Collections.nCopies(count, new WriterSheets.ManyLinks(LINK)));
    }

    @Test
    void rowCountAboveTheExcelLimitIsReportedBeforeWriting() {
        // title + header + data rows = limit + 1
        List<WriterSheets.Layout> rows = Collections.nCopies(SheetWriter.MAX_ROWS - 1,
                new WriterSheets.Layout("c", "d", 1));

        SheetsmithGenerationException exception = failure(sheet("Big", WriterSheets.Layout.class, rows));

        assertThat(exception.sheetName()).isEqualTo("Big");
        assertThat(exception.rowIndex()).isZero();
        assertThat(exception.getMessage()).contains("1048577", "1048576");
    }

    private static SheetsmithGenerationException failure(WritableSheet sheet) {
        SheetsmithGenerationException exception = catchThrowableOfType(SheetsmithGenerationException.class,
                () -> write(sheet));
        assertThat(exception).isNotNull();
        return exception;
    }
}
