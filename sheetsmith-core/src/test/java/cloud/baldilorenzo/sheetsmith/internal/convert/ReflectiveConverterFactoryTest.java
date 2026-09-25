package cloud.baldilorenzo.sheetsmith.internal.convert;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;
import cloud.baldilorenzo.sheetsmith.convert.ConversionContext;
import cloud.baldilorenzo.sheetsmith.fixtures.WriterSheets;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ReflectiveConverterFactoryTest {

    private final ReflectiveConverterFactory factory = new ReflectiveConverterFactory();

    public static class ThrowingConstructor implements CellConverter<String> {
        public ThrowingConstructor() {
            throw new IllegalStateException("boom");
        }

        @Override
        public CellValue convert(String value, ConversionContext context) {
            return CellValue.text(value);
        }
    }

    @Test
    void createsThroughThePublicNoArgumentConstructor() {
        assertThat(factory.create(WriterSheets.Upper.class)).isInstanceOf(WriterSheets.Upper.class);
    }

    @Test
    void rejectsClassesWithoutPublicNoArgumentConstructor() {
        assertThatIllegalArgumentException().isThrownBy(() -> factory.create(WriterSheets.NoDefaultConstructor.class))
                .withMessageContaining("no public no-argument constructor");
    }

    @Test
    void reportsConstructorFailures() {
        assertThatIllegalArgumentException().isThrownBy(() -> factory.create(ThrowingConstructor.class))
                .withMessageContaining("boom")
                .withCauseInstanceOf(IllegalStateException.class);
    }
}
