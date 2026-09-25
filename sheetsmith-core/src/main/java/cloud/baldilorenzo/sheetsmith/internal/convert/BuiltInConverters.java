package cloud.baldilorenzo.sheetsmith.internal.convert;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Converters for the types written natively. Types with a time zone, {@code java.util.Date}, {@code LocalTime}
 * and {@code UUID} are deliberately missing: they need an explicit converter.
 */
public final class BuiltInConverters {

    private static final Map<Class<?>, CellConverter<?>> CONVERTERS = Map.of(
            CharSequence.class, (CellConverter<CharSequence>) (value, context) -> CellValue.text(value.toString()),
            Character.class, (CellConverter<Character>) (value, context) -> CellValue.text(value.toString()),
            Number.class, (CellConverter<Number>) (value, context) -> CellValue.number(value.doubleValue()),
            Boolean.class, (CellConverter<Boolean>) (value, context) -> CellValue.bool(value),
            Enum.class, (CellConverter<Enum<?>>) (value, context) -> CellValue.text(value.name()),
            LocalDate.class, (CellConverter<LocalDate>) (value, context) -> CellValue.date(value),
            LocalDateTime.class, (CellConverter<LocalDateTime>) (value, context) -> CellValue.dateTime(value));

    private BuiltInConverters() {
    }

    /**
     * Returns the built-in converters by registered type.
     *
     * @return an unmodifiable map
     */
    public static Map<Class<?>, CellConverter<?>> all() {
        return CONVERTERS;
    }
}
