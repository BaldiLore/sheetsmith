package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.fixtures.TemplateSheets;
import cloud.baldilorenzo.sheetsmith.internal.style.StyleAttributes;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateCascadeTest {

    private static final StyleAttributes TITLE = StyleAttributes.builder().fontSize(16).build();
    private static final StyleAttributes HEADER = StyleAttributes.builder().bold(true).build();
    private static final StyleAttributes HEADER_FIRST = StyleAttributes.builder().borderLeft(Border.THIN).build();
    private static final StyleAttributes HEADER_LAST = StyleAttributes.builder().borderRight(Border.THIN).build();
    private static final StyleAttributes ODD = StyleAttributes.builder().italic(true).build();
    private static final StyleAttributes OWN = StyleAttributes.builder().fontColor("#00AA00").build();

    private static final SheetMetadata.BodySlots TEMPLATE_BODY = new SheetMetadata.BodySlots(
            StyleAttributes.builder().fontName("Arial").build(),
            StyleAttributes.builder().fillColor("#F2F2F2").build(),
            ODD,
            StyleAttributes.builder().bold(false).build(),
            StyleAttributes.builder().borderBottom(Border.DOUBLE).build(),
            StyleAttributes.builder().borderLeft(Border.DASHED).build(),
            StyleAttributes.builder().borderRight(Border.DASHED).build());

    private final MetadataExtractor extractor = new MetadataExtractor();

    @Test
    void sheetWithoutTemplateKeepsTheDefaultsWithTheOptionsLeftToInherit() {
        SheetMetadata metadata = extractor.extract(TemplateSheets.WithoutTemplate.class);

        assertThat(metadata.options()).isEqualTo(new SheetMetadata.Options(true, false, true));
        assertThat(metadata.preset()).isEqualTo(TablePreset.INHERIT);
        assertThat(metadata.accentColor()).isNull();
        assertThat(metadata.outerBorder()).isNull();
        assertThat(metadata.outerBorderColor()).isNull();
        assertThat(metadata.titleStyle()).isEqualTo(StyleAttributes.EMPTY);
        assertThat(metadata.header()).isEqualTo(SheetMetadata.HeaderSlots.EMPTY);
        assertThat(metadata.body()).isEqualTo(SheetMetadata.BodySlots.EMPTY);
    }

    @Test
    void everyAttributeNotSetOnTheSheetComesFromTheTemplate() {
        SheetMetadata metadata = extractor.extract(TemplateSheets.InheritsEverything.class);

        assertThat(metadata.title()).isEqualTo("Report");
        assertThat(metadata.titleStyle()).isEqualTo(TITLE);
        assertThat(metadata.options()).isEqualTo(new SheetMetadata.Options(false, true, false));
        assertThat(metadata.preset()).isEqualTo(TablePreset.MEDIUM);
        assertThat(metadata.accentColor()).isEqualTo("#1F4E79");
        assertThat(metadata.outerBorder()).isEqualTo(Border.THIN);
        assertThat(metadata.outerBorderColor()).isEqualTo("#A1B2C3");
        assertThat(metadata.header()).isEqualTo(new SheetMetadata.HeaderSlots(HEADER, HEADER_FIRST, HEADER_LAST));
        assertThat(metadata.body()).isEqualTo(TEMPLATE_BODY);
    }

    @Test
    void everyAttributeSetOnTheSheetWinsOverTheTemplate() {
        SheetMetadata metadata = extractor.extract(TemplateSheets.OverridesEverything.class);

        assertThat(metadata.titleStyle()).isEqualTo(OWN);
        assertThat(metadata.options()).isEqualTo(new SheetMetadata.Options(true, false, true));
        assertThat(metadata.preset()).isEqualTo(TablePreset.MEDIUM);
        assertThat(metadata.accentColor()).isEqualTo("#00AA00");
        assertThat(metadata.outerBorder()).isEqualTo(Border.MEDIUM);
        assertThat(metadata.outerBorderColor()).isEqualTo("DARK_BLUE");
        assertThat(metadata.header()).isEqualTo(new SheetMetadata.HeaderSlots(OWN, OWN, OWN));
        assertThat(metadata.body()).isEqualTo(new SheetMetadata.BodySlots(OWN, OWN, OWN, OWN, OWN, OWN, OWN));
    }

    @Test
    void sheetOptionsWinOverTheOppositeOptionsOfTheTemplate() {
        assertThat(extractor.extract(TemplateSheets.OverridesEverything.class).options())
                .as("TRUE, FALSE, TRUE on the sheet over FALSE, TRUE, FALSE on the template")
                .isEqualTo(new SheetMetadata.Options(true, false, true));
        assertThat(extractor.extract(TemplateSheets.OverridesDefaultOptions.class).options())
                .as("FALSE, TRUE, FALSE on the sheet over TRUE, FALSE, TRUE on the template")
                .isEqualTo(new SheetMetadata.Options(false, true, false));
    }

    @Test
    void sheetOverridesTheAccentColourAndKeepsThePresetOfTheTemplate() {
        SheetMetadata metadata = extractor.extract(TemplateSheets.OwnAccent.class);

        assertThat(metadata.preset()).isEqualTo(TablePreset.MEDIUM);
        assertThat(metadata.accentColor()).isEqualTo("#7F6000");
    }

    @Test
    void templateWithoutPresetLeavesPresetAndAccentToTheApplicationDefaults() {
        SheetMetadata metadata = extractor.extract(TemplateSheets.UsesBlankTemplate.class);

        assertThat(metadata.preset()).isEqualTo(TablePreset.INHERIT);
        assertThat(metadata.accentColor()).isNull();
        assertThat(metadata.options()).isEqualTo(new SheetMetadata.Options(true, false, true));
        assertThat(metadata.outerBorder()).isNull();
        assertThat(metadata.outerBorderColor()).isNull();
    }

    @Test
    void slotSetOnTheSheetWinsAndTheOtherSlotsKeepTheTemplate() {
        SheetMetadata metadata = extractor.extract(TemplateSheets.OverridesOneSlot.class);

        assertThat(metadata.header()).isEqualTo(new SheetMetadata.HeaderSlots(HEADER, OWN, HEADER_LAST));
        assertThat(metadata.body()).isEqualTo(new SheetMetadata.BodySlots(TEMPLATE_BODY.base(), TEMPLATE_BODY.even(),
                OWN, TEMPLATE_BODY.firstRow(), TEMPLATE_BODY.lastRow(), TEMPLATE_BODY.firstColumn(),
                TEMPLATE_BODY.lastColumn()));
    }

    @Test
    void strongestLevelWinsForAStyleNameDefinedAtEveryLevel() {
        assertThat(extractor.extract(TemplateSheets.SharedFromTemplate.class).header().base())
                .as("template style sheet")
                .isEqualTo(StyleAttributes.builder().fontColor("#111111").bold(true).build());
        assertThat(extractor.extract(TemplateSheets.SharedFromSheetStyleSheet.class).header().base())
                .as("sheet style sheet, replacing the style of the template entirely")
                .isEqualTo(StyleAttributes.builder().fontColor("#222222").build());
        assertThat(extractor.extract(TemplateSheets.SharedFromClass.class).header().base())
                .as("sheet class")
                .isEqualTo(StyleAttributes.builder().fontColor("#333333").build());
    }

    @Test
    void sheetCanRedefineTheStyleReferencedByASlotOfTheTemplate() {
        SheetMetadata metadata = extractor.extract(TemplateSheets.RedefinesTemplateStyle.class);

        assertThat(metadata.header().base()).isEqualTo(StyleAttributes.builder().fontColor("#FF0000").build());
        assertThat(metadata.header().firstColumn()).isEqualTo(HEADER_FIRST);
    }

    @Test
    void styleSheetReferencedByBothTheTemplateAndTheSheetIsNotAConflict() {
        SheetMetadata metadata = extractor.extract(TemplateSheets.SameStyleSheetAsTemplate.class);

        assertThat(metadata.header().base()).isEqualTo(HEADER);
    }

    @Test
    void columnCanUseAStyleOfTheTemplateStyleSheets() {
        ColumnMetadata column = extractor.extract(TemplateSheets.ColumnUsesTemplateStyle.class).columns().get(0);

        assertThat(column.styles().base()).isEqualTo(StyleAttributes.builder().dataFormat("#,##0.00").build());
        assertThat(column.headerStyle()).isEqualTo(StyleAttributes.builder().fontColor("#111111").bold(true).build());
    }

    @Test
    void titleStyleOfTheTemplateIsIgnoredWithoutTitle() {
        SheetMetadata metadata = extractor.extract(TemplateSheets.WithoutTitle.class);

        assertThat(metadata.title()).isNull();
        assertThat(metadata.titleStyle()).isEqualTo(StyleAttributes.EMPTY);
    }

    @Test
    void titleStyleOfTheTemplateAppliesToTheTitle() {
        assertThat(extractor.extract(TemplateSheets.InheritsEverything.class).titleStyle()).isEqualTo(TITLE);
    }
}
