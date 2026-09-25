package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.internal.style.StyleAttributes;

import java.util.Objects;

/**
 * Validated description of a column.
 *
 * @param header         the header text
 * @param order          the order value
 * @param fieldName      the name of the annotated field
 * @param valueType      the declared type of the field
 * @param accessor       reads the value from an instance
 * @param width          the width in characters, or null for automatic
 * @param format         the Excel data format of the data cells, or null
 * @param converterClass the field-level converter class, or null
 * @param headerStyle    the style of the header cell of this column
 * @param styles         the data slots of this column
 */
public record ColumnMetadata(
        String header,
        int order,
        String fieldName,
        Class<?> valueType,
        ValueAccessor accessor,
        Integer width,
        String format,
        Class<? extends CellConverter<?>> converterClass,
        StyleAttributes headerStyle,
        Slots styles) {

    /**
     * Creates a column description.
     *
     * @throws NullPointerException if a non-nullable component is null
     */
    public ColumnMetadata {
        Objects.requireNonNull(header, "header");
        Objects.requireNonNull(fieldName, "fieldName");
        Objects.requireNonNull(valueType, "valueType");
        Objects.requireNonNull(accessor, "accessor");
        Objects.requireNonNull(headerStyle, "headerStyle");
        Objects.requireNonNull(styles, "styles");
    }

    /**
     * Resolved data slots of a column.
     *
     * @param base     every data cell of the column
     * @param even     even data rows
     * @param odd      odd data rows
     * @param firstRow the first data row
     * @param lastRow  the last data row
     */
    public record Slots(
            StyleAttributes base,
            StyleAttributes even,
            StyleAttributes odd,
            StyleAttributes firstRow,
            StyleAttributes lastRow) {

        /** Slots with no style. */
        public static final Slots EMPTY = new Slots(StyleAttributes.EMPTY, StyleAttributes.EMPTY,
                StyleAttributes.EMPTY, StyleAttributes.EMPTY, StyleAttributes.EMPTY);

        /**
         * Creates column slots.
         *
         * @throws NullPointerException if a component is null
         */
        public Slots {
            Objects.requireNonNull(base, "base");
            Objects.requireNonNull(even, "even");
            Objects.requireNonNull(odd, "odd");
            Objects.requireNonNull(firstRow, "firstRow");
            Objects.requireNonNull(lastRow, "lastRow");
        }
    }
}
