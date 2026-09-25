package cloud.baldilorenzo.sheetsmith.internal.convert;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellConverterFactory;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;
import cloud.baldilorenzo.sheetsmith.fixtures.WriterSheets;
import cloud.baldilorenzo.sheetsmith.internal.metadata.MetadataExtractor;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.tuple;

class ConverterBinderTest {

    private final MetadataExtractor extractor = new MetadataExtractor();
    private final ConverterBinder binder =
            new ConverterBinder(new ConverterRegistry(Map.of()), new ReflectiveConverterFactory());

    @Test
    void fieldLevelConverterWinsOverRegisteredOnes() {
        CellConverter<Object> registered = (value, context) -> CellValue.text("registered");
        ConverterBinder binder = new ConverterBinder(new ConverterRegistry(Map.of(String.class, registered)),
                new ReflectiveConverterFactory());

        SheetBinding binding = binder.bind(extractor.extract(WriterSheets.FieldConverters.class));

        assertThat(binding.columns().get(0).converter()).isInstanceOf(WriterSheets.Upper.class);
        assertThat(binding.columns().get(1).converter()).isInstanceOf(WriterSheets.Describe.class);
    }

    @Test
    void columnsWithoutFieldConverterUseTheRegistry() {
        SheetBinding binding = binder.bind(extractor.extract(WriterSheets.Nullable.class));

        assertThat(binding.columns()).extracting(column -> (Object) column.converter()).containsExactly(
                BuiltInConverters.all().get(CharSequence.class),
                BuiltInConverters.all().get(Number.class),
                BuiltInConverters.all().get(LocalDate.class));
    }

    @Test
    void fieldLevelConvertersAreCreatedOncePerClass() {
        AtomicInteger created = new AtomicInteger();
        ReflectiveConverterFactory reflective = new ReflectiveConverterFactory();
        CellConverterFactory counting = new CellConverterFactory() {
            @Override
            public <C extends CellConverter<?>> C create(Class<C> converterClass) {
                created.incrementAndGet();
                return reflective.create(converterClass);
            }
        };
        ConverterBinder binder = new ConverterBinder(new ConverterRegistry(Map.of()), counting);

        SheetBinding first = binder.bind(extractor.extract(WriterSheets.SharedFieldConverter.class));
        SheetBinding second = binder.bind(extractor.extract(WriterSheets.SharedFieldConverter.class));

        assertThat(created).hasValue(1);
        assertThat(first.columns().get(0).converter()).isSameAs(first.columns().get(1).converter())
                .isSameAs(second.columns().get(0).converter());
    }

    @Test
    void v10TypeWithoutConverter() {
        assertThat(errors(binder, WriterSheets.V10Unsupported.class)).singleElement().satisfies(error -> {
            assertThat(error).extracting(ConfigurationError::code, ConfigurationError::element)
                    .containsExactly("V-10", "when");
            assertThat(error.message()).contains("java.util.Date", "@ExcelColumn(converter = ...)");
        });
    }

    @Test
    void v10AmbiguousConverter() {
        CellConverter<Object> converter = (value, context) -> CellValue.blank();
        ConverterBinder binder = new ConverterBinder(new ConverterRegistry(Map.of(
                WriterSheets.Labelled.class, converter, WriterSheets.Coded.class, converter)),
                new ReflectiveConverterFactory());

        assertThat(errors(binder, WriterSheets.V10Ambiguous.class)).singleElement().satisfies(error -> {
            assertThat(error.code()).isEqualTo("V-10");
            assertThat(error.message()).contains("ambiguous");
        });
    }

    @Test
    void v11FieldConverterForAnIncompatibleType() {
        assertThat(errors(binder, WriterSheets.V11Incompatible.class)).singleElement().satisfies(error -> {
            assertThat(error.code()).isEqualTo("V-11");
            assertThat(error.message()).contains("java.lang.Integer", "java.lang.String");
        });
    }

    @Test
    void v12FieldConverterThatCannotBeCreated() {
        assertThat(errors(binder, WriterSheets.V12Uncreatable.class)).singleElement().satisfies(error -> {
            assertThat(error.code()).isEqualTo("V-12");
            assertThat(error.message()).contains("no public no-argument constructor");
        });
    }

    @Test
    void v12FactoryReturningNull() {
        CellConverterFactory nullFactory = new CellConverterFactory() {
            @Override
            public <C extends CellConverter<?>> C create(Class<C> converterClass) {
                return null;
            }
        };
        ConverterBinder binder = new ConverterBinder(new ConverterRegistry(Map.of()), nullFactory);

        assertThat(errors(binder, WriterSheets.FieldConverters.class)).extracting(ConfigurationError::code)
                .containsExactly("V-12", "V-12");
    }

    @Test
    void allBindingErrorsAreReportedTogether() {
        assertThat(errors(binder, WriterSheets.ManyBindingErrors.class))
                .extracting(ConfigurationError::code, ConfigurationError::element)
                .containsExactly(tuple("V-10", "a"), tuple("V-11", "b"), tuple("V-12", "c"));
    }

    private List<ConfigurationError> errors(ConverterBinder binder, Class<?> type) {
        SheetsmithConfigurationException exception = catchThrowableOfType(SheetsmithConfigurationException.class,
                () -> binder.bind(extractor.extract(type)));
        assertThat(exception).as("binding of %s", type.getSimpleName()).isNotNull();
        return exception.errors();
    }
}
