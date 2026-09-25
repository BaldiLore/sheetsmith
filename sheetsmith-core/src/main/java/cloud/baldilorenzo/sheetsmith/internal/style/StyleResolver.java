package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.internal.metadata.ColumnMetadata;
import cloud.baldilorenzo.sheetsmith.internal.metadata.SheetMetadata;

import java.util.Objects;

/**
 * Computes the effective style of the title, header and data cells of a sheet by merging style layers from the
 * least to the most specific. Immutable and thread-safe.
 */
public final class StyleResolver {

    private final SheetMetadata metadata;
    private final OuterBorder outerBorder;

    /**
     * Creates a resolver for a sheet.
     *
     * @param metadata the sheet metadata
     */
    public StyleResolver(SheetMetadata metadata) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.outerBorder = OuterBorder.of(metadata.outerBorder(), metadata.outerBorderColor());
    }

    /**
     * Returns the style of the title.
     *
     * @return the effective title style
     */
    public StyleAttributes title() {
        return metadata.titleStyle();
    }

    /**
     * Returns the style of a header cell: header base, outer border edges, header last then first column, column
     * header style.
     *
     * @param column  the column of the cell
     * @param role    the role of the cell, from {@link CellRole#header(int, int)}
     * @param hasData whether the table has data rows
     * @return the effective header style
     */
    public StyleAttributes header(ColumnMetadata column, CellRole role, boolean hasData) {
        SheetMetadata.HeaderSlots header = metadata.header();
        StyleAttributes style = StyleAttributes.EMPTY
                .merge(header.base())
                .merge(outerBorder.header(role, hasData));
        if (role.lastColumn()) {
            style = style.merge(header.lastColumn());
        }
        if (role.firstColumn()) {
            style = style.merge(header.firstColumn());
        }
        return style.merge(column.headerStyle());
    }

    /**
     * Returns the style of a data cell: table base, table odd or even, outer border edges, table last then first
     * column, table last then first row, column base, column odd or even, column last then first row, column
     * format.
     *
     * @param column the column of the cell
     * @param role   the role of the cell, from {@link CellRole#data(int, int, int, int)}
     * @return the effective data style
     */
    public StyleAttributes body(ColumnMetadata column, CellRole role) {
        SheetMetadata.BodySlots body = metadata.body();
        StyleAttributes style = StyleAttributes.EMPTY
                .merge(body.base())
                .merge(role.even() ? body.even() : body.odd())
                .merge(outerBorder.body(role));
        if (role.lastColumn()) {
            style = style.merge(body.lastColumn());
        }
        if (role.firstColumn()) {
            style = style.merge(body.firstColumn());
        }
        style = rows(style, role, body.lastRow(), body.firstRow());

        ColumnMetadata.Slots slots = column.styles();
        style = style.merge(slots.base()).merge(role.even() ? slots.even() : slots.odd());
        style = rows(style, role, slots.lastRow(), slots.firstRow());
        if (column.format() != null) {
            style = style.merge(StyleAttributes.builder().dataFormat(column.format()).build());
        }
        return style;
    }

    private static StyleAttributes rows(StyleAttributes style, CellRole role, StyleAttributes lastRow,
                                        StyleAttributes firstRow) {
        if (role.lastRow()) {
            style = style.merge(lastRow);
        }
        if (role.firstRow()) {
            style = style.merge(firstRow);
        }
        return style;
    }
}
