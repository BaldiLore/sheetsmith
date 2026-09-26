package cloud.baldilorenzo.sheetsmith.autoconfigure;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalidabstract.AbstractSheet;
import cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalidinterface.InterfaceSheet;
import cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalidnested.InnerSheetHolder;
import cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalidstaticnested.StaticSheetHolder;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class SheetsmithStartupValidatorTest {

    private static final String SCAN = "cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SheetsmithAutoConfiguration.class));

    @Test
    void invalidAbstractSheetClassStopsTheApplicationAtStartup() {
        assertStartupFailsWith(SCAN + ".invalidabstract", tuple("V-04", AbstractSheet.class));
    }

    @Test
    void invalidNonStaticNestedSheetClassStopsTheApplicationAtStartup() {
        assertStartupFailsWith(SCAN + ".invalidnested", tuple("V-04", InnerSheetHolder.InnerSheet.class));
    }

    @Test
    void invalidStaticNestedSheetClassStopsTheApplicationAtStartup() {
        assertStartupFailsWith(SCAN + ".invalidstaticnested", tuple("V-04", StaticSheetHolder.StaticSheet.class));
    }

    @Test
    void invalidInterfaceAnnotatedWithExcelSheetStopsTheApplicationAtStartup() {
        assertStartupFailsWith(SCAN + ".invalidinterface", tuple("V-02", InterfaceSheet.class));
    }

    @Test
    void validAbstractAndNestedSheetClassesPassTheStartupValidation() {
        assertStartupSucceeds(SCAN + ".validtypes");
    }

    @Test
    void typesOnlyMetaAnnotatedWithExcelSheetAreNotValidated() {
        assertStartupSucceeds(SCAN + ".metaannotated");
    }

    @Test
    void annotationTypesAnnotatedWithExcelSheetAreNotValidated() {
        assertStartupSucceeds(SCAN + ".annotationtype");
    }

    private void assertStartupFailsWith(String scannedPackage, Tuple expected) {
        runner.withPropertyValues("sheetsmith.validation.packages=" + scannedPackage).run(context -> {
            assertThat(context).hasFailed();
            SheetsmithConfigurationException exception =
                    cause(context.getStartupFailure(), SheetsmithConfigurationException.class);
            assertThat(exception.errors())
                    .extracting(ConfigurationError::code, ConfigurationError::type)
                    .containsExactly(expected);
        });
    }

    private void assertStartupSucceeds(String scannedPackage) {
        runner.withPropertyValues("sheetsmith.validation.packages=" + scannedPackage).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(SheetsmithStartupValidator.class);
        });
    }

    private static <T extends Throwable> T cause(Throwable failure, Class<T> type) {
        for (Throwable t = failure; t != null; t = t.getCause()) {
            if (type.isInstance(t)) {
                return type.cast(t);
            }
        }
        throw new AssertionError("no " + type.getSimpleName() + " in " + failure);
    }
}
