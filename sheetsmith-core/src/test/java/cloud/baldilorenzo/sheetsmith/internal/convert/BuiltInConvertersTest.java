package cloud.baldilorenzo.sheetsmith.internal.convert;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BuiltInConvertersTest {

    private static final Set<String> SQL_TYPES = Set.of("java.sql.Date", "java.sql.Timestamp");

    /** Built-in converters ignore the context, so none is needed. */
    @SuppressWarnings("unchecked")
    private static <T> CellValue convert(Class<T> type, T value) {
        CellConverter<T> converter = (CellConverter<T>) BuiltInConverters.all().get(type);
        assertThat(converter).as("built-in converter for %s", type.getName()).isNotNull();
        return converter.convert(value, null);
    }

    @Test
    void uuidIsWrittenAsItsCanonicalText() {
        UUID id = UUID.fromString("3f2a9c1e-7b4d-4e8a-9f61-2c5d8e0b4a17");

        assertThat(convert(UUID.class, id)).isEqualTo(new CellValue.Text("3f2a9c1e-7b4d-4e8a-9f61-2c5d8e0b4a17"));
    }

    @Test
    void uriIsWrittenAsText() {
        assertThat(convert(URI.class, URI.create("https://example.com/a?b=c")))
                .isEqualTo(new CellValue.Text("https://example.com/a?b=c"));
    }

    @Test
    void urlIsWrittenAsItsExternalForm() throws MalformedURLException {
        assertThat(convert(URL.class, URI.create("https://example.com/docs").toURL()))
                .isEqualTo(new CellValue.Text("https://example.com/docs"));
    }

    @Test
    void fileIsWrittenAsItsPath() {
        File file = new File("reports/q3.xlsx");

        assertThat(convert(File.class, file)).isEqualTo(new CellValue.Text(file.getPath()));
    }

    @Test
    void currencyIsWrittenAsItsCode() {
        assertThat(convert(Currency.class, Currency.getInstance("EUR"))).isEqualTo(new CellValue.Text("EUR"));
    }

    @Test
    void zoneIdIsWrittenAsItsId() {
        assertThat(convert(ZoneId.class, ZoneId.of("Europe/Rome"))).isEqualTo(new CellValue.Text("Europe/Rome"));
    }

    @Test
    void localeIsWrittenAsItsLanguageTag() {
        assertThat(convert(Locale.class, Locale.forLanguageTag("it-IT"))).isEqualTo(new CellValue.Text("it-IT"));
        assertThat(convert(Locale.class, Locale.ROOT)).isEqualTo(new CellValue.Text("und"));
    }

    @Test
    void yearMonthIsWrittenAsIsoText() {
        assertThat(convert(YearMonth.class, YearMonth.of(2026, 10))).isEqualTo(new CellValue.Text("2026-10"));
    }

    @Test
    void yearIsWrittenAsANumber() {
        assertThat(convert(Year.class, Year.of(2026))).isEqualTo(new CellValue.Numeric(2026));
    }

    @Test
    void atomicBooleanIsWrittenAsABoolean() {
        assertThat(convert(AtomicBoolean.class, new AtomicBoolean(true))).isEqualTo(new CellValue.Bool(true));
    }

    @Test
    void optionalIntIsWrittenAsANumberOrBlank() {
        assertThat(convert(OptionalInt.class, OptionalInt.of(42))).isEqualTo(new CellValue.Numeric(42));
        assertThat(convert(OptionalInt.class, OptionalInt.empty())).isSameAs(CellValue.blank());
    }

    @Test
    void optionalLongIsWrittenAsANumberOrBlank() {
        assertThat(convert(OptionalLong.class, OptionalLong.of(9_000_000_000L)))
                .isEqualTo(new CellValue.Numeric(9_000_000_000d));
        assertThat(convert(OptionalLong.class, OptionalLong.empty())).isSameAs(CellValue.blank());
    }

    @Test
    void optionalDoubleIsWrittenAsANumberOrBlank() {
        assertThat(convert(OptionalDouble.class, OptionalDouble.of(1.5))).isEqualTo(new CellValue.Numeric(1.5));
        assertThat(convert(OptionalDouble.class, OptionalDouble.empty())).isSameAs(CellValue.blank());
    }

    @Test
    void sqlDateIsWrittenAsADate() {
        assertThat(convert(java.sql.Date.class, java.sql.Date.valueOf(LocalDate.of(2026, 10, 14))))
                .isEqualTo(new CellValue.Date(LocalDate.of(2026, 10, 14)));
    }

    @Test
    void timestampIsWrittenAsADateTime() {
        LocalDateTime moment = LocalDateTime.of(2026, 10, 14, 9, 30, 15, 123_000_000);

        assertThat(convert(java.sql.Timestamp.class, java.sql.Timestamp.valueOf(moment)))
                .isEqualTo(new CellValue.DateTime(moment));
    }

    @Test
    void noInterfaceIsAKeyExceptCharSequence() {
        assertThat(BuiltInConverters.all().keySet()).filteredOn(Class::isInterface)
                .as("built-in converters must be registered for classes only, CharSequence apart: a domain type "
                        + "implementing two interfaces with a built-in converter would become ambiguous (V-10)")
                .containsExactly(CharSequence.class);
    }

    @Test
    void theMapIsUnmodifiable() {
        Map<Class<?>, CellConverter<?>> all = BuiltInConverters.all();

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> all.put(Object.class, (value, context) -> CellValue.blank()));
    }

    @Test
    void sqlConvertersAreAddedOnlyWhenTheModuleIsAvailable() {
        Set<String> withoutSql = names(BuiltInConverters.build(false));
        Set<String> withSql = names(BuiltInConverters.build(true));

        assertThat(withoutSql).doesNotContainAnyElementsOf(SQL_TYPES);
        assertThat(withSql).containsAll(SQL_TYPES).containsAll(withoutSql).hasSize(withoutSql.size() + 2);
    }

    @Test
    void sqlConvertersArePresentInTheTestRuntime() {
        assertThat(names(BuiltInConverters.all())).containsAll(SQL_TYPES);
    }

    /**
     * Adding or removing a built-in converter changes which sheet classes are valid, so it is a compatibility
     * decision: update this list, the Javadoc of {@code CellConverter} and the user manual together.
     */
    @Test
    void theRegisteredTypesAreExactlyTheExpectedOnes() {
        assertThat(names(BuiltInConverters.build(true))).containsExactlyInAnyOrder(
                // text and numbers
                "java.lang.CharSequence",
                "java.lang.Character",
                "java.lang.Number",
                "java.lang.Boolean",
                "java.lang.Enum",
                // dates and times
                "java.time.LocalDate",
                "java.time.LocalDateTime",
                "java.time.YearMonth",
                "java.time.Year",
                // identifiers and resources
                "java.util.UUID",
                "java.net.URI",
                "java.net.URL",
                "java.io.File",
                "java.util.Currency",
                "java.time.ZoneId",
                "java.util.Locale",
                // optional and atomic values
                "java.util.OptionalInt",
                "java.util.OptionalLong",
                "java.util.OptionalDouble",
                "java.util.concurrent.atomic.AtomicBoolean",
                // java.sql types
                "java.sql.Date",
                "java.sql.Timestamp");
    }

    @Test
    void convertersAreStateless() {
        assertThat(convert(UUID.class, new UUID(0, 1))).isEqualTo(new CellValue.Text(new UUID(0, 1).toString()));
        assertThat(convert(UUID.class, new UUID(0, 2))).isEqualTo(new CellValue.Text(new UUID(0, 2).toString()));
        assertThat(convert(OptionalInt.class, OptionalInt.of(1))).isEqualTo(new CellValue.Numeric(1));
        assertThat(convert(OptionalInt.class, OptionalInt.empty())).isSameAs(CellValue.blank());
        assertThat(convert(OptionalInt.class, OptionalInt.of(2))).isEqualTo(new CellValue.Numeric(2));
    }

    private static Set<String> names(Map<Class<?>, CellConverter<?>> converters) {
        return converters.keySet().stream().map(Class::getName).collect(Collectors.toSet());
    }
}
