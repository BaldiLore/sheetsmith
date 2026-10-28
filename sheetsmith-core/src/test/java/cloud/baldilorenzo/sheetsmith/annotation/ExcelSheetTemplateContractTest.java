package cloud.baldilorenzo.sheetsmith.annotation;

import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import cloud.baldilorenzo.sheetsmith.style.Toggle;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelSheetTemplateContractTest {

    @Test
    void attributesMatchTheHomonymousAttributesOfExcelSheet() throws NoSuchMethodException {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("preset", TablePreset.INHERIT);
        expected.put("accentColor", "");
        expected.put("styleSheets", new Class<?>[0]);
        expected.put("titleStyle", "");
        expected.put("freezeHeader", Toggle.INHERIT);
        expected.put("autoFilter", Toggle.INHERIT);
        expected.put("autoSizeColumns", Toggle.INHERIT);
        expected.put("outerBorder", Border.INHERIT);
        expected.put("outerBorderColor", "");
        expected.put("header", ExcelSheet.class.getMethod("header").getDefaultValue());
        expected.put("body", ExcelSheet.class.getMethod("body").getDefaultValue());

        assertThat(Arrays.stream(ExcelSheetTemplate.class.getDeclaredMethods()).map(Method::getName))
                .containsExactlyInAnyOrderElementsOf(expected.keySet());
        for (Map.Entry<String, Object> attribute : expected.entrySet()) {
            Method method = ExcelSheetTemplate.class.getMethod(attribute.getKey());
            Method onSheet = ExcelSheet.class.getMethod(attribute.getKey());
            assertThat(method.getGenericReturnType()).as("type of %s", attribute.getKey())
                    .isEqualTo(onSheet.getGenericReturnType());
            assertThat(method.getDefaultValue()).as("default of %s", attribute.getKey())
                    .isEqualTo(attribute.getValue())
                    .isEqualTo(onSheet.getDefaultValue());
        }
    }

    @Test
    void isRuntimeRetainedDocumentedAndTargetsTypes() {
        assertThat(ExcelSheetTemplate.class.getAnnotation(Retention.class).value())
                .isEqualTo(RetentionPolicy.RUNTIME);
        assertThat(ExcelSheetTemplate.class.isAnnotationPresent(Documented.class)).isTrue();
        assertThat(ExcelSheetTemplate.class.getAnnotation(Target.class).value()).containsExactly(ElementType.TYPE);
    }

    @Test
    void noneIsAFinalUnannotatedMarkerWithAPrivateConstructor() {
        Class<?> none = ExcelSheetTemplate.None.class;

        assertThat(Modifier.isFinal(none.getModifiers())).isTrue();
        assertThat(none.getAnnotations()).isEmpty();
        Constructor<?>[] constructors = none.getDeclaredConstructors();
        assertThat(constructors).singleElement()
                .satisfies(constructor -> assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue());
    }

    @Test
    void excelSheetTemplateDefaultsToNone() throws NoSuchMethodException {
        assertThat(ExcelSheet.class.getMethod("template").getDefaultValue()).isEqualTo(ExcelSheetTemplate.None.class);
    }
}
