package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.internal.metadata.ColumnMetadata;
import cloud.baldilorenzo.sheetsmith.internal.metadata.SheetMetadata;
import cloud.baldilorenzo.sheetsmith.style.Align;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.Fill;
import cloud.baldilorenzo.sheetsmith.style.Script;
import cloud.baldilorenzo.sheetsmith.style.Underline;
import cloud.baldilorenzo.sheetsmith.style.VerticalAlign;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cloud.baldilorenzo.sheetsmith.internal.style.TestSheets.column;
import static cloud.baldilorenzo.sheetsmith.internal.style.TestSheets.sheet;
import static org.assertj.core.api.Assertions.assertThat;

class StyleCacheTest {

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private final StyleCache cache = new StyleCache(workbook);

    @AfterEach
    void closeWorkbook() throws IOException {
        workbook.close();
    }

    @Test
    void equalAttributesGiveTheSameCellStyle() {
        CellStyle first = cache.get(StyleAttributes.builder().bold(true).fontSize(12).build());
        CellStyle second = cache.get(StyleAttributes.builder().fontSize(12).bold(true).build());
        CellStyle other = cache.get(StyleAttributes.builder().bold(true).build());

        assertThat(second).isSameAs(first);
        assertThat(other).isNotSameAs(first);
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    void fontsAndDataFormatsAreDeduplicated() {
        XSSFCellStyle red = (XSSFCellStyle) cache.get(
                StyleAttributes.builder().bold(true).fillColor("RED").dataFormat("0.00").build());
        XSSFCellStyle blue = (XSSFCellStyle) cache.get(
                StyleAttributes.builder().bold(true).fillColor("BLUE").dataFormat("0.00").build());
        int fontsBefore = workbook.getNumberOfFonts();

        cache.get(StyleAttributes.builder().bold(true).fillColor("GREEN").build());

        assertThat(red.getFontIndex()).isEqualTo(blue.getFontIndex());
        assertThat(red.getDataFormat()).isEqualTo(blue.getDataFormat());
        assertThat(workbook.getNumberOfFonts()).isEqualTo(fontsBefore);
    }

    @Test
    void styleWithoutFontAttributesKeepsTheDefaultFont() {
        XSSFCellStyle style = (XSSFCellStyle) cache.get(StyleAttributes.builder().align(Align.CENTER).build());

        assertThat(style.getFontIndex()).isZero();
    }

    @Test
    void fillColourWithoutPatternIsSolid() {
        CellStyle implicit = cache.get(StyleAttributes.builder().fillColor("#FF0000").build());
        CellStyle explicit = cache.get(StyleAttributes.builder().fillColor("#FF0000")
                .fillPattern(Fill.SOLID_FOREGROUND).build());
        CellStyle patterned = cache.get(StyleAttributes.builder().fillColor("#FF0000")
                .fillPattern(Fill.BRICKS).build());

        assertThat(implicit.getFillPattern()).isEqualTo(FillPatternType.SOLID_FOREGROUND);
        assertThat(explicit).isSameAs(implicit);
        assertThat(patterned.getFillPattern()).isEqualTo(FillPatternType.BRICKS);
    }

    @Test
    void everyAttributeIsAppliedToThePoiStyle() {
        XSSFCellStyle style = (XSSFCellStyle) cache.get(StyleAttributes.builder()
                .align(Align.RIGHT).verticalAlign(VerticalAlign.TOP).wrapText(true).shrinkToFit(true)
                .rotation(255).indent(2)
                .borderTop(Border.THIN).borderBottom(Border.DOUBLE).borderLeft(Border.DASHED)
                .borderRight(Border.THICK)
                .borderTopColor("#112233").borderBottomColor("RED").borderLeftColor("#445566")
                .borderRightColor("BLUE")
                .fillColor("#FFEEDD").fillBackgroundColor("YELLOW").fillPattern(Fill.FINE_DOTS)
                .fontName("Arial").fontSize(14).bold(true).italic(true).strikeout(true)
                .underline(Underline.DOUBLE).fontColor("#010203").script(Script.SUB)
                .dataFormat("dd/mm/yyyy").locked(false).hidden(true).quotePrefix(true)
                .build());

        assertThat(style.getAlignment()).isEqualTo(HorizontalAlignment.RIGHT);
        assertThat(style.getVerticalAlignment()).isEqualTo(VerticalAlignment.TOP);
        assertThat(style.getWrapText()).isTrue();
        assertThat(style.getShrinkToFit()).isTrue();
        assertThat(style.getRotation()).isEqualTo((short) 255);
        assertThat(style.getIndention()).isEqualTo((short) 2);
        assertThat(style.getBorderTop()).isEqualTo(BorderStyle.THIN);
        assertThat(style.getBorderBottom()).isEqualTo(BorderStyle.DOUBLE);
        assertThat(style.getBorderLeft()).isEqualTo(BorderStyle.DASHED);
        assertThat(style.getBorderRight()).isEqualTo(BorderStyle.THICK);
        assertThat(style.getTopBorderXSSFColor().getRGB()).containsExactly(0x11, 0x22, 0x33);
        assertThat(style.getBottomBorderColor()).isEqualTo(IndexedColors.RED.getIndex());
        assertThat(style.getLeftBorderXSSFColor().getRGB()).containsExactly(0x44, 0x55, 0x66);
        assertThat(style.getRightBorderColor()).isEqualTo(IndexedColors.BLUE.getIndex());
        assertThat(style.getFillForegroundXSSFColor().getRGB()).containsExactly(0xFF, 0xEE, 0xDD);
        assertThat(style.getFillBackgroundColor()).isEqualTo(IndexedColors.YELLOW.getIndex());
        assertThat(style.getFillPattern()).isEqualTo(FillPatternType.FINE_DOTS);
        assertThat(style.getDataFormatString()).isEqualTo("dd/mm/yyyy");
        assertThat(style.getLocked()).isFalse();
        assertThat(style.getHidden()).isTrue();
        assertThat(style.getQuotePrefixed()).isTrue();

        XSSFFont font = style.getFont();
        assertThat(font.getFontName()).isEqualTo("Arial");
        assertThat(font.getFontHeightInPoints()).isEqualTo((short) 14);
        assertThat(font.getBold()).isTrue();
        assertThat(font.getItalic()).isTrue();
        assertThat(font.getStrikeout()).isTrue();
        assertThat(font.getUnderline()).isEqualTo(Font.U_DOUBLE);
        assertThat(font.getTypeOffset()).isEqualTo(Font.SS_SUB);
        assertThat(font.getXSSFColor().getRGB()).containsExactly(0x01, 0x02, 0x03);
    }

    @Test
    void worksWithStreamingWorkbooks() throws IOException {
        try (SXSSFWorkbook streaming = new SXSSFWorkbook()) {
            StyleCache streamingCache = new StyleCache(streaming);

            CellStyle style = streamingCache.get(StyleAttributes.builder().bold(true).fillColor("#00FF00").build());

            assertThat(style).isInstanceOf(XSSFCellStyle.class);
            assertThat(streamingCache.get(StyleAttributes.builder().fillColor("#00FF00").bold(true).build()))
                    .isSameAs(style);
        }
    }

    @Test
    void zebraSheetWithTenThousandRowsCreatesStylesPerRoleNotPerCell() {
        SheetMetadata.BodySlots body = new SheetMetadata.BodySlots(
                StyleAttributes.builder().border(Border.THIN).build(),
                StyleAttributes.builder().fillColor("#F2F2F2").build(),
                StyleAttributes.builder().fillColor("#FFFFFF").build(),
                StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY);
        ColumnMetadata amount = column("amount", StyleAttributes.EMPTY, ColumnMetadata.Slots.EMPTY, "#,##0.00");
        List<ColumnMetadata> columns = List.of(column("name"), amount, column("note"));
        StyleResolver resolver = new StyleResolver(sheet(SheetMetadata.HeaderSlots.EMPTY, body, Border.MEDIUM,
                "DARK_BLUE", columns));
        int stylesBefore = workbook.getNumCellStyles();
        int rows = 10_000;

        Sheet sheet = workbook.createSheet("zebra");
        Set<StyleAttributes> distinct = new HashSet<>();
        for (int i = 1; i <= rows; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < columns.size(); j++) {
                StyleAttributes attributes = resolver.body(columns.get(j), CellRole.data(i, rows, j, columns.size()));
                distinct.add(attributes);
                row.createCell(j).setCellStyle(cache.get(attributes));
            }
        }

        // three columns, each with odd, even and last-row roles (the last row is even)
        assertThat(distinct).hasSize(9);
        assertThat(cache.size()).isEqualTo(distinct.size());
        assertThat(workbook.getNumCellStyles() - stylesBefore).isEqualTo(distinct.size());
    }
}
