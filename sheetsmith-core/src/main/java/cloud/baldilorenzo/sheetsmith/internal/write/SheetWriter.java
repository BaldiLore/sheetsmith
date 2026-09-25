package cloud.baldilorenzo.sheetsmith.internal.write;

import cloud.baldilorenzo.sheetsmith.SheetsmithGenerationException;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;
import cloud.baldilorenzo.sheetsmith.convert.ConversionContext;
import cloud.baldilorenzo.sheetsmith.internal.convert.SheetBinding;
import cloud.baldilorenzo.sheetsmith.internal.metadata.ColumnMetadata;
import cloud.baldilorenzo.sheetsmith.internal.metadata.SheetMetadata;
import cloud.baldilorenzo.sheetsmith.internal.style.CellRole;
import cloud.baldilorenzo.sheetsmith.internal.style.StyleAttributes;
import cloud.baldilorenzo.sheetsmith.internal.style.StyleCache;
import cloud.baldilorenzo.sheetsmith.internal.style.StyleResolver;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;
import java.util.Objects;

/**
 * Writes one sheet: title, header, data rows and layout options. Immutable and thread-safe.
 */
public final class SheetWriter {

    /** Maximum number of rows of an Excel sheet, title and header included. */
    static final int MAX_ROWS = 1_048_576;

    /** Maximum number of characters of an Excel text cell. */
    static final int MAX_TEXT_LENGTH = 32_767;

    /** Maximum column width, in characters. */
    static final int MAX_WIDTH = 255;

    private final DefaultFormats formats;
    private final ColumnSizer columnSizer;

    /**
     * Creates a writer.
     *
     * @param formats the default formats
     */
    public SheetWriter(DefaultFormats formats) {
        this(formats, Sheet::autoSizeColumn);
    }

    SheetWriter(DefaultFormats formats, ColumnSizer columnSizer) {
        this.formats = Objects.requireNonNull(formats, "formats");
        this.columnSizer = Objects.requireNonNull(columnSizer, "columnSizer");
    }

    /**
     * Writes a sheet into a workbook.
     *
     * @param workbook the workbook
     * @param styles   the style cache of the workbook
     * @param input    the sheet to write
     * @throws SheetsmithGenerationException if an Excel limit is exceeded, the data list contains a null element,
     *                                       or a value cannot be read or converted
     */
    public void write(Workbook workbook, StyleCache styles, WritableSheet input) {
        new Run(workbook, styles, input).write();
    }

    /** Sizes a column to its content; replaceable in tests to simulate environments without fonts. */
    @FunctionalInterface
    interface ColumnSizer {
        void autoSize(Sheet sheet, int column);
    }

    /** State of the writing of one sheet. */
    private final class Run {

        private final StyleCache styles;
        private final String name;
        private final SheetMetadata metadata;
        private final List<SheetBinding.Column> columns;
        private final List<?> rows;
        private final StyleResolver resolver;
        private final Sheet sheet;
        private final int headerRow;
        private final int[] textWidths;

        Run(Workbook workbook, StyleCache styles, WritableSheet input) {
            this.styles = styles;
            this.name = input.name();
            this.metadata = input.binding().metadata();
            this.columns = input.binding().columns();
            this.rows = input.rows();
            this.resolver = new StyleResolver(metadata);
            this.headerRow = metadata.title() != null ? 1 : 0;
            this.textWidths = new int[columns.size()];
            long totalRows = headerRow + 1L + rows.size();
            if (totalRows > MAX_ROWS) {
                throw new SheetsmithGenerationException("the sheet needs " + totalRows
                        + " rows, more than the Excel limit of " + MAX_ROWS, name, 0, null, null);
            }
            this.sheet = workbook.createSheet(name);
        }

        void write() {
            if (metadata.title() != null) {
                writeTitle();
            }
            writeHeader();
            for (int i = 1; i <= rows.size(); i++) {
                writeDataRow(i);
            }
            applyLayout();
        }

