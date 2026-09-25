package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.style.Border;

/**
 * Frame drawn around the header and the data rows. Produces, for each cell, a layer that sets only the border
 * sides on the edge of the frame. The title is never framed.
 */
public final class OuterBorder {

    /** No frame: every layer is empty. */
    public static final OuterBorder NONE = new OuterBorder(null, null);

    private final Border line;
    private final String color;

    private OuterBorder(Border line, String color) {
        this.line = line;
        this.color = color;
    }

    /**
     * Returns a frame.
     *
     * @param line  the frame line, or null for no frame
     * @param color the frame colour, or null for automatic
     * @return the frame
     * @throws IllegalArgumentException if the line is {@code INHERIT}
     */
    public static OuterBorder of(Border line, String color) {
        if (line == Border.INHERIT) {
            throw new IllegalArgumentException("no frame is represented by a null line");
        }
        return line == null ? NONE : new OuterBorder(line, color);
    }

    /**
     * Returns the edges of a header cell: top on every column, left on the first, right on the last, and bottom
     * when there are no data rows.
     *
     * @param role    the role of the header cell
     * @param hasData whether the table has data rows
     * @return the layer
     */
    public StyleAttributes header(CellRole role, boolean hasData) {
        return edges(true, !hasData, role.firstColumn(), role.lastColumn());
    }

    /**
     * Returns the edges of a data cell: left on the first column, right on the last, bottom on the last row.
     *
     * @param role the role of the data cell
     * @return the layer
     */
    public StyleAttributes body(CellRole role) {
        return edges(false, role.lastRow(), role.firstColumn(), role.lastColumn());
    }

    private StyleAttributes edges(boolean top, boolean bottom, boolean left, boolean right) {
        if (line == null || !(top || bottom || left || right)) {
            return StyleAttributes.EMPTY;
        }
        StyleAttributes.Builder builder = StyleAttributes.builder();
        if (top) {
            builder.borderTop(line).borderTopColor(color);
        }
        if (bottom) {
            builder.borderBottom(line).borderBottomColor(color);
        }
        if (left) {
            builder.borderLeft(line).borderLeftColor(color);
        }
        if (right) {
            builder.borderRight(line).borderRightColor(color);
        }
        return builder.build();
    }
}
