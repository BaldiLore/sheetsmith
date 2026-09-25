package cloud.baldilorenzo.sheetsmith.internal.style;

/**
 * Position of a cell in the table, which selects the style slots that apply to it. Flags are independent: a
 * single data row is both first and last, a single column is both first and last.
 *
 * @param firstRow    the cell is in the first data row
 * @param lastRow     the cell is in the last data row
 * @param even        the cell is in an even data row
 * @param firstColumn the cell is in the first column
 * @param lastColumn  the cell is in the last column
 */
public record CellRole(boolean firstRow, boolean lastRow, boolean even, boolean firstColumn, boolean lastColumn) {

    /**
     * Returns the role of a data cell.
     *
     * @param row         the 1-based data row index
     * @param rowCount    the number of data rows
     * @param column      the 0-based column index
     * @param columnCount the number of columns
     * @return the role
     * @throws IllegalArgumentException if an index is out of range
     */
    public static CellRole data(int row, int rowCount, int column, int columnCount) {
        if (row < 1 || row > rowCount) {
            throw new IllegalArgumentException("row " + row + " is out of range 1 to " + rowCount);
        }
        checkColumn(column, columnCount);
        return new CellRole(row == 1, row == rowCount, row % 2 == 0, column == 0, column == columnCount - 1);
    }

    /**
     * Returns the role of a header cell: only the column flags are set.
     *
     * @param column      the 0-based column index
     * @param columnCount the number of columns
     * @return the role
     * @throws IllegalArgumentException if the index is out of range
     */
    public static CellRole header(int column, int columnCount) {
        checkColumn(column, columnCount);
        return new CellRole(false, false, false, column == 0, column == columnCount - 1);
    }

    private static void checkColumn(int column, int columnCount) {
        if (column < 0 || column >= columnCount) {
            throw new IllegalArgumentException("column " + column + " is out of range 0 to " + (columnCount - 1));
        }
    }
}
