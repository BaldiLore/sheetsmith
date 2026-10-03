package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.internal.metadata.ColumnMetadata;
import cloud.baldilorenzo.sheetsmith.internal.metadata.SheetMetadata;
import cloud.baldilorenzo.sheetsmith.style.Border;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cloud.baldilorenzo.sheetsmith.internal.style.TestSheets.column;
import static cloud.baldilorenzo.sheetsmith.internal.style.TestSheets.sheet;
import static org.assertj.core.api.Assertions.assertThat;

class OuterBorderTest {

    private static final int COLUMNS = 3;
    private static final int ROWS = 2;

    private final List<ColumnMetadata> columns = List.of(column("a"), column("b"), column("c"));

    @Test
    void frameIsContinuousAroundHeaderAndData() {
        StyleResolver resolver = resolver(SheetMetadata.BodySlots.EMPTY, "RED");

        for (int j = 0; j < COLUMNS; j++) {
            StyleAttributes header = resolver.header(columns.get(j), CellRole.header(j, COLUMNS), true);
            assertSides(header, true, false, j == 0, j == COLUMNS - 1);
            for (int i = 1; i <= ROWS; i++) {
                StyleAttributes cell = resolver.body(columns.get(j), CellRole.data(i, ROWS, j, COLUMNS));
                assertSides(cell, false, i == ROWS, j == 0, j == COLUMNS - 1);
            }
        }
    }

    @Test
    void headerIsClosedAtTheBottomWhenThereAreNoDataRows() {
        StyleResolver resolver = resolver(SheetMetadata.BodySlots.EMPTY, null);

        for (int j = 0; j < COLUMNS; j++) {
            StyleAttributes header = resolver.header(columns.get(j), CellRole.header(j, COLUMNS), false);
            assertSides(header, true, true, j == 0, j == COLUMNS - 1);
        }
    }

    @Test
    void frameColourIsAppliedOnlyWhenSet() {
        StyleAttributes coloured = resolver(SheetMetadata.BodySlots.EMPTY, "RED")
                .body(columns.get(0), CellRole.data(1, ROWS, 0, COLUMNS));
        StyleAttributes automatic = resolver(SheetMetadata.BodySlots.EMPTY, null)
                .body(columns.get(0), CellRole.data(1, ROWS, 0, COLUMNS));

        assertThat(coloured.borderLeftColor()).isEqualTo("RED");
        assertThat(coloured.borderRightColor()).isNull();
        assertThat(automatic.borderLeft()).isEqualTo(Border.MEDIUM);
        assertThat(automatic.borderLeftColor()).isNull();
    }

    @Test
    void frameOverridesBaseButExplicitRoleSlotsOverrideTheFrame() {
        SheetMetadata.BodySlots body = new SheetMetadata.BodySlots(
                StyleAttributes.builder().border(Border.DOTTED).build(),
                StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY,
                StyleAttributes.builder().borderBottom(Border.DOUBLE).build(),
                StyleAttributes.builder().borderLeft(Border.NONE).build(),
                StyleAttributes.EMPTY);
        StyleResolver resolver = resolver(body, null);

        StyleAttributes lastColumn = resolver.body(columns.get(2), CellRole.data(1, ROWS, 2, COLUMNS));
        StyleAttributes firstColumn = resolver.body(columns.get(0), CellRole.data(1, ROWS, 0, COLUMNS));
        StyleAttributes lastRow = resolver.body(columns.get(1), CellRole.data(ROWS, ROWS, 1, COLUMNS));

        assertThat(lastColumn.borderRight()).isEqualTo(Border.MEDIUM);
        assertThat(lastColumn.borderLeft()).isEqualTo(Border.DOTTED);
        assertThat(firstColumn.borderLeft()).isEqualTo(Border.NONE);
        assertThat(lastRow.borderBottom()).isEqualTo(Border.DOUBLE);
    }

    @Test
    void noFrameProducesEmptyLayers() {
        assertThat(OuterBorder.of(null, "RED")).isSameAs(OuterBorder.NONE);
        assertThat(OuterBorder.NONE.header(CellRole.header(0, 1), false)).isEqualTo(StyleAttributes.EMPTY);
        assertThat(OuterBorder.NONE.body(CellRole.data(1, 1, 0, 1))).isEqualTo(StyleAttributes.EMPTY);
    }

    private StyleResolver resolver(SheetMetadata.BodySlots body, String color) {
        return new StyleResolver(sheet(SheetMetadata.HeaderSlots.EMPTY, body, Border.MEDIUM, color, columns));
    }

    private static void assertSides(StyleAttributes style, boolean top, boolean bottom, boolean left,
                                    boolean right) {
        assertThat(style.borderTop()).isEqualTo(top ? Border.MEDIUM : null);
        assertThat(style.borderBottom()).isEqualTo(bottom ? Border.MEDIUM : null);
        assertThat(style.borderLeft()).isEqualTo(left ? Border.MEDIUM : null);
        assertThat(style.borderRight()).isEqualTo(right ? Border.MEDIUM : null);
    }
}
