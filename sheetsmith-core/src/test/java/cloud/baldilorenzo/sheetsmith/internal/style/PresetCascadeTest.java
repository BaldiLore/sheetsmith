package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.internal.metadata.ColumnMetadata;
import cloud.baldilorenzo.sheetsmith.internal.metadata.SheetMetadata;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cloud.baldilorenzo.sheetsmith.internal.style.TestSheets.column;
import static cloud.baldilorenzo.sheetsmith.internal.style.TestSheets.sheet;
import static org.assertj.core.api.Assertions.assertThat;

/** Preset layers sit below every declared style and are overridden attribute by attribute. */
class PresetCascadeTest {

    private static final String A = "#4472C4";
    private static final PresetFactory.Layers MEDIUM = PresetFactory.layers(TablePreset.MEDIUM, A);
    private static final PresetFactory.Layers DARK = PresetFactory.layers(TablePreset.DARK, A);

    @Test
    void presetAppliesWhenNothingIsDeclared() {
        ColumnMetadata column = column("c");
        StyleResolver resolver = new StyleResolver(
                sheet(SheetMetadata.HeaderSlots.EMPTY, SheetMetadata.BodySlots.EMPTY, List.of(column)), DARK);

        assertThat(resolver.header(column, CellRole.header(0, 1), true)).isEqualTo(DARK.headerBase());
        assertThat(resolver.body(column, CellRole.data(1, 2, 0, 1))).isEqualTo(DARK.bodyOdd());
        assertThat(resolver.body(column, CellRole.data(2, 2, 0, 1))).isEqualTo(DARK.bodyEven());
    }

    @Test
    void classHeaderStyleOverridesOnlyTheAttributesItSets() {
        SheetMetadata.HeaderSlots header = new SheetMetadata.HeaderSlots(
                StyleAttributes.builder().fontColor("#FF0000").build(), StyleAttributes.EMPTY, StyleAttributes.EMPTY);
        ColumnMetadata column = column("c");
        StyleResolver resolver = new StyleResolver(sheet(header, SheetMetadata.BodySlots.EMPTY, List.of(column)),
                MEDIUM);

        assertThat(resolver.header(column, CellRole.header(0, 1), true)).isEqualTo(StyleAttributes.builder()
                .bold(true).fillColor(A).fontColor("#FF0000").build());
    }

    @Test
    void tableAndColumnSlotsOverridePresetBodyAttributesOneByOne() {
        SheetMetadata.BodySlots body = new SheetMetadata.BodySlots(
                StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.builder().italic(true).build(),
                StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY);
        ColumnMetadata column = column("c", StyleAttributes.EMPTY, new ColumnMetadata.Slots(
                StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.builder().fillColor("#FFFFFF").build(),
                StyleAttributes.EMPTY, StyleAttributes.EMPTY), null);
        StyleResolver resolver = new StyleResolver(sheet(SheetMetadata.HeaderSlots.EMPTY, body, List.of(column)),
                MEDIUM);

        assertThat(resolver.body(column, CellRole.data(1, 3, 0, 1))).isEqualTo(StyleAttributes.builder()
                .border(Border.THIN).borderColor("#B4C7E7").fillColor("#FFFFFF").italic(true).build());
    }

    @Test
    void outerBorderAndRoleSlotsStillApplyAbovePreset() {
        SheetMetadata.BodySlots body = new SheetMetadata.BodySlots(
                StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY,
                StyleAttributes.EMPTY, StyleAttributes.builder().bold(true).build(), StyleAttributes.EMPTY,
                StyleAttributes.EMPTY);
        ColumnMetadata column = column("c");
        StyleResolver resolver = new StyleResolver(
                sheet(SheetMetadata.HeaderSlots.EMPTY, body, Border.THICK, "RED", List.of(column)), MEDIUM);

        StyleAttributes last = resolver.body(column, CellRole.data(2, 2, 0, 1));

        assertThat(last.borderBottom()).isEqualTo(Border.THICK);
        assertThat(last.borderBottomColor()).isEqualTo("RED");
        assertThat(last.borderTop()).isEqualTo(Border.THIN);
        assertThat(last.bold()).isTrue();
    }

    @Test
    void titleStyleOverridesPresetTitle() {
        ColumnMetadata column = column("c");
        StyleResolver resolver = new StyleResolver(
                sheet(SheetMetadata.HeaderSlots.EMPTY, SheetMetadata.BodySlots.EMPTY, List.of(column)), MEDIUM);

        assertThat(resolver.title()).isEqualTo(StyleAttributes.builder()
                .bold(true).fontSize(14).fontColor("#335693").fontName("title").build());
    }

    @Test
    void withoutPresetTheCascadeIsUnchanged() {
        ColumnMetadata column = column("c");
        SheetMetadata metadata = sheet(SheetMetadata.HeaderSlots.EMPTY, SheetMetadata.BodySlots.EMPTY,
                List.of(column));

        assertThat(new StyleResolver(metadata, PresetFactory.Layers.NONE).body(column, CellRole.data(1, 1, 0, 1)))
                .isEqualTo(new StyleResolver(metadata).body(column, CellRole.data(1, 1, 0, 1)))
                .isEqualTo(StyleAttributes.EMPTY);
    }
}
