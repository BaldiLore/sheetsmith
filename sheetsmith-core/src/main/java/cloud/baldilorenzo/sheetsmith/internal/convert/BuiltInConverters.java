package cloud.baldilorenzo.sheetsmith.internal.convert;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Converters for the types written natively. All of them are stateless and thread-safe.
 * <p>
 * The covered types are:
 * <ul>
 *   <li>text and numbers: {@code CharSequence}, {@code Character}, {@code Number}, {@code Boolean} and
 *       {@code Enum}, written as the constant name;</li>
 *   <li>dates and times: {@code LocalDate}, {@code LocalDateTime}, {@code YearMonth} as ISO text and
 *       {@code Year} as a number;</li>
 *   <li>identifiers and resources, written as text: {@code UUID}, {@code URI}, {@code URL}, {@code File},
 *       {@code Currency} (the ISO code), {@code ZoneId} (the id, which also covers {@code ZoneOffset}) and
 *       {@code Locale} (the language tag);</li>
 *   <li>optional and atomic values: {@code OptionalInt}, {@code OptionalLong} and {@code OptionalDouble}, written
 *       as a number or as a blank cell when empty, and {@code AtomicBoolean};</li>
 *   <li>{@code java.sql.Date} and {@code java.sql.Timestamp}, written as a date and a date-time in the default
 *       time zone of the JVM, when the {@code java.sql} module is present.</li>
 * </ul>
 * Other types are deliberately missing and need an explicit converter:
 * <ul>
 *   <li>instants and types with a time zone ({@code Instant}, {@code ZonedDateTime}, {@code OffsetDateTime},
 *       {@code java.util.Date}, {@code Calendar}): writing them would require choosing a time zone;</li>
 *   <li>times of day ({@code LocalTime}, {@code OffsetTime}, {@code java.sql.Time}): {@code CellValue} has no time-only
 *       kind, and attaching them to an arbitrary date would be misleading;</li>
 *   <li>types without a single representation ({@code Duration}, {@code Period}, {@code MonthDay});</li>
 *   <li>interfaces other than {@code CharSequence}: a domain type implementing two interfaces with a converter
 *       would become ambiguous (rule V-10), so the keys are classes only;</li>
 *   <li>containers ({@code Optional}, collections, arrays): they would require delegating to the converter of
 *       their elements.</li>
 * </ul>
 * The {@code java.sql} converters live in {@link SqlConverters}, the only class referring to {@code java.sql}
 * types, and are added only when the boot layer contains the {@code java.sql} module. A modular application that
 * does not read that module, or a runtime image built without it, can therefore still use every other converter.
 */
public final class BuiltInConverters {

    private static final Map<Class<?>, CellConverter<?>> CONVERTERS =
            build(ModuleLayer.boot().findModule("java.sql").isPresent());

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

    /**
     * Builds the built-in converters.
     *
     * @param sqlAvailable whether the {@code java.sql} module is present
     * @return an unmodifiable map, with the {@code java.sql} converters only when {@code sqlAvailable} is true
     */
    static Map<Class<?>, CellConverter<?>> build(boolean sqlAvailable) {
        Map<Class<?>, CellConverter<?>> converters = new HashMap<>(Map.ofEntries(
                // text and numbers
                Map.entry(CharSequence.class,
                        (CellConverter<CharSequence>) (value, context) -> CellValue.text(value.toString())),
                Map.entry(Character.class,
                        (CellConverter<Character>) (value, context) -> CellValue.text(value.toString())),
                Map.entry(Number.class,
                        (CellConverter<Number>) (value, context) -> CellValue.number(value.doubleValue())),
                Map.entry(Boolean.class, (CellConverter<Boolean>) (value, context) -> CellValue.bool(value)),
                Map.entry(Enum.class, (CellConverter<Enum<?>>) (value, context) -> CellValue.text(value.name())),
                // dates and times
                Map.entry(LocalDate.class, (CellConverter<LocalDate>) (value, context) -> CellValue.date(value)),
                Map.entry(LocalDateTime.class,
                        (CellConverter<LocalDateTime>) (value, context) -> CellValue.dateTime(value)),
                Map.entry(YearMonth.class,
                        (CellConverter<YearMonth>) (value, context) -> CellValue.text(value.toString())),
                Map.entry(Year.class, (CellConverter<Year>) (value, context) -> CellValue.number(value.getValue())),
                // identifiers and resources
                Map.entry(UUID.class, (CellConverter<UUID>) (value, context) -> CellValue.text(value.toString())),
                Map.entry(URI.class, (CellConverter<URI>) (value, context) -> CellValue.text(value.toString())),
                Map.entry(URL.class,
                        (CellConverter<URL>) (value, context) -> CellValue.text(value.toExternalForm())),
                Map.entry(File.class, (CellConverter<File>) (value, context) -> CellValue.text(value.getPath())),
                Map.entry(Currency.class,
                        (CellConverter<Currency>) (value, context) -> CellValue.text(value.getCurrencyCode())),
                Map.entry(ZoneId.class, (CellConverter<ZoneId>) (value, context) -> CellValue.text(value.getId())),
                Map.entry(Locale.class,
                        (CellConverter<Locale>) (value, context) -> CellValue.text(value.toLanguageTag())),
                // optional and atomic values
                Map.entry(OptionalInt.class, (CellConverter<OptionalInt>) (value, context) ->
                        value.isPresent() ? CellValue.number(value.getAsInt()) : CellValue.blank()),
                Map.entry(OptionalLong.class, (CellConverter<OptionalLong>) (value, context) ->
                        value.isPresent() ? CellValue.number(value.getAsLong()) : CellValue.blank()),
                Map.entry(OptionalDouble.class, (CellConverter<OptionalDouble>) (value, context) ->
                        value.isPresent() ? CellValue.number(value.getAsDouble()) : CellValue.blank()),
                Map.entry(AtomicBoolean.class,
                        (CellConverter<AtomicBoolean>) (value, context) -> CellValue.bool(value.get()))));
        // java.sql types, isolated so that this class never loads them when the module is missing
        if (sqlAvailable) {
            converters.putAll(SqlConverters.all());
        }
        return Map.copyOf(converters);
    }
}
