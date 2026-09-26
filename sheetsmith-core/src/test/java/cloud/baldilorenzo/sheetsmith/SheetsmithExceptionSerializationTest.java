package cloud.baldilorenzo.sheetsmith;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SheetsmithExceptionSerializationTest {

    @Test
    void configurationExceptionKeepsItsErrorsAfterSerialisation() throws Exception {
        List<ConfigurationError> errors = List.of(
                new ConfigurationError("V-06", String.class, "amount", "style 'money' not found"),
                new ConfigurationError("V-02", Integer.class, "", "no columns"),
                new ConfigurationError("V-18", null, "", "the sheet list is empty"));
        SheetsmithConfigurationException original = new SheetsmithConfigurationException(errors);

        SheetsmithConfigurationException copy = roundTrip(original);

        assertThat(copy.errors()).isEqualTo(errors);
        assertThat(copy).hasMessage(original.getMessage());
        ConfigurationError extra = new ConfigurationError("V-01", String.class, "", "extra");
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> copy.errors().add(extra));
    }

    @Test
    void generationExceptionKeepsItsLocationAndCauseAfterSerialisation() throws Exception {
        IllegalStateException cause = new IllegalStateException("boom");
        SheetsmithGenerationException original =
                new SheetsmithGenerationException("converter failed", "Invoices", 3, "amount", cause);

        SheetsmithGenerationException copy = roundTrip(original);

        assertThat(copy.sheetName()).isEqualTo("Invoices");
        assertThat(copy.rowIndex()).isEqualTo(3);
        assertThat(copy.fieldName()).contains("amount");
        assertThat(copy).hasMessage(original.getMessage());
        assertThat(copy.getCause()).isInstanceOf(IllegalStateException.class).hasMessage("boom");
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(T value) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) in.readObject();
        }
    }
}
