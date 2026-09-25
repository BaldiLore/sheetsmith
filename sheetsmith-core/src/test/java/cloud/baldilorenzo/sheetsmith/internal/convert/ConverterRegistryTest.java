package cloud.baldilorenzo.sheetsmith.internal.convert;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;
import cloud.baldilorenzo.sheetsmith.fixtures.WriterSheets;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ConverterRegistryTest {

    interface Base {
    }

    interface Middle extends Base {
    }

    interface Other {
    }

    static class Parent implements Middle {
    }

    static class Child extends Parent {
    }

    static class GrandChild extends Child implements Other {
    }

    private static CellConverter<Object> named(String name) {
        return (value, context) -> CellValue.text(name);
    }

    @Test
    void builtInConvertersCoverTheNativeTypes() {
        ConverterRegistry registry = new ConverterRegistry(Map.of());
        Map<Class<?>, CellConverter<?>> builtIn = BuiltInConverters.all();

        assertThat(registry.find(String.class)).isSameAs(builtIn.get(CharSequence.class));
        assertThat(registry.find(StringBuilder.class)).isSameAs(builtIn.get(CharSequence.class));
        assertThat(registry.find(char.class)).isSameAs(builtIn.get(Character.class));
        assertThat(registry.find(int.class)).isSameAs(builtIn.get(Number.class));
        assertThat(registry.find(BigDecimal.class)).isSameAs(builtIn.get(Number.class));
        assertThat(registry.find(boolean.class)).isSameAs(builtIn.get(Boolean.class));
        assertThat(registry.find(WriterSheets.Color.class)).isSameAs(builtIn.get(Enum.class));
        assertThat(registry.find(LocalDate.class)).isSameAs(builtIn.get(LocalDate.class));
        assertThat(registry.find(LocalDateTime.class)).isSameAs(builtIn.get(LocalDateTime.class));
        assertThat(registry.find(Date.class)).isNull();
        assertThat(registry.find(Object.class)).isNull();
    }

    @Test
    void exactTypeWinsOverSupertypes() {
        CellConverter<Object> exact = named("exact");
        ConverterRegistry registry = new ConverterRegistry(Map.of(
                Child.class, exact, Parent.class, named("parent"), Middle.class, named("middle")));

        assertThat(registry.find(Child.class)).isSameAs(exact);
    }

    @Test
    void closestSuperclassWins() {
        CellConverter<Object> child = named("child");
        ConverterRegistry registry = new ConverterRegistry(Map.of(Child.class, child, Parent.class, named("parent")));

        assertThat(registry.find(GrandChild.class)).isSameAs(child);
    }

    @Test
    void superclassWinsOverInterface() {
        CellConverter<Object> parent = named("parent");
        ConverterRegistry registry = new ConverterRegistry(Map.of(Parent.class, parent, Other.class, named("other")));

        assertThat(registry.find(GrandChild.class)).isSameAs(parent);
    }

    @Test
    void closestInterfaceWinsByBreadthFirstDistance() {
        CellConverter<Object> middle = named("middle");
        ConverterRegistry registry = new ConverterRegistry(Map.of(Middle.class, middle, Base.class, named("base")));

        assertThat(registry.find(Parent.class)).isSameAs(middle);
        assertThat(registry.find(GrandChild.class)).isSameAs(middle);
    }

    @Test
    void interfaceOfTheClassIsCloserThanInterfaceOfItsSuperclass() {
        CellConverter<Object> other = named("other");
        ConverterRegistry registry = new ConverterRegistry(Map.of(Other.class, other, Middle.class, named("middle")));

        assertThat(registry.find(GrandChild.class)).isSameAs(other);
    }

    @Test
    void interfacesAtTheSameDistanceAreAmbiguous() {
        ConverterRegistry registry = new ConverterRegistry(Map.of(
                WriterSheets.Labelled.class, named("labelled"), WriterSheets.Coded.class, named("coded")));

        assertThatExceptionOfType(ConverterRegistry.AmbiguousConverterException.class)
                .isThrownBy(() -> registry.find(WriterSheets.Tag.class))
                .withMessageContaining("Labelled").withMessageContaining("Coded");
    }

    @Test
    void applicationConvertersWinOverBuiltInOnes() {
        CellConverter<Object> number = named("number");
        CellConverter<Object> text = named("text");
        ConverterRegistry registry = new ConverterRegistry(Map.of(Number.class, number, CharSequence.class, text));

        assertThat(registry.find(Integer.class)).isSameAs(number);
        assertThat(registry.find(String.class)).isSameAs(text);
    }

    @Test
    void applicationSupertypeWinsOverBuiltInExactType() {
        CellConverter<Object> any = named("any");
        ConverterRegistry registry = new ConverterRegistry(Map.of(Object.class, any));

        assertThat(registry.find(LocalDate.class)).isSameAs(any);
    }
}
