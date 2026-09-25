package cloud.baldilorenzo.sheetsmith.convert;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class CellValueTest {

    @Test
    void factoriesCreateTheMatchingKind() {
        assertThat(CellValue.text("a")).isEqualTo(new CellValue.Text("a"));
        assertThat(CellValue.number(1.5)).isEqualTo(new CellValue.Numeric(1.5));
        assertThat(CellValue.bool(true)).isEqualTo(new CellValue.Bool(true));
        assertThat(CellValue.date(LocalDate.of(2026, 1, 2))).isEqualTo(new CellValue.Date(LocalDate.of(2026, 1, 2)));
        assertThat(CellValue.dateTime(LocalDateTime.of(2026, 1, 2, 3, 4)))
                .isEqualTo(new CellValue.DateTime(LocalDateTime.of(2026, 1, 2, 3, 4)));
    }

    @Test
    void blankIsASingleton() {
        assertThat(CellValue.blank()).isInstanceOf(CellValue.Blank.class).isSameAs(CellValue.blank());
    }

    @Test
    void componentsRejectNull() {
        assertThatNullPointerException().isThrownBy(() -> CellValue.text(null));
        assertThatNullPointerException().isThrownBy(() -> CellValue.date(null));
        assertThatNullPointerException().isThrownBy(() -> CellValue.dateTime(null));
    }
}
