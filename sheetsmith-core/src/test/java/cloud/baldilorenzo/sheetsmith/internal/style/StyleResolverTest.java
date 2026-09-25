package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.internal.metadata.ColumnMetadata;
import cloud.baldilorenzo.sheetsmith.internal.metadata.SheetMetadata;
import cloud.baldilorenzo.sheetsmith.style.Border;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cloud.baldilorenzo.sheetsmith.internal.style.TestSheets.column;
import static cloud.baldilorenzo.sheetsmith.internal.style.TestSheets.sheet;
import static cloud.baldilorenzo.sheetsmith.internal.style.TestSheets.tag;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every slot tags its style with its own name as font name: the font name of the resolved style is the slot that
 * won the cascade.
 */
class StyleResolverTest {

    @ParameterizedTest(name = "[{0}] row {1}/{2}, column {3}/{4} -> {5}")
    @CsvSource(delimiter = ';', value = {
            // table base and even/odd
            "tBase;              1;3;0;2; tBase",
            "tBase tOdd;         1;3;0;2; tOdd",
            "tBase tEven;        1;3;0;2; tBase",
            "tBase tEven;        2;3;0;2; tEven",
            "tOdd;               2;3;0;2; -",
            // column roles over even/odd, row roles over column roles
            "tOdd tFirstCol;     1;3;0;2; tFirstCol",
            "tOdd tLastCol;      1;3;1;2; tLastCol",
            "tLastCol;           2;3;0;2; -",
            "tFirstCol tFirstRow;1;3;0;2; tFirstRow",
            "tLastCol tLastRow;  3;3;1;2; tLastRow",
            "tFirstCol tLastRow; 3;3;0;2; tLastRow",
            "tLastRow;           2;3;0;2; -",
            // single row: first row wins; single column: first column wins
            "tFirstRow tLastRow; 1;1;0;2; tFirstRow",
            "tFirstCol tLastCol; 2;3;0;1; tFirstCol",
            // column level over table level
            "tFirstRow cBase;    1;3;0;2; cBase",
            "tLastCol cBase;     1;3;1;2; cBase",
            "cBase cOdd;         1;3;0;2; cOdd",
            "cBase cOdd;         2;3;0;2; cBase",
            "cBase cEven;        2;3;0;2; cEven",
            "cOdd cFirstRow;     1;3;0;2; cFirstRow",
            "cEven cLastRow;     2;2;0;2; cLastRow",
            "cFirstRow cLastRow; 1;1;0;2; cFirstRow",
            "cLastRow;           2;3;0;2; -"
    })
    void bodyCascade(String slots, int row, int rowCount, int columnIndex, int columnCount, String expected) {
        Set<String> set = Arrays.stream(slots.trim().split("\\s+")).collect(Collectors.toSet());
        SheetMetadata.BodySlots body = new SheetMetadata.BodySlots(
                slot(set, "tBase"), slot(set, "tEven"), slot(set, "tOdd"), slot(set, "tFirstRow"),
                slot(set, "tLastRow"), slot(set, "tFirstCol"), slot(set, "tLastCol"));
        ColumnMetadata column = column("c", StyleAttributes.EMPTY, new ColumnMetadata.Slots(
                slot(set, "cBase"), slot(set, "cEven"), slot(set, "cOdd"), slot(set, "cFirstRow"),
                slot(set, "cLastRow")), null);
        StyleResolver resolver = new StyleResolver(sheet(SheetMetadata.HeaderSlots.EMPTY, body, List.of(column)));

        StyleAttributes style = resolver.body(column, CellRole.data(row, rowCount, columnIndex, columnCount));

        assertThat(style.fontName()).isEqualTo(expected.equals("-") ? null : expected);
    }

