package cloud.baldilorenzo.sheetsmith.style;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fails the build when a mirrored enum and its Apache POI counterpart diverge, for example after a POI upgrade
 * that adds constants.
 */
class MirroredEnumCoverageTest {

    static Stream<Arguments> mirroredEnums() {
        return Stream.of(
                Arguments.of(Align.class, HorizontalAlignment.class),
                Arguments.of(VerticalAlign.class, VerticalAlignment.class),
                Arguments.of(Border.class, BorderStyle.class),
                Arguments.of(Fill.class, FillPatternType.class),
                Arguments.of(Underline.class, FontUnderline.class));
    }

    @ParameterizedTest(name = "{0} mirrors {1}")
    @MethodSource("mirroredEnums")
    void libraryEnumHasOneConstantPerPoiConstantPlusInherit(Class<? extends Enum<?>> library,
                                                            Class<? extends Enum<?>> poi) {
        Set<String> libraryNames = names(library);

        assertThat(libraryNames).contains("INHERIT");
        libraryNames.remove("INHERIT");
        assertThat(libraryNames).containsExactlyInAnyOrderElementsOf(names(poi));
    }

    private static Set<String> names(Class<? extends Enum<?>> type) {
        return Arrays.stream(type.getEnumConstants()).map(Enum::name).collect(Collectors.toSet());
    }
}
