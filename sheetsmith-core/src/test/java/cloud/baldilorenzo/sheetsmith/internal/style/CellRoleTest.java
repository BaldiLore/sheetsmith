package cloud.baldilorenzo.sheetsmith.internal.style;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CellRoleTest {

    @Test
    void dataRowsAreNumberedFromOneSoTheFirstRowIsOdd() {
        assertThat(CellRole.data(1, 3, 0, 2)).isEqualTo(new CellRole(true, false, false, true, false));
        assertThat(CellRole.data(2, 3, 1, 2)).isEqualTo(new CellRole(false, false, true, false, true));
        assertThat(CellRole.data(3, 3, 1, 3)).isEqualTo(new CellRole(false, true, false, false, false));
    }

    @Test
    void singleRowAndSingleColumnHaveBothFlags() {
        assertThat(CellRole.data(1, 1, 0, 1)).isEqualTo(new CellRole(true, true, false, true, true));
    }

    @Test
    void headerHasOnlyColumnFlags() {
        assertThat(CellRole.header(0, 1)).isEqualTo(new CellRole(false, false, false, true, true));
    }

    @Test
    void indexesOutOfRangeAreRejected() {
        assertThatIllegalArgumentException().isThrownBy(() -> CellRole.data(0, 3, 0, 1));
        assertThatIllegalArgumentException().isThrownBy(() -> CellRole.data(4, 3, 0, 1));
        assertThatIllegalArgumentException().isThrownBy(() -> CellRole.header(1, 1));
    }
}