        private void writeTitle() {
            int m = columns.size();
            Row row = sheet.createRow(0);
            CellStyle style = styles.get(resolver.title());
            for (int j = 0; j < m; j++) {
                row.createCell(j).setCellStyle(style);
            }
            row.getCell(0).setCellValue(metadata.title());
            if (m > 1) {
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, m - 1));
            }
        }

        private void writeHeader() {
            int m = columns.size();
            boolean hasData = !rows.isEmpty();
            Row row = sheet.createRow(headerRow);
            for (int j = 0; j < m; j++) {
                ColumnMetadata column = columns.get(j).metadata();
                Cell cell = row.createCell(j);
                cell.setCellValue(column.header());
                cell.setCellStyle(styles.get(resolver.header(column, CellRole.header(j, m), hasData)));
                textWidths[j] = column.header().length();
            }
        }

        private void writeDataRow(int i) {
            Object element = rows.get(i - 1);
            if (element == null) {
                throw new SheetsmithGenerationException("null element in the data list", name, i, null, null);
            }
            Row row = sheet.createRow(headerRow + i);
            for (int j = 0; j < columns.size(); j++) {
                writeDataCell(row.createCell(j), element, i, j);
            }
        }

        private void writeDataCell(Cell cell, Object element, int i, int j) {
            SheetBinding.Column column = columns.get(j);
            ColumnMetadata definition = column.metadata();
            StyleAttributes style = resolver.body(definition, CellRole.data(i, rows.size(), j, columns.size()));

            Object value;
            try {
                value = definition.accessor().get(element);
            } catch (RuntimeException e) {
                throw new SheetsmithGenerationException("cannot read the value (" + definition.accessor() + "): " + e,
                        name, i, definition.fieldName(), e);
            }
            if (value == null) {
                cell.setCellStyle(styles.get(style));
                return;
            }

            CellValue converted;
            try {
                converted = column.converter().convert(value, new Context(i, definition));
            } catch (RuntimeException e) {
                throw new SheetsmithGenerationException("converter failed: " + e, name, i, definition.fieldName(), e);
            }
            if (converted == null) {
                throw new SheetsmithGenerationException("converter returned null; return CellValue.blank() for an "
                        + "empty cell", name, i, definition.fieldName(), null);
            }

            if (converted instanceof CellValue.Text text) {
                String string = text.value();
                if (string.length() > MAX_TEXT_LENGTH) {
                    throw new SheetsmithGenerationException("text of " + string.length()
                            + " characters exceeds the Excel limit of " + MAX_TEXT_LENGTH, name, i,
                            definition.fieldName(), null);
                }
                cell.setCellValue(string);
                textWidths[j] = Math.max(textWidths[j], string.length());
            } else if (converted instanceof CellValue.Numeric numeric) {
                cell.setCellValue(numeric.value());
                style = withDefaultFormat(style, formats.numberFormat());
            } else if (converted instanceof CellValue.Bool bool) {
                cell.setCellValue(bool.value());
            } else if (converted instanceof CellValue.Date date) {
                cell.setCellValue(date.value());
                style = withDefaultFormat(style, formats.dateFormat());
            } else if (converted instanceof CellValue.DateTime dateTime) {
                cell.setCellValue(dateTime.value());
                style = withDefaultFormat(style, formats.dateTimeFormat());
            }
            cell.setCellStyle(styles.get(style));
        }

        private void applyLayout() {
            int m = columns.size();
            if (metadata.options().freezeHeader()) {
                sheet.createFreezePane(0, headerRow + 1);
            }
            if (metadata.options().autoFilter()) {
                sheet.setAutoFilter(new CellRangeAddress(headerRow, headerRow + rows.size(), 0, m - 1));
            }
            for (int j = 0; j < m; j++) {
                Integer width = columns.get(j).metadata().width();
                if (width != null) {
                    sheet.setColumnWidth(j, width * 256);
                } else if (metadata.options().autoSizeColumns()) {
                    autoSize(j);
                }
            }
        }

        /** Falls back to an estimate when text measurement fails, typically on headless systems without fonts. */
        private void autoSize(int j) {
            try {
                columnSizer.autoSize(sheet, j);
            } catch (RuntimeException | LinkageError | InternalError e) {
                sheet.setColumnWidth(j, Math.min(textWidths[j] + 2, MAX_WIDTH) * 256);
            }
        }

        private StyleAttributes withDefaultFormat(StyleAttributes style, String format) {
            if (style.dataFormat() != null || format.isEmpty()) {
                return style;
            }
            return style.toBuilder().dataFormat(format).build();
        }

        /** Conversion context of one cell. */
        private final class Context implements ConversionContext {

            private final int rowIndex;
            private final ColumnMetadata column;

            Context(int rowIndex, ColumnMetadata column) {
                this.rowIndex = rowIndex;
                this.column = column;
            }

            @Override
            public String sheetName() {
                return name;
            }

            @Override
            public int rowIndex() {
                return rowIndex;
            }

            @Override
            public String fieldName() {
                return column.fieldName();
            }

            @Override
            public Class<?> sourceType() {
                return metadata.type();
            }

            @Override
            public Class<?> valueType() {
                return column.valueType();
            }
        }
    }
}
