package cloud.baldilorenzo.sheetsmith;

import cloud.baldilorenzo.sheetsmith.fixtures.ValidSheets;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SheetDataTest {

    @Test
    void rowsAreCopiedIntoAnUnmodifiableList() {
        List<ValidSheets.Person> rows = new ArrayList<>(List.of(new ValidSheets.Person("a", 1)));

        SheetData<ValidSheets.Person> data = new SheetData<>("S", ValidSheets.Person.class, rows);
        rows.add(new ValidSheets.Person("b", 2));

        assertThat(data.rows()).hasSize(1);
        assertThatThrownBy(() -> data.rows().add(null)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void nullElementsAreKeptForTheWriterToReport() {
        SheetData<ValidSheets.Person> data =
                SheetData.of("S", ValidSheets.Person.class, Arrays.asList(new ValidSheets.Person("a", 1), null));

        assertThat(data.rows()).hasSize(2).containsNull();
    }

    @Test
    void ofAcceptsListsOfASubtype() {
        List<ValidSheets.Employee> employees = List.of(new ValidSheets.Employee(1, "dev"));

        SheetData<ValidSheets.Base> data = SheetData.of("S", ValidSheets.Base.class, employees);

        assertThat(data.rows()).containsExactlyElementsOf(employees);
    }

    @Test
    void componentsRejectNull() {
        assertThatNullPointerException().isThrownBy(() -> SheetData.of(null, ValidSheets.Person.class, List.of()))
                .withMessage("name");
        assertThatNullPointerException().isThrownBy(() -> SheetData.of("S", null, List.of())).withMessage("type");
        assertThatNullPointerException().isThrownBy(() -> SheetData.of("S", ValidSheets.Person.class, null))
                .withMessage("rows");
    }
}
