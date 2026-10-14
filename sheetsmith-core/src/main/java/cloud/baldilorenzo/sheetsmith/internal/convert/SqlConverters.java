package cloud.baldilorenzo.sheetsmith.internal.convert;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;

/**
 * Built-in converters for the {@code java.sql} types, the only main class referring to that module. Loaded by
 * {@link BuiltInConverters} only when the {@code java.sql} module is present. Both converters use the default time
 * zone of the JVM, as {@link Date#toLocalDate()} and {@link Timestamp#toLocalDateTime()} do.
 */
final class SqlConverters {

    private SqlConverters() {
    }

    /**
     * Returns the {@code java.sql} converters by registered type.
     *
     * @return an unmodifiable map
     */
    static Map<Class<?>, CellConverter<?>> all() {
        return Map.of(
                Date.class, (CellConverter<Date>) (value, context) -> CellValue.date(value.toLocalDate()),
                Timestamp.class,
                (CellConverter<Timestamp>) (value, context) -> CellValue.dateTime(value.toLocalDateTime()));
    }
}
