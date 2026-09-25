package cloud.baldilorenzo.sheetsmith.annotation;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.fixtures.RepeatedStyles;
import cloud.baldilorenzo.sheetsmith.style.Align;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.Fill;
import cloud.baldilorenzo.sheetsmith.style.Script;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import cloud.baldilorenzo.sheetsmith.style.Toggle;
import cloud.baldilorenzo.sheetsmith.style.Underline;
import cloud.baldilorenzo.sheetsmith.style.VerticalAlign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationContractTest {

    private static final Object NO_DEFAULT = new Object();

    private static final String STRING = String.class.getName();
    private static final String INT = int.class.getName();
    private static final String BOOLEAN = boolean.class.getName();

    @Test
    void excelSheetHasTheDocumentedAttributes() {
        assertAttributes(ExcelSheet.class,
                attr("title", STRING, ""),
                attr("titleStyle", STRING, ""),
                attr("preset", TablePreset.class.getName(), TablePreset.INHERIT),
                attr("accentColor", STRING, ""),
                attr("styleSheets", "java.lang.Class<?>[]", new Class<?>[0]),
                attr("freezeHeader", BOOLEAN, true),
                attr("autoFilter", BOOLEAN, false),
                attr("autoSizeColumns", BOOLEAN, true),
                attr("outerBorder", Border.class.getName(), Border.INHERIT),
                attr("outerBorderColor", STRING, ""),
                attr("header", HeaderStyles.class.getName(), new AllDefaults(HeaderStyles.class)),
                attr("body", BodyStyles.class.getName(), new AllDefaults(BodyStyles.class)));
    }

    @Test
    void excelColumnHasTheDocumentedAttributes() {
        assertAttributes(ExcelColumn.class,
                attr("header", STRING, NO_DEFAULT),
                attr("order", INT, NO_DEFAULT),
                attr("width", INT, ExcelStyle.UNSET),
                attr("format", STRING, ""),
                attr("converter", "java.lang.Class<? extends " + CellConverter.class.getName() + "<?>>",
                        CellConverter.None.class),
                attr("headerStyle", STRING, ""),
                attr("styles", ColumnStyles.class.getName(), new AllDefaults(ColumnStyles.class)));
    }

    @Test
    void excelStyleHasTheDocumentedAttributes() {
        String border = Border.class.getName();
        String toggle = Toggle.class.getName();
        assertAttributes(ExcelStyle.class,
                attr("name", STRING, NO_DEFAULT),
                attr("align", Align.class.getName(), Align.INHERIT),
                attr("verticalAlign", VerticalAlign.class.getName(), VerticalAlign.INHERIT),
                attr("wrapText", toggle, Toggle.INHERIT),
                attr("shrinkToFit", toggle, Toggle.INHERIT),
                attr("rotation", INT, ExcelStyle.UNSET),
                attr("indent", INT, ExcelStyle.UNSET),
                attr("border", border, Border.INHERIT),
                attr("borderColor", STRING, ""),
                attr("borderTop", border, Border.INHERIT),
                attr("borderBottom", border, Border.INHERIT),
                attr("borderLeft", border, Border.INHERIT),
                attr("borderRight", border, Border.INHERIT),
                attr("borderTopColor", STRING, ""),
                attr("borderBottomColor", STRING, ""),
                attr("borderLeftColor", STRING, ""),
                attr("borderRightColor", STRING, ""),
                attr("fillColor", STRING, ""),
                attr("fillBackgroundColor", STRING, ""),
                attr("fillPattern", Fill.class.getName(), Fill.INHERIT),
                attr("fontName", STRING, ""),
                attr("fontSize", INT, ExcelStyle.UNSET),
                attr("bold", toggle, Toggle.INHERIT),
                attr("italic", toggle, Toggle.INHERIT),
                attr("strikeout", toggle, Toggle.INHERIT),
                attr("underline", Underline.class.getName(), Underline.INHERIT),
                attr("fontColor", STRING, ""),
                attr("script", Script.class.getName(), Script.INHERIT),
                attr("dataFormat", STRING, ""),
                attr("locked", toggle, Toggle.INHERIT),
                attr("hidden", toggle, Toggle.INHERIT),
                attr("quotePrefix", toggle, Toggle.INHERIT));
    }

    @Test
    void unsetSentinelIsIntegerMinValue() {
        assertThat(ExcelStyle.UNSET).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void excelStylesContainsExcelStyleArray() {
        assertAttributes(ExcelStyles.class, attr("value", ExcelStyle.class.getName() + "[]", NO_DEFAULT));
    }

    @Test
    void excelStyleSheetHasNoAttributes() {
        assertThat(ExcelStyleSheet.class.getDeclaredMethods()).isEmpty();
    }

    @Test
    void headerStylesHasTheDocumentedSlots() {
        assertAttributes(HeaderStyles.class,
                attr("base", STRING, ""),
                attr("firstColumn", STRING, ""),
                attr("lastColumn", STRING, ""));
    }

    @Test
    void bodyStylesHasTheDocumentedSlots() {
        assertAttributes(BodyStyles.class,
                attr("base", STRING, ""),
                attr("even", STRING, ""),
                attr("odd", STRING, ""),
                attr("firstRow", STRING, ""),
                attr("lastRow", STRING, ""),
                attr("firstColumn", STRING, ""),
                attr("lastColumn", STRING, ""));
    }

    @Test
    void columnStylesHasTheDocumentedSlots() {
        assertAttributes(ColumnStyles.class,
                attr("base", STRING, ""),
                attr("even", STRING, ""),
                attr("odd", STRING, ""),
                attr("firstRow", STRING, ""),
                attr("lastRow", STRING, ""));
    }

    @ParameterizedTest
    @ValueSource(classes = {ExcelSheet.class, ExcelColumn.class, ExcelStyle.class, ExcelStyles.class,
            ExcelStyleSheet.class, HeaderStyles.class, BodyStyles.class, ColumnStyles.class})
    void everyAnnotationIsRuntimeRetainedAndDocumented(Class<? extends Annotation> type) {
        assertThat(type.getAnnotation(Retention.class).value()).isEqualTo(RetentionPolicy.RUNTIME);
        assertThat(type.isAnnotationPresent(Documented.class)).isTrue();
    }

    @Test
    void annotationsHaveTheDocumentedTargets() {
        assertThat(targets(ExcelSheet.class)).containsExactly(ElementType.TYPE);
        assertThat(targets(ExcelColumn.class)).containsExactly(ElementType.FIELD);
        assertThat(targets(ExcelStyle.class)).containsExactly(ElementType.TYPE);
        assertThat(targets(ExcelStyles.class)).containsExactly(ElementType.TYPE);
        assertThat(targets(ExcelStyleSheet.class)).containsExactly(ElementType.TYPE);
        assertThat(targets(HeaderStyles.class)).isEmpty();
        assertThat(targets(BodyStyles.class)).isEmpty();
        assertThat(targets(ColumnStyles.class)).isEmpty();
    }

    @Test
    void excelStyleIsRepeatableThroughExcelStyles() {
        assertThat(ExcelStyle.class.getAnnotation(Repeatable.class).value()).isEqualTo(ExcelStyles.class);

        ExcelStyle[] styles = RepeatedStyles.class.getAnnotationsByType(ExcelStyle.class);

        assertThat(styles).extracting(ExcelStyle::name).containsExactly("header", "money");
        assertThat(RepeatedStyles.class.getAnnotation(ExcelStyles.class).value()).hasSize(2);
    }

    private static List<ElementType> targets(Class<? extends Annotation> type) {
        return Arrays.asList(type.getAnnotation(Target.class).value());
    }

    private static Attr attr(String name, String typeName, Object defaultValue) {
        return new Attr(name, typeName, defaultValue);
    }

    private static void assertAttributes(Class<? extends Annotation> type, Attr... expected) {
        var actual = Arrays.stream(type.getDeclaredMethods())
                .collect(Collectors.toMap(Method::getName, Function.identity()));

        assertThat(actual.keySet())
                .as("attributes of @%s", type.getSimpleName())
                .containsExactlyInAnyOrderElementsOf(Arrays.stream(expected).map(Attr::name).toList());

        for (Attr attr : expected) {
            Method method = actual.get(attr.name());
            String where = "@" + type.getSimpleName() + "." + attr.name();
            assertThat(method.getGenericReturnType().getTypeName()).as("type of %s", where).isEqualTo(attr.typeName());

            Object defaultValue = method.getDefaultValue();
            if (attr.defaultValue() == NO_DEFAULT) {
                assertThat(defaultValue).as("default of %s", where).isNull();
            } else if (attr.defaultValue() instanceof AllDefaults allDefaults) {
                assertThat(defaultValue).as("default of %s", where).isInstanceOf(allDefaults.type());
                assertHasOnlyDefaultValues((Annotation) defaultValue);
            } else {
                assertThat(defaultValue).as("default of %s", where).isEqualTo(attr.defaultValue());
            }
        }
    }

    private static void assertHasOnlyDefaultValues(Annotation annotation) {
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            try {
                assertThat(method.invoke(annotation))
                        .as("@%s.%s", annotation.annotationType().getSimpleName(), method.getName())
                        .isEqualTo(method.getDefaultValue());
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }
    }

    private record Attr(String name, String typeName, Object defaultValue) {
    }

    /** Expected default: an instance of the given annotation with every attribute at its own default. */
    private record AllDefaults(Class<? extends Annotation> type) {
    }
}
