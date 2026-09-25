package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.style.Align;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.Fill;
import cloud.baldilorenzo.sheetsmith.style.Script;
import cloud.baldilorenzo.sheetsmith.style.Underline;
import cloud.baldilorenzo.sheetsmith.style.VerticalAlign;
import org.apache.poi.ss.usermodel.Font;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PoiMappingTest {

    @ParameterizedTest
    @EnumSource(value = Align.class, mode = EnumSource.Mode.EXCLUDE, names = "INHERIT")
    void convertsEveryAlignByName(Align align) {
        assertThat(PoiMapping.horizontalAlignment(align).name()).isEqualTo(align.name());
    }

    @ParameterizedTest
    @EnumSource(value = VerticalAlign.class, mode = EnumSource.Mode.EXCLUDE, names = "INHERIT")
    void convertsEveryVerticalAlignByName(VerticalAlign align) {
        assertThat(PoiMapping.verticalAlignment(align).name()).isEqualTo(align.name());
    }

    @ParameterizedTest
    @EnumSource(value = Border.class, mode = EnumSource.Mode.EXCLUDE, names = "INHERIT")
    void convertsEveryBorderByName(Border border) {
        assertThat(PoiMapping.borderStyle(border).name()).isEqualTo(border.name());
    }

    @ParameterizedTest
    @EnumSource(value = Fill.class, mode = EnumSource.Mode.EXCLUDE, names = "INHERIT")
    void convertsEveryFillByName(Fill fill) {
        assertThat(PoiMapping.fillPattern(fill).name()).isEqualTo(fill.name());
    }

    @ParameterizedTest
    @EnumSource(value = Underline.class, mode = EnumSource.Mode.EXCLUDE, names = "INHERIT")
    void convertsEveryUnderlineByName(Underline underline) {
        assertThat(PoiMapping.fontUnderline(underline).name()).isEqualTo(underline.name());
    }

    @Test
    void convertsScriptToFontTypeOffset() {
        assertThat(PoiMapping.typeOffset(Script.NONE)).isEqualTo(Font.SS_NONE);
        assertThat(PoiMapping.typeOffset(Script.SUPER)).isEqualTo(Font.SS_SUPER);
        assertThat(PoiMapping.typeOffset(Script.SUB)).isEqualTo(Font.SS_SUB);
    }

    @Test
    void rejectsInherit() {
        assertThatIllegalArgumentException().isThrownBy(() -> PoiMapping.horizontalAlignment(Align.INHERIT))
                .withMessageContaining("Align.INHERIT");
        assertThatIllegalArgumentException().isThrownBy(() -> PoiMapping.verticalAlignment(VerticalAlign.INHERIT))
                .withMessageContaining("VerticalAlign.INHERIT");
        assertThatIllegalArgumentException().isThrownBy(() -> PoiMapping.borderStyle(Border.INHERIT))
                .withMessageContaining("Border.INHERIT");
        assertThatIllegalArgumentException().isThrownBy(() -> PoiMapping.fillPattern(Fill.INHERIT))
                .withMessageContaining("Fill.INHERIT");
        assertThatIllegalArgumentException().isThrownBy(() -> PoiMapping.fontUnderline(Underline.INHERIT))
                .withMessageContaining("Underline.INHERIT");
        assertThatIllegalArgumentException().isThrownBy(() -> PoiMapping.typeOffset(Script.INHERIT))
                .withMessageContaining("Script.INHERIT");
    }

    @Test
    void rejectsNull() {
        assertThatNullPointerException().isThrownBy(() -> PoiMapping.borderStyle(null));
        assertThatNullPointerException().isThrownBy(() -> PoiMapping.typeOffset(null));
    }
}
