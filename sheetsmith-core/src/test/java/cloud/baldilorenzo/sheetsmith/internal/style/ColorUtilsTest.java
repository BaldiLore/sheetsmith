package cloud.baldilorenzo.sheetsmith.internal.style;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

class ColorUtilsTest {

    private static final String ACCENT = "#4472C4";

    @Test
    void tintMixesWithWhite() {
        assertThat(ColorUtils.tint(ACCENT, 0)).isEqualTo(ACCENT);
        assertThat(ColorUtils.tint(ACCENT, 0.75)).isEqualTo("#D0DCF0");
        assertThat(ColorUtils.tint(ACCENT, 0.85)).isEqualTo("#E3EAF6");
        assertThat(ColorUtils.tint(ACCENT, 1)).isEqualTo("#FFFFFF");
    }

    @Test
    void shadeMixesWithBlack() {
        assertThat(ColorUtils.shade(ACCENT, 0)).isEqualTo(ACCENT);
        assertThat(ColorUtils.shade(ACCENT, 0.25)).isEqualTo("#335693");
        assertThat(ColorUtils.shade(ACCENT, 0.5)).isEqualTo("#223962");
        assertThat(ColorUtils.shade(ACCENT, 1)).isEqualTo("#000000");
    }

    @Test
    void hexInputIsCaseInsensitive() {
        assertThat(ColorUtils.shade("#4472c4", 0.5)).isEqualTo("#223962");
    }

    @Test
    void normalizeUpperCasesHexColoursOnly() {
        assertThat(ColorUtils.normalize("#4472c4")).isEqualTo("#4472C4");
        assertThat(ColorUtils.normalize("#4472C4")).isEqualTo("#4472C4");
        assertThat(ColorUtils.normalize("DARK_BLUE")).isEqualTo("DARK_BLUE");
        assertThat(ColorUtils.normalize(null)).isNull();
    }

    @Test
    void indexedColoursUseTheirDefaultRgb() {
        assertThat(ColorUtils.rgb("DARK_RED")).containsExactly(128, 0, 0);
        assertThat(ColorUtils.tint("WHITE", 0.5)).isEqualTo("#FFFFFF");
        assertThat(ColorUtils.rgb("AUTOMATIC")).containsExactly(0, 0, 0);
    }

    @Test
    void luminanceFollowsWcag() {
        assertThat(ColorUtils.luminance("#000000")).isZero();
        assertThat(ColorUtils.luminance("#FFFFFF")).isCloseTo(1, within(1e-9));
        assertThat(ColorUtils.luminance(ACCENT)).isCloseTo(0.1726, within(0.001));
    }

    @ParameterizedTest
    @ValueSource(strings = {"#4472C4", "#000000", "#223962", "DARK_RED", "#7F7F7F"})
    void contrastOnDarkColoursIsWhite(String color) {
        assertThat(ColorUtils.contrast(color)).isEqualTo("#FFFFFF");
    }

    @ParameterizedTest
    @ValueSource(strings = {"#FFFFFF", "#FFC000", "#E3EAF6", "YELLOW", "#BFBFBF"})
    void contrastOnLightColoursIsBlack(String color) {
        assertThat(ColorUtils.contrast(color)).isEqualTo("#000000");
    }

    @Test
    void fractionsOutsideZeroToOneAreRejected() {
        assertThatIllegalArgumentException().isThrownBy(() -> ColorUtils.tint(ACCENT, -0.1));
        assertThatIllegalArgumentException().isThrownBy(() -> ColorUtils.shade(ACCENT, 1.1));
        assertThatIllegalArgumentException().isThrownBy(() -> ColorUtils.shade(ACCENT, Double.NaN));
    }
}
