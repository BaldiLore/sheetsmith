package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.internal.style.StyleAttributes;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;

import java.util.List;
import java.util.Objects;

/**
 * Validated description of the sheet defined by an exported class. Presets and application defaults are not
 * resolved here, so the description does not depend on configuration.
 *
 * @param type             the exported class
 * @param title            the title, or null when there is no title row
 * @param titleStyle       the declared title style
 * @param options          the layout options
 * @param preset           the preset chosen on the class, possibly {@link TablePreset#INHERIT}
 * @param accentColor      the accent colour chosen on the class, or null to use the application default
 * @param outerBorder      the frame line, or null for no frame
 * @param outerBorderColor the frame colour, or null for automatic
 * @param header           the header slots
 * @param body             the data slots at table level
 * @param columns          the columns, sorted by ascending order
 */
public record SheetMetadata(
        Class<?> type,
        String title,
        StyleAttributes titleStyle,
        Options options,
        TablePreset preset,
        String accentColor,
        Border outerBorder,
        String outerBorderColor,
        HeaderSlots header,
        BodySlots body,
        List<ColumnMetadata> columns) {

    /**
     * Creates a sheet description.
     *
     * @throws NullPointerException if a non-nullable component is null
     */
    public SheetMetadata {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(titleStyle, "titleStyle");
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(preset, "preset");
        Objects.requireNonNull(header, "header");
        Objects.requireNonNull(body, "body");
        columns = List.copyOf(columns);
        if (outerBorder == Border.INHERIT) {
            throw new IllegalArgumentException("no frame is represented by a null outerBorder");
        }
    }

    /**
     * Layout options of the sheet.
     *
     * @param freezeHeader    freeze the rows up to and including the header
     * @param autoFilter      add an auto-filter on header and data
     * @param autoSizeColumns size columns without explicit width to their content
     */
    public record Options(boolean freezeHeader, boolean autoFilter, boolean autoSizeColumns) {
    }

    /**
     * Resolved header slots.
     *
     * @param base        every header cell
     * @param firstColumn the header cell of the first column
     * @param lastColumn  the header cell of the last column
     */
    public record HeaderSlots(StyleAttributes base, StyleAttributes firstColumn, StyleAttributes lastColumn) {

        /** Slots with no style. */
        public static final HeaderSlots EMPTY =
                new HeaderSlots(StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY);

        /**
         * Creates header slots.
         *
         * @throws NullPointerException if a component is null
         */
        public HeaderSlots {
            Objects.requireNonNull(base, "base");
            Objects.requireNonNull(firstColumn, "firstColumn");
            Objects.requireNonNull(lastColumn, "lastColumn");
        }
    }

    /**
     * Resolved data slots at table level.
     *
     * @param base        every data cell
     * @param even        even data rows
     * @param odd         odd data rows
     * @param firstRow    the first data row
     * @param lastRow     the last data row
     * @param firstColumn the data cells of the first column
     * @param lastColumn  the data cells of the last column
     */
    public record BodySlots(
            StyleAttributes base,
            StyleAttributes even,
            StyleAttributes odd,
            StyleAttributes firstRow,
            StyleAttributes lastRow,
            StyleAttributes firstColumn,
            StyleAttributes lastColumn) {

        /** Slots with no style. */
        public static final BodySlots EMPTY = new BodySlots(StyleAttributes.EMPTY, StyleAttributes.EMPTY,
                StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY,
                StyleAttributes.EMPTY);

        /**
         * Creates data slots.
         *
         * @throws NullPointerException if a component is null
         */
        public BodySlots {
            Objects.requireNonNull(base, "base");
            Objects.requireNonNull(even, "even");
            Objects.requireNonNull(odd, "odd");
            Objects.requireNonNull(firstRow, "firstRow");
            Objects.requireNonNull(lastRow, "lastRow");
            Objects.requireNonNull(firstColumn, "firstColumn");
            Objects.requireNonNull(lastColumn, "lastColumn");
        }
    }
}
