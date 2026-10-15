package cloud.baldilorenzo.sheetsmith.internal.write;

import cloud.baldilorenzo.sheetsmith.SheetsmithDefaults;
import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;
import cloud.baldilorenzo.sheetsmith.fixtures.WriterSheets;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import cloud.baldilorenzo.sheetsmith.value.Hyperlink;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

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

    private static final LocalDateTime MOMENT = LocalDateTime.of(2026, 10, 14, 9, 30, 15, 123_456_789);

    private static WriterSheets.AdditionalTypes additionalTypes(OptionalInt optionalInt, OptionalLong optionalLong,
                                                                OptionalDouble optionalDouble) {
        try {
            return new WriterSheets.AdditionalTypes(
                    UUID.fromString("3f2a9c1e-7b4d-4e8a-9f61-2c5d8e0b4a17"), URI.create("https://example.com/a?b=c"),
                    URI.create("https://example.com/docs").toURL(), new File("reports/q3.xlsx"),
                    Currency.getInstance("EUR"), ZoneId.of("Europe/Rome"), Locale.forLanguageTag("it-IT"),
                    YearMonth.of(2026, 10), Year.of(2026), new AtomicBoolean(true), optionalInt, optionalLong,
                    optionalDouble, java.sql.Date.valueOf(LocalDate.of(2026, 10, 14)), Timestamp.valueOf(MOMENT),
                    Year.of(2026));
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static WriterSheets.AdditionalTypes presentAdditionalTypes() {
        return additionalTypes(OptionalInt.of(42), OptionalLong.of(9_000_000_000L), OptionalDouble.of(1.5));
    }

    @Test
    void additionalBuiltInTypesAreWrittenWithTheExpectedCellType() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Additional", WriterSheets.AdditionalTypes.class,
                List.of(presentAdditionalTypes())))) {
            Row row = workbook.getSheet("Additional").getRow(1);

            assertText(row.getCell(0), "3f2a9c1e-7b4d-4e8a-9f61-2c5d8e0b4a17");
            assertText(row.getCell(1), "https://example.com/a?b=c");
            assertText(row.getCell(2), "https://example.com/docs");
            assertText(row.getCell(3), new File("reports/q3.xlsx").getPath());
            assertText(row.getCell(4), "EUR");
            assertText(row.getCell(5), "Europe/Rome");
            assertText(row.getCell(6), "it-IT");
            assertText(row.getCell(7), "2026-10");
            assertNumber(row.getCell(8), 2026, "General");
            assertThat(row.getCell(9).getCellType()).isEqualTo(CellType.BOOLEAN);
            assertThat(row.getCell(9).getBooleanCellValue()).isTrue();
            assertNumber(row.getCell(10), 42, "General");
            assertNumber(row.getCell(11), 9_000_000_000d, "General");
            assertNumber(row.getCell(12), 1.5, "General");
            assertThat(row.getCell(13).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(row.getCell(13).getLocalDateTimeCellValue()).isEqualTo(LocalDate.of(2026, 10, 14).atStartOfDay());
            assertThat(row.getCell(13).getCellStyle().getDataFormatString()).isEqualTo("yyyy-mm-dd");
            assertThat(row.getCell(14).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(row.getCell(14).getLocalDateTimeCellValue())
                    .isEqualTo(MOMENT.truncatedTo(ChronoUnit.MILLIS));
            assertThat(row.getCell(14).getCellStyle().getDataFormatString()).isEqualTo("yyyy-mm-dd hh:mm:ss");
            assertNumber(row.getCell(15), 2026, "0");
        }
    }

    @Test
    void emptyOptionalsAreWrittenAsBlankCellsThatKeepTheirStyle() throws IOException {
        List<WriterSheets.AdditionalTypes> rows = List.of(
                additionalTypes(OptionalInt.empty(), OptionalLong.empty(), OptionalDouble.empty()),
                presentAdditionalTypes());

        try (XSSFWorkbook workbook = write(sheet("Optionals", WriterSheets.AdditionalTypes.class, rows))) {
            Row empty = workbook.getSheet("Optionals").getRow(1);
            Row present = workbook.getSheet("Optionals").getRow(2);

            for (int j = 10; j <= 12; j++) {
                Cell cell = empty.getCell(j);
                assertThat(cell).isNotNull();
                assertThat(cell.getCellType()).isEqualTo(CellType.BLANK);
                assertThat(cell.getCellStyle().getBorderTop()).isEqualTo(BorderStyle.THIN);
                assertThat(cell.getCellStyle().getBorderLeft()).isEqualTo(BorderStyle.THIN);
                assertThat(cell.getCellStyle().getIndex()).isEqualTo(present.getCell(j).getCellStyle().getIndex());
            }
        }
    }

    @Test
    void yearFollowsTheNumberFormat() throws IOException {
        SheetsmithDefaults formats = new SheetsmithDefaults("dd/mm/yyyy", "dd/mm/yyyy hh:mm", "#,##0.00",
                TablePreset.NONE, "#4472C4");
        WorkbookWriter writer = new WorkbookWriter(WorkbookSupplier.XSSF, formats);

        try (XSSFWorkbook workbook = write(writer, sheet("Years", WriterSheets.AdditionalTypes.class,
                List.of(presentAdditionalTypes())))) {
            Row row = workbook.getSheet("Years").getRow(1);

            assertNumber(row.getCell(8), 2026, "#,##0.00");
            assertNumber(row.getCell(15), 2026, "0");
        }
    }

    // ---- hyperlinks ----

    @Test
    void httpsLinkWithDescriptionShowsTheDescriptionAndOpensTheAddress() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.Links.class, List.of(
                new WriterSheets.Links("Docs", Hyperlink.of("https://example.com/docs?page=2", "Documentation")))))) {
            Cell cell = workbook.getSheet("Links").getRow(1).getCell(1);

            assertText(cell, "Documentation");
            assertThat(cell.getHyperlink()).isNotNull();
            assertThat(cell.getHyperlink().getType()).isEqualTo(HyperlinkType.URL);
            assertThat(cell.getHyperlink().getAddress()).isEqualTo("https://example.com/docs?page=2");
        }
    }

    @Test
    void mailtoLinkWithoutDescriptionShowsTheAddress() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.Links.class, List.of(
                new WriterSheets.Links("Support", Hyperlink.of("mailto:support@example.com")))))) {
            Cell cell = workbook.getSheet("Links").getRow(1).getCell(1);

            assertText(cell, "mailto:support@example.com");
            assertThat(cell.getHyperlink().getType()).isEqualTo(HyperlinkType.EMAIL);
            assertThat(cell.getHyperlink().getAddress()).isEqualTo("mailto:support@example.com");
        }
    }

    @Test
    void nonAsciiAddressIsLinkedInAsciiFormAndShownReadable() throws IOException {
        Hyperlink link = Hyperlink.of("https://example.com/caffè/menù?q=perché");

        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.Links.class,
                List.of(new WriterSheets.Links("Menu", link))))) {
            Cell cell = workbook.getSheet("Links").getRow(1).getCell(1);

            assertText(cell, "https://example.com/caffè/menù?q=perché");
            assertThat(cell.getHyperlink().getAddress())
                    .isEqualTo("https://example.com/caff%C3%A8/men%C3%B9?q=perch%C3%A9");
        }
    }

    @Test
    void everyRowHasItsOwnLink() throws IOException {
        List<WriterSheets.Links> rows = List.of(
                new WriterSheets.Links("A", Hyperlink.of("https://example.com/a", "First")),
                new WriterSheets.Links("B", Hyperlink.of("https://example.com/b", "Second")),
                new WriterSheets.Links("C", Hyperlink.of("mailto:c@example.com", "Third")));

        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.Links.class, rows))) {
            Sheet sheet = workbook.getSheet("Links");

            assertThat(sheet.getRow(1).getCell(1).getHyperlink().getAddress()).isEqualTo("https://example.com/a");
            assertThat(sheet.getRow(2).getCell(1).getHyperlink().getAddress()).isEqualTo("https://example.com/b");
            assertThat(sheet.getRow(3).getCell(1).getHyperlink().getAddress()).isEqualTo("mailto:c@example.com");
            assertThat(sheet.getHyperlinkList()).hasSize(3);
            assertThat(sheet.getRow(1).getCell(0).getHyperlink()).isNull();
        }
    }

    @Test
    void nullLinkProducesAnEmptyCellWithoutLinkThatKeepsItsStyle() throws IOException {
        List<WriterSheets.GridLinks> rows = List.of(
                new WriterSheets.GridLinks("None", null),
                new WriterSheets.GridLinks("Some", Hyperlink.of("https://example.com")));

        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.GridLinks.class, rows))) {
            Cell cell = workbook.getSheet("Links").getRow(1).getCell(1);

            assertThat(cell.getCellType()).isEqualTo(CellType.BLANK);
            assertThat(cell.getHyperlink()).isNull();
            assertThat(cell.getCellStyle().getBorderTop()).isEqualTo(BorderStyle.THIN);
            assertThat(font(cell).getUnderline()).isEqualTo(Font.U_NONE);
            assertThat(isLinkBlue(font(cell))).isFalse();
            assertThat(workbook.getSheet("Links").getHyperlinkList()).hasSize(1);
        }
    }

    @Test
    void fieldConverterReturningTextProducesAPlainCellWithoutLink() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.ConvertedLinks.class,
                List.of(new WriterSheets.ConvertedLinks(Hyperlink.of("https://example.com", "Example")))))) {
            Cell cell = workbook.getSheet("Links").getRow(1).getCell(0);

            assertText(cell, "EXAMPLE");
            assertThat(cell.getHyperlink()).isNull();
            assertThat(font(cell).getUnderline()).isEqualTo(Font.U_NONE);
            assertThat(workbook.getSheet("Links").getHyperlinkList()).isEmpty();
        }
    }

    @Test
    void applicationConverterReturningTextForHyperlinkProducesAPlainCellWithoutLink() throws IOException {
        Map<Class<?>, CellConverter<?>> converters = Map.of(Hyperlink.class,
                (CellConverter<Hyperlink>) (value, context) -> CellValue.text(value.address().toString()));

        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.Links.class,
                List.of(new WriterSheets.Links("A", Hyperlink.of("https://example.com", "Example"))), converters))) {
            Cell cell = workbook.getSheet("Links").getRow(1).getCell(1);

            assertText(cell, "https://example.com");
            assertThat(cell.getHyperlink()).isNull();
        }
    }

    @Test
    void fieldConverterReturningALinkKeepsTheLinkWithItsNewDescription() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.RelabelledLinks.class,
                List.of(new WriterSheets.RelabelledLinks(Hyperlink.of("https://example.com/docs", "docs")))))) {
            Cell cell = workbook.getSheet("Links").getRow(1).getCell(0);

            assertText(cell, "Open docs");
            assertThat(cell.getHyperlink().getType()).isEqualTo(HyperlinkType.URL);
            assertThat(cell.getHyperlink().getAddress()).isEqualTo("https://example.com/docs");
            assertThat(font(cell).getUnderline()).isEqualTo(Font.U_SINGLE);
            assertThat(isLinkBlue(font(cell))).isTrue();
        }
    }

    @Test
    void applicationConverterForADomainTypeCanProduceALink() throws IOException {
        List<WriterSheets.Orders> rows = List.of(
                new WriterSheets.Orders("Northwind Paper", new WriterSheets.OrderNumber("SO-10482")),
                new WriterSheets.Orders("Blue Harbour Tools", null));

        try (XSSFWorkbook workbook = write(sheet("Orders", WriterSheets.Orders.class, rows, ORDER_LINKS))) {
            Sheet sheet = workbook.getSheet("Orders");
            Cell linked = sheet.getRow(1).getCell(1);
            Cell empty = sheet.getRow(2).getCell(1);

            assertText(linked, "SO-10482");
            assertThat(linked.getHyperlink().getType()).isEqualTo(HyperlinkType.URL);
            assertThat(linked.getHyperlink().getAddress()).isEqualTo("https://shop.example.com/orders/SO-10482");
            assertThat(font(linked).getUnderline()).isEqualTo(Font.U_SINGLE);
            assertThat(isLinkBlue(font(linked))).isTrue();
            assertThat(empty.getCellType()).isEqualTo(CellType.BLANK);
            assertThat(empty.getHyperlink()).isNull();
            assertThat(sheet.getHyperlinkList()).hasSize(1);
        }
    }

    /** Writes an order number as a link to the page of the order. */
    static final Map<Class<?>, CellConverter<?>> ORDER_LINKS = Map.of(WriterSheets.OrderNumber.class,
            (CellConverter<WriterSheets.OrderNumber>) (value, context) -> CellValue.link(
                    Hyperlink.of("https://shop.example.com/orders/" + value.value(), value.value())));

    @Test
    void linkCellsAreUnderlinedAndBlueWithoutPresetOrStyles() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.Links.class, List.of(
                new WriterSheets.Links("A", Hyperlink.of("https://example.com", "Example")))))) {
            Row row = workbook.getSheet("Links").getRow(1);

            assertThat(font(row.getCell(1)).getUnderline()).isEqualTo(Font.U_SINGLE);
            assertThat(isLinkBlue(font(row.getCell(1)))).isTrue();
            assertThat(font(row.getCell(0)).getUnderline()).isEqualTo(Font.U_NONE);
            assertThat(isLinkBlue(font(row.getCell(0)))).isFalse();
            assertThat(row.getCell(1).getCellStyle().getDataFormatString()).isEqualTo("General");
        }
    }

    @Test
    void underlineAndFontColourSetByTheCascadeWinOverTheLinkAppearance() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.PlainStyledLinks.class,
                List.of(new WriterSheets.PlainStyledLinks(Hyperlink.of("https://example.com", "Example")))))) {
            Cell cell = workbook.getSheet("Links").getRow(1).getCell(0);

            assertThat(cell.getHyperlink()).isNotNull();
            assertThat(font(cell).getUnderline()).isEqualTo(Font.U_NONE);
            assertThat(font(cell).getXSSFColor().getRGB()).containsExactly(0xC0, 0x00, 0x00);
        }
    }

    @Test
    void darkPresetKeepsItsContrastingFontColourAndGetsTheUnderline() throws IOException {
        List<WriterSheets.DarkLinks> rows = List.of(
                new WriterSheets.DarkLinks("A", Hyperlink.of("https://example.com/a", "First")),
                new WriterSheets.DarkLinks("B", Hyperlink.of("https://example.com/b", "Second")));

        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.DarkLinks.class, rows))) {
            Sheet sheet = workbook.getSheet("Links");
            for (int r = 1; r <= 2; r++) {
                XSSFFont name = font(sheet.getRow(r).getCell(0));
                XSSFFont link = font(sheet.getRow(r).getCell(1));

                assertThat(name.getXSSFColor()).as("preset font colour of row %d", r).isNotNull();
                assertThat(link.getXSSFColor().getRGB()).isEqualTo(name.getXSSFColor().getRGB());
                assertThat(isLinkBlue(link)).isFalse();
                assertThat(link.getUnderline()).isEqualTo(Font.U_SINGLE);
            }
        }
    }

    @Test
    void linkCellStylesDoNotGrowWithTheNumberOfRows() throws IOException {
        assertThat(cellStyles(links(10))).isEqualTo(cellStyles(links(200)));
    }

    @Test
    void linksAreKeptInTheReopenedFile() throws IOException {
        WorkbookWriter writer = new WorkbookWriter(WorkbookSupplier.XSSF, WriterSupport.STANDARD);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.write(List.of(sheet("Links", WriterSheets.Links.class, List.of(
                new WriterSheets.Links("Docs", Hyperlink.of("https://example.com/docs", "Documentation")),
                new WriterSheets.Links("Mail", Hyperlink.of("mailto:info@example.com"))))), out);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = workbook.getSheet("Links");
            Cell docs = sheet.getRow(1).getCell(1);
            Cell mail = sheet.getRow(2).getCell(1);

            assertText(docs, "Documentation");
            assertThat(docs.getHyperlink().getType()).isEqualTo(HyperlinkType.URL);
            assertThat(docs.getHyperlink().getAddress()).isEqualTo("https://example.com/docs");
            assertText(mail, "mailto:info@example.com");
            assertThat(mail.getHyperlink().getType()).isEqualTo(HyperlinkType.EMAIL);
            assertThat(mail.getHyperlink().getAddress()).isEqualTo("mailto:info@example.com");
        }
    }

    private static List<WriterSheets.Links> links(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> new WriterSheets.Links("n" + i, Hyperlink.of("https://example.com/" + i, "Link " + i)))
                .toList();
    }

    private static int cellStyles(List<WriterSheets.Links> rows) throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Links", WriterSheets.Links.class, rows))) {
            return workbook.getNumCellStyles();
        }
    }

    /** Whether the font has the colour of the links, {@code #0563C1}. */
    private static boolean isLinkBlue(XSSFFont font) {
        return font.getXSSFColor() != null
                && Arrays.equals(font.getXSSFColor().getRGB(), new byte[]{0x05, 0x63, (byte) 0xC1});
    }

    private static XSSFFont font(Cell cell) {
        return ((XSSFCellStyle) cell.getCellStyle()).getFont();
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
