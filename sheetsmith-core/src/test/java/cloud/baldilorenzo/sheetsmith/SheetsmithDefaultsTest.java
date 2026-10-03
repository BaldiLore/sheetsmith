package cloud.baldilorenzo.sheetsmith;

import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class SheetsmithDefaultsTest {

    @Test
    void standardValues() {
        assertThat(SheetsmithDefaults.standard()).isEqualTo(new SheetsmithDefaults(
                "yyyy-mm-dd", "yyyy-mm-dd hh:mm:ss", "", TablePreset.NONE, "#4472C4"));
    }

    @Test
    void acceptsIndexedAccentColourAndEmptyNumberFormat() {
        SheetsmithDefaults defaults = new SheetsmithDefaults("d", "d h", "", TablePreset.DARK, "DARK_BLUE");

        assertThat(defaults.accentColor()).isEqualTo("DARK_BLUE");
    }

    @Test
    void rejectsInvalidValues() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new SheetsmithDefaults(" ", "d h", "", TablePreset.NONE, "#FFFFFF"))
                .withMessageContaining("dateFormat");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new SheetsmithDefaults("d", "", "", TablePreset.NONE, "#FFFFFF"))
                .withMessageContaining("dateTimeFormat");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new SheetsmithDefaults("d", "d h", "", TablePreset.INHERIT, "#FFFFFF"))
                .withMessageContaining("INHERIT");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new SheetsmithDefaults("d", "d h", "", TablePreset.NONE, "blue"))
                .withMessageContaining("blue");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new SheetsmithDefaults("d", "d h", "", TablePreset.NONE, ""));
    }

    @Test
    void rejectsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new SheetsmithDefaults("d", "d h", null, TablePreset.NONE, "#FFFFFF"))
                .withMessage("numberFormat");
        assertThatNullPointerException()
                .isThrownBy(() -> new SheetsmithDefaults("d", "d h", "", null, "#FFFFFF"))
                .withMessage("preset");
    }
}
