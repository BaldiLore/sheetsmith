package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.internal.metadata.ColumnMetadata;
import cloud.baldilorenzo.sheetsmith.internal.metadata.SheetMetadata;
import cloud.baldilorenzo.sheetsmith.internal.metadata.ValueAccessor;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;

import java.lang.invoke.MethodHandles;
import java.util.List;

/** Builds sheet metadata programmatically for style tests. */
final class TestSheets {

    private TestSheets() {
    }

    static StyleAttributes tag(String name) {
        return StyleAttributes.builder().fontName(name).build();
    }

    static ColumnMetadata column(String name) {
        return column(name, StyleAttributes.EMPTY, ColumnMetadata.Slots.EMPTY, null);
    }

    static ColumnMetadata column(String name, StyleAttributes headerStyle, ColumnMetadata.Slots styles,
                                 String format) {
        return new ColumnMetadata(name, 0, name, Object.class,
                new ValueAccessor(MethodHandles.identity(Object.class), "identity"),
                null, format, null, headerStyle, styles);
    }

    static SheetMetadata sheet(SheetMetadata.HeaderSlots header, SheetMetadata.BodySlots body,
                               List<ColumnMetadata> columns) {
        return sheet(header, body, null, null, columns);
    }

    static SheetMetadata sheet(SheetMetadata.HeaderSlots header, SheetMetadata.BodySlots body,
                               Border outerBorder, String outerBorderColor, List<ColumnMetadata> columns) {
        return new SheetMetadata(Object.class, "Title", tag("title"), new SheetMetadata.Options(true, false, true),
                TablePreset.NONE, null, outerBorder, outerBorderColor, header, body, columns);
    }
}
