package cloud.baldilorenzo.sheetsmith.internal.write;

import cloud.baldilorenzo.sheetsmith.SheetsmithDefaults;
import cloud.baldilorenzo.sheetsmith.fixtures.TemplateSheets;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static cloud.baldilorenzo.sheetsmith.internal.write.WriterSupport.sheet;
import static cloud.baldilorenzo.sheetsmith.internal.write.WriterSupport.write;
import static org.assertj.core.api.Assertions.assertThat;

class TemplateWritingTest {

    @Test
    void sheetIsWrittenWithTheConfigurationOfItsTemplate() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Report", TemplateSheets.InheritsEverything.class,
                List.of(new TemplateSheets.InheritsEverything("A-1", BigDecimal.ONE))))) {
            XSSFSheet sheet = workbook.getSheet("Report");

            assertThat(sheet.getPaneInformation()).as("freezeHeader FALSE").isNull();
            assertThat(sheet.getCTWorksheet().getAutoFilter().getRef()).as("autoFilter TRUE").isEqualTo("A2:B3");
            assertThat(sheet.getColumnWidth(0)).as("autoSizeColumns FALSE")
                    .isEqualTo(sheet.getDefaultColumnWidth() * 256);
            XSSFCellStyle header = (XSSFCellStyle) sheet.getRow(1).getCell(1).getCellStyle();
            assertThat(header.getFont().getBold()).isTrue();
            assertThat(header.getBorderTop()).as("frame").isEqualTo(BorderStyle.THIN);
            assertThat(header.getBorderRight()).as("lastColumn slot").isEqualTo(BorderStyle.THIN);
        }
    }

    @Test
    void titleStyleOfTheTemplateIsAppliedToTheTitle() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Report", TemplateSheets.InheritsEverything.class, List.of()))) {
            XSSFSheet sheet = workbook.getSheet("Report");
            XSSFCellStyle title = (XSSFCellStyle) sheet.getRow(0).getCell(0).getCellStyle();

            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Report");
            assertThat(title.getFont().getFontHeightInPoints()).isEqualTo((short) 16);
        }
    }

    @Test
    void sheetWithoutTitleHasNoTitleRowWhenTheTemplateHasATitleStyle() throws IOException {
        try (XSSFWorkbook workbook = write(sheet("Codes", TemplateSheets.WithoutTitle.class,
                List.of(new TemplateSheets.WithoutTitle("A-1"))))) {
            XSSFSheet sheet = workbook.getSheet("Codes");

            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Code");
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("A-1");
            assertThat(sheet.getMergedRegions()).isEmpty();
        }
    }

    @Test
    void twoSheetClassesReferencingTheSameTemplateInOneWorkbook() throws IOException {
        try (XSSFWorkbook workbook = write(
                sheet("Report", TemplateSheets.InheritsEverything.class,
                        List.of(new TemplateSheets.InheritsEverything("A-1", BigDecimal.ONE))),
                sheet("Purchases", TemplateSheets.OwnAccent.class,
                        List.of(new TemplateSheets.OwnAccent("ACME"))))) {
            XSSFSheet report = workbook.getSheet("Report");
            XSSFSheet purchases = workbook.getSheet("Purchases");

            for (XSSFSheet sheet : List.of(report, purchases)) {
                assertThat(sheet.getPaneInformation()).isNull();
                assertThat(sheet.getCTWorksheet().isSetAutoFilter()).isTrue();
                assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo(sheet.getSheetName());
                assertThat(sheet.getRow(1).getCell(0).getCellStyle().getBorderLeft()).isEqualTo(BorderStyle.THIN);
            }
            assertThat(headerFill(report, 1)).as("accent colour of each sheet")
                    .isNotEqualTo(headerFill(purchases, 1));
        }
    }

    @Test
    void templateWithoutPresetUsesTheApplicationDefaultPreset() throws IOException {
        SheetsmithDefaults standard = WriterSupport.STANDARD;
        SheetsmithDefaults defaults = new SheetsmithDefaults(standard.dateFormat(), standard.dateTimeFormat(),
                standard.numberFormat(), TablePreset.DARK, "#7F6000");
        WorkbookWriter writer = new WorkbookWriter(WorkbookSupplier.XSSF, defaults);

        try (XSSFWorkbook workbook = write(writer,
                sheet("Template", TemplateSheets.UsesBlankTemplate.class, List.of()),
                sheet("Plain", TemplateSheets.WithoutTemplate.class, List.of()))) {
            String withTemplate = headerFill(workbook.getSheet("Template"), 0);

            assertThat(withTemplate).isNotNull().isEqualTo(headerFill(workbook.getSheet("Plain"), 0));
        }
    }

    private static String headerFill(XSSFSheet sheet, int headerRow) {
        XSSFCellStyle style = (XSSFCellStyle) sheet.getRow(headerRow).getCell(0).getCellStyle();
        return style.getFillForegroundColorColor() == null ? null : style.getFillForegroundColorColor().getARGBHex();
    }
}