    @ParameterizedTest(name = "[{0}] column {1}/{2} -> {3}")
    @CsvSource(delimiter = ';', value = {
            "hBase;                0;2; hBase",
            "hBase hFirstCol;      0;2; hFirstCol",
            "hBase hFirstCol;      1;2; hBase",
            "hBase hLastCol;       1;2; hLastCol",
            "hFirstCol hLastCol;   0;1; hFirstCol",
            "hFirstCol cHeader;    0;2; cHeader",
            "hLastCol cHeader;     1;2; cHeader",
            "tBase tFirstCol tOdd; 0;2; -"
    })
    void headerCascade(String slots, int columnIndex, int columnCount, String expected) {
        Set<String> set = Arrays.stream(slots.trim().split("\\s+")).collect(Collectors.toSet());
        SheetMetadata.HeaderSlots header = new SheetMetadata.HeaderSlots(
                slot(set, "hBase"), slot(set, "hFirstCol"), slot(set, "hLastCol"));
        SheetMetadata.BodySlots body = new SheetMetadata.BodySlots(slot(set, "tBase"), StyleAttributes.EMPTY,
                slot(set, "tOdd"), StyleAttributes.EMPTY, StyleAttributes.EMPTY, slot(set, "tFirstCol"),
                StyleAttributes.EMPTY);
        ColumnMetadata column = column("c", slot(set, "cHeader"), ColumnMetadata.Slots.EMPTY, null);
        StyleResolver resolver = new StyleResolver(sheet(header, body, List.of(column)));

        StyleAttributes style = resolver.header(column, CellRole.header(columnIndex, columnCount), true);

        assertThat(style.fontName()).isEqualTo(expected.equals("-") ? null : expected);
    }

    @Test
    void layersMergeAttributeByAttribute() {
        SheetMetadata.BodySlots body = new SheetMetadata.BodySlots(
                StyleAttributes.builder().fontName("Arial").fontSize(10).build(),
                StyleAttributes.EMPTY,
                StyleAttributes.builder().fillColor("#EEEEEE").build(),
                StyleAttributes.builder().bold(true).build(),
                StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY);
        ColumnMetadata column = column("c", StyleAttributes.EMPTY, new ColumnMetadata.Slots(
                StyleAttributes.builder().fontSize(12).build(), StyleAttributes.EMPTY, StyleAttributes.EMPTY,
                StyleAttributes.EMPTY, StyleAttributes.EMPTY), null);
        StyleResolver resolver = new StyleResolver(sheet(SheetMetadata.HeaderSlots.EMPTY, body, List.of(column)));

        assertThat(resolver.body(column, CellRole.data(1, 3, 0, 1))).isEqualTo(StyleAttributes.builder()
                .fontName("Arial").fontSize(12).fillColor("#EEEEEE").bold(true).build());
    }

    @Test
    void columnFormatIsTheMostSpecificLayer() {
        SheetMetadata.BodySlots body = new SheetMetadata.BodySlots(
                StyleAttributes.builder().dataFormat("0").build(), StyleAttributes.EMPTY, StyleAttributes.EMPTY,
                StyleAttributes.builder().dataFormat("0.0").build(), StyleAttributes.EMPTY, StyleAttributes.EMPTY,
                StyleAttributes.EMPTY);
        ColumnMetadata formatted = column("formatted", StyleAttributes.EMPTY, new ColumnMetadata.Slots(
                StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY,
                StyleAttributes.builder().dataFormat("0.00").build(), StyleAttributes.EMPTY), "#,##0.000");
        ColumnMetadata plain = column("plain");
        StyleResolver resolver = new StyleResolver(
                sheet(SheetMetadata.HeaderSlots.EMPTY, body, List.of(formatted, plain)));

        assertThat(resolver.body(formatted, CellRole.data(1, 2, 0, 2)).dataFormat()).isEqualTo("#,##0.000");
        assertThat(resolver.body(plain, CellRole.data(1, 2, 1, 2)).dataFormat()).isEqualTo("0.0");
        assertThat(resolver.body(plain, CellRole.data(2, 2, 1, 2)).dataFormat()).isEqualTo("0");
    }

    @Test
    void titleUsesOnlyTheTitleStyle() {
        SheetMetadata.BodySlots body = new SheetMetadata.BodySlots(tag("tBase"), StyleAttributes.EMPTY,
                StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY,
                StyleAttributes.EMPTY);
        SheetMetadata.HeaderSlots header = new SheetMetadata.HeaderSlots(tag("hBase"), StyleAttributes.EMPTY,
                StyleAttributes.EMPTY);
        StyleResolver resolver = new StyleResolver(sheet(header, body, Border.THICK, null, List.of(column("c"))));

        assertThat(resolver.title()).isEqualTo(tag("title"));
    }

    private static StyleAttributes slot(Set<String> set, String name) {
        return set.contains(name) ? tag(name) : StyleAttributes.EMPTY;
    }
}
