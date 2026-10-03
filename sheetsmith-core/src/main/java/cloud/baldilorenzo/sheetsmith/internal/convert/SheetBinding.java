package cloud.baldilorenzo.sheetsmith.internal.convert;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.internal.metadata.ColumnMetadata;
import cloud.baldilorenzo.sheetsmith.internal.metadata.SheetMetadata;

import java.util.List;
import java.util.Objects;

/**
 * Sheet metadata with a converter bound to each column.
 *
 * @param metadata the sheet metadata
 * @param columns  the bound columns, in the order of {@link SheetMetadata#columns()}
 */
public record SheetBinding(SheetMetadata metadata, List<Column> columns) {

    /**
     * Creates a binding.
     *
     * @param metadata the sheet metadata, not null
     * @param columns  the bound columns, one per metadata column
     */
    public SheetBinding {
        Objects.requireNonNull(metadata, "metadata");
        columns = List.copyOf(columns);
        if (columns.size() != metadata.columns().size()) {
            throw new IllegalArgumentException("expected " + metadata.columns().size() + " bound columns");
        }
    }

    /**
     * A column with its converter.
     *
     * @param metadata  the column metadata
     * @param converter the converter of the column values
     */
    public record Column(ColumnMetadata metadata, CellConverter<Object> converter) {

        /**
         * Creates a bound column.
         *
         * @param metadata  the column metadata, not null
         * @param converter the converter, not null
         */
        public Column {
            Objects.requireNonNull(metadata, "metadata");
            Objects.requireNonNull(converter, "converter");
        }
    }
}
