package cloud.baldilorenzo.sheetsmith;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SheetsmithExceptionTest {

    @Test
    void configurationExceptionListsOneErrorPerLine() {
        SheetsmithConfigurationException exception = new SheetsmithConfigurationException(List.of(
                new ConfigurationError("V-06", String.class, "amount", "style 'money' not found"),
                new ConfigurationError("V-02", String.class, "", "no columns"),
                new ConfigurationError("V-18", null, "", "the sheet list is empty")));

        assertThat(exception.getMessage().lines()).containsExactly(
                "[V-06] java.lang.String.amount: style 'money' not found",
                "[V-02] java.lang.String: no columns",
                "[V-18] the sheet list is empty");
        assertThat(exception.errors()).hasSize(3);
    }

    @Test
    void configurationExceptionRequiresAtLeastOneError() {
        assertThatIllegalArgumentException().isThrownBy(() -> new SheetsmithConfigurationException(List.of()));
    }

    @Test
    void generationExceptionNamesSheetRowAndField() {
        IllegalStateException cause = new IllegalStateException("boom");
        SheetsmithGenerationException exception =
                new SheetsmithGenerationException("converter failed", "Invoices", 3, "amount", cause);

        assertThat(exception).hasMessage("converter failed (sheet 'Invoices', row 3, field 'amount')")
                .hasCause(cause);
        assertThat(exception.sheetName()).isEqualTo("Invoices");
        assertThat(exception.rowIndex()).isEqualTo(3);
        assertThat(exception.fieldName()).contains("amount");
    }

    @Test
    void generationExceptionOmitsRowAndFieldWhenNotSpecific() {
        SheetsmithGenerationException exception =
                new SheetsmithGenerationException("too many rows", "Invoices", 0, null, null);

        assertThat(exception).hasMessage("too many rows (sheet 'Invoices')");
        assertThat(exception.fieldName()).isEmpty();
    }
}
