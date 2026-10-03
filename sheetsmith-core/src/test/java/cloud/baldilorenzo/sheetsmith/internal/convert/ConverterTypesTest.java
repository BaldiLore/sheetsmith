package cloud.baldilorenzo.sheetsmith.internal.convert;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;
import cloud.baldilorenzo.sheetsmith.convert.ConversionContext;
import cloud.baldilorenzo.sheetsmith.fixtures.WriterSheets;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConverterTypesTest {

    abstract static class Base<T> implements CellConverter<T> {
        @Override
        public CellValue convert(T value, ConversionContext context) {
            return CellValue.blank();
        }
    }

    static class BoundThroughSuperclass extends Base<Long> {
    }

    interface Special<X> extends CellConverter<X> {
    }

    static class BoundThroughInterface implements Special<Double> {
        @Override
        public CellValue convert(Double value, ConversionContext context) {
            return CellValue.number(value);
        }
    }

    static class Unbound<T> extends Base<T> {
    }

    static class ParameterizedArgument extends Base<List<String>> {
    }

    @SuppressWarnings("rawtypes")
    static class Raw implements CellConverter {
        @Override
        public CellValue convert(Object value, ConversionContext context) {
            return CellValue.blank();
        }
    }

    @Test
    void directImplementation() {
        assertThat(ConverterTypes.handledType(WriterSheets.Upper.class)).isEqualTo(String.class);
    }

    @Test
    void typeArgumentBoundInASuperclass() {
        assertThat(ConverterTypes.handledType(BoundThroughSuperclass.class)).isEqualTo(Long.class);
    }

    @Test
    void typeArgumentBoundInASuperinterface() {
        assertThat(ConverterTypes.handledType(BoundThroughInterface.class)).isEqualTo(Double.class);
    }

    @Test
    void parameterizedTypeArgumentGivesItsRawType() {
        assertThat(ConverterTypes.handledType(ParameterizedArgument.class)).isEqualTo(List.class);
    }

    @Test
    void arrayTypeArgument() {
        CellConverter<String[]> converter = new CellConverter<>() {
            @Override
            public CellValue convert(String[] value, ConversionContext context) {
                return CellValue.text(String.join(",", value));
            }
        };

        assertThat(ConverterTypes.handledType(converter.getClass())).isEqualTo(String[].class);
    }

    @Test
    void undeterminableTypes() {
        assertThat(ConverterTypes.handledType(Unbound.class)).isNull();
        assertThat(ConverterTypes.handledType(WriterSheets.ToText.class)).isNull();
        assertThat(ConverterTypes.handledType(Raw.class)).isNull();
    }
}
