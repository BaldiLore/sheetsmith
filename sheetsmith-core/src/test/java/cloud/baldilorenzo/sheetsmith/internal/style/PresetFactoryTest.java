package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.internal.metadata.MetadataValidator;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PresetFactoryTest {

    private static final String A = "#4472C4";
    private static final String WHITE = "#FFFFFF";
    private static final String BLACK = "#000000";
    private static final List<String> ACCENTS =
            List.of("#4472C4", "#ED7D31", "#FFC000", "#70AD47", "#000000", "#FFFFFF", "DARK_RED", "LIGHT_YELLOW");

    private static final StyleAttributes TITLE =
            StyleAttributes.builder().bold(true).fontSize(14).fontColor("#335693").build();

    @Test
    void light() {
        assertThat(PresetFactory.layers(TablePreset.LIGHT, A)).isEqualTo(new PresetFactory.Layers(
                TITLE,
                StyleAttributes.builder().bold(true).fontColor("#335693")
                        .borderBottom(Border.MEDIUM).borderBottomColor(A).build(),
                StyleAttributes.builder().borderBottom(Border.THIN).borderBottomColor("#D0DCF0").build(),
                StyleAttributes.builder().fillColor("#E3EAF6").build(),
                StyleAttributes.EMPTY));
    }

    @Test
    void medium() {
        assertThat(PresetFactory.layers(TablePreset.MEDIUM, A)).isEqualTo(new PresetFactory.Layers(
                TITLE,
                StyleAttributes.builder().bold(true).fillColor(A).fontColor(WHITE).build(),
                StyleAttributes.builder().border(Border.THIN).borderColor("#B4C7E7").build(),
                StyleAttributes.builder().fillColor("#DAE3F3").build(),
                StyleAttributes.EMPTY));
    }

    @Test
    void dark() {
        assertThat(PresetFactory.layers(TablePreset.DARK, A)).isEqualTo(new PresetFactory.Layers(
                TITLE,
                StyleAttributes.builder().bold(true).fillColor("#223962").fontColor(WHITE).build(),
                StyleAttributes.EMPTY,
                StyleAttributes.builder().fillColor(A).fontColor(WHITE).build(),
                StyleAttributes.builder().fillColor("#335693").fontColor(WHITE).build()));
    }

    @ParameterizedTest
    @EnumSource(value = TablePreset.class, names = {"LIGHT", "MEDIUM", "DARK"})
    void accentCaseDoesNotChangeTheLayers(TablePreset preset) {
        assertThat(PresetFactory.layers(preset, "#4472c4")).isEqualTo(PresetFactory.layers(preset, A));
    }

    @Test
    void textOnLightAccentsIsBlack() {
        PresetFactory.Layers medium = PresetFactory.layers(TablePreset.MEDIUM, "#FFC000");
        PresetFactory.Layers dark = PresetFactory.layers(TablePreset.DARK, "#FFC000");

        assertThat(medium.headerBase().fontColor()).isEqualTo(BLACK);
        assertThat(dark.bodyOdd().fontColor()).isEqualTo(BLACK);
        assertThat(dark.headerBase().fontColor()).isEqualTo(WHITE);
    }

    @Test
    void noneProducesNoLayers() {
        assertThat(PresetFactory.layers(TablePreset.NONE, A)).isEqualTo(PresetFactory.Layers.NONE);
    }

    @Test
    void inheritMustBeResolvedFirst() {
        assertThatIllegalArgumentException().isThrownBy(() -> PresetFactory.layers(TablePreset.INHERIT, A));
    }

    @ParameterizedTest
    @EnumSource(value = TablePreset.class, names = {"LIGHT", "MEDIUM", "DARK"})
    void everyValidAccentProducesValidColours(TablePreset preset) {
        for (String accent : ACCENTS) {
            PresetFactory.Layers layers = PresetFactory.layers(preset, accent);

            Stream.of(layers.title(), layers.headerBase(), layers.bodyBase(), layers.bodyOdd(), layers.bodyEven())
                    .flatMap(layer -> Stream.of(layer.fontColor(), layer.fillColor(), layer.borderTopColor(),
                            layer.borderBottomColor(), layer.borderLeftColor(), layer.borderRightColor()))
                    .filter(color -> color != null)
                    .forEach(color -> assertThat(MetadataValidator.isValidColor(color))
                            .as("%s %s: %s", preset, accent, color).isTrue());
            assertThat(layers.title().bold()).isTrue();
            assertThat(layers.headerBase().bold()).isTrue();
        }
    }
}
