package cloud.baldilorenzo.sheetsmith.internal.write;

import cloud.baldilorenzo.sheetsmith.SheetsmithDefaults;
import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.internal.convert.ConverterBinder;
import cloud.baldilorenzo.sheetsmith.internal.convert.ConverterRegistry;
import cloud.baldilorenzo.sheetsmith.internal.convert.ReflectiveConverterFactory;
import cloud.baldilorenzo.sheetsmith.internal.metadata.MetadataExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

/** Writes sheets to bytes and reads them back with POI. */
final class WriterSupport {

    static final SheetsmithDefaults STANDARD = SheetsmithDefaults.standard();

    private static final MetadataExtractor EXTRACTOR = new MetadataExtractor();

    private WriterSupport() {
    }

    static WritableSheet sheet(String name, Class<?> type, List<?> rows) {
        return sheet(name, type, rows, Map.of());
    }

    static WritableSheet sheet(String name, Class<?> type, List<?> rows,
                               Map<Class<?>, CellConverter<?>> converters) {
        ConverterBinder binder = new ConverterBinder(new ConverterRegistry(converters),
                new ReflectiveConverterFactory());
        return new WritableSheet(name, binder.bind(EXTRACTOR.extract(type)), rows);
    }

    static XSSFWorkbook write(WritableSheet... sheets) {
        return write(new WorkbookWriter(WorkbookSupplier.XSSF, STANDARD), sheets);
    }

    static XSSFWorkbook write(WorkbookWriter writer, WritableSheet... sheets) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer.write(List.of(sheets), out);
            return new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
