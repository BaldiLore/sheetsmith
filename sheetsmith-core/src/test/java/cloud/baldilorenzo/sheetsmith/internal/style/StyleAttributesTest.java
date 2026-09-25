package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.style.Align;
import cloud.baldilorenzo.sheetsmith.style.Border;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class StyleAttributesTest {

    @Test
    void setAttributesOfTheUpperLayerReplaceTheLowerOnes() {
        StyleAttributes under = StyleAttributes.builder().bold(true).fontSize(10).align(Align.LEFT).build();
        StyleAttributes over = StyleAttributes.builder().bold(false).fillColor("#FFFFFF").build();

        assertThat(under.merge(over)).isEqualTo(StyleAttributes.builder()
                .bold(false).fontSize(10).align(Align.LEFT).fillColor("#FFFFFF").build());
    }

    @Test
    void unsetAttributesNeverOverrideSetOnes() {
        StyleAttributes under = StyleAttributes.builder().border(Border.THIN).borderColor("RED").build();

        assertThat(under.merge(StyleAttributes.EMPTY)).isEqualTo(under);
        assertThat(StyleAttributes.EMPTY.merge(under)).isEqualTo(under);
        assertThat(under.merge(StyleAttributes.builder().borderTop(Border.THICK).build()))
                .isEqualTo(under.toBuilder().borderTop(Border.THICK).build());
    }

    @Test
    void emptyHasEveryComponentUnset() {
        assertThat(StyleAttributes.EMPTY).hasAllNullFieldsOrProperties();
    }

    @Test
    void inheritIsRejected() {
        assertThatIllegalArgumentException().isThrownBy(() -> StyleAttributes.builder().align(Align.INHERIT).build());
        assertThatIllegalArgumentException().isThrownBy(() -> StyleAttributes.builder().border(Border.INHERIT).build());
    }
}
