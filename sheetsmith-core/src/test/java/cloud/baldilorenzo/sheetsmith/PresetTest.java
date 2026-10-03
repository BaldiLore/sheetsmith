package cloud.baldilorenzo.sheetsmith;

import cloud.baldilorenzo.sheetsmith.fixtures.PresetSheets;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PresetTest {

    private static final SheetsmithDefaults DARK_DEFAULTS =
            new SheetsmithDefaults("yyyy-mm-dd", "yyyy-mm-dd hh:mm:ss", "", TablePreset.DARK, "#4472C4");

    @Test
    void inheritUsesTheDefaultPresetAndAccent() throws IOException {
        XSSFCellStyle header = headerStyle(DARK_DEFAULTS, PresetSheets.InheritPreset.class,
                new PresetSheets.InheritPreset("a"));

        assertThat(header.getFillPattern()).isEqualTo(FillPatternType.SOLID_FOREGROUND);
        assertThat(header.getFillForegroundXSSFColor().getARGBHex()).endsWith("223962");
        assertThat(header.getFont().getXSSFColor().getARGBHex()).endsWith("FFFFFF");
    }

    @Test
    void inheritWithStandardDefaultsAppliesNoPreset() throws IOException {
        XSSFCellStyle header = headerStyle(SheetsmithDefaults.standard(), PresetSheets.InheritPreset.class,
                new PresetSheets.InheritPreset("a"));

        assertThat(header.getFillPattern()).isEqualTo(FillPatternType.NO_FILL);
        assertThat(header.getFont().getBold()).isFalse();
    }

    @Test
    void noneAppliesNoPresetEvenWhenTheDefaultIsSet() throws IOException {
        XSSFCellStyle header = headerStyle(DARK_DEFAULTS, PresetSheets.NoPreset.class,
                new PresetSheets.NoPreset("a"));

        assertThat(header.getFillPattern()).isEqualTo(FillPatternType.NO_FILL);
    }

    @Test
    void classAccentOverridesTheDefaultAccent() throws IOException {
        XSSFCellStyle header = headerStyle(DARK_DEFAULTS, PresetSheets.MediumWithAccent.class,
                new PresetSheets.MediumWithAccent("a"));

        assertThat(header.getFillForegroundXSSFColor().getARGBHex()).endsWith("FFC000");
        assertThat(header.getFont().getXSSFColor().getARGBHex()).endsWith("000000");
    }

    @Test
    void classPresetUsesTheDefaultAccentWhenNoneIsSet() throws IOException {
        XSSFCellStyle header = headerStyle(DARK_DEFAULTS, PresetSheets.LightWithDefaultAccent.class,
                new PresetSheets.LightWithDefaultAccent("a"));

        assertThat(header.getFillPattern()).isEqualTo(FillPatternType.NO_FILL);
        assertThat(header.getFont().getXSSFColor().getARGBHex()).endsWith("335693");
        assertThat(header.getBottomBorderXSSFColor().getARGBHex()).endsWith("4472C4");
    }

    private static <T> XSSFCellStyle headerStyle(SheetsmithDefaults defaults, Class<T> type, T row)
            throws IOException {
        byte[] bytes = Sheetsmith.builder().defaults(defaults).build()
                .generate(List.of(SheetData.of("S", type, List.of(row))));
        try (XSSFWorkbook workbook = SheetsmithTest.read(bytes)) {
            return workbook.getSheet("S").getRow(0).getCell(0).getCellStyle();
        }
    }
}
