package cloud.baldilorenzo.sheetsmith.autoconfigure;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalidtemplate.UnusedInvalidTemplate;
import cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.sharedtemplate.SharedInvalidTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class SheetsmithStartupTemplateValidationTest {

    private static final String SCAN = "cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SheetsmithAutoConfiguration.class));

    @Test
    void invalidTemplateReferencedByNoSheetClassStopsTheApplicationAtStartup() {
        assertThat(startupErrors(SCAN + ".invalidtemplate"))
                .extracting(ConfigurationError::code, ConfigurationError::type, ConfigurationError::element)
                .containsExactly(tuple("V-13", UnusedInvalidTemplate.class, "@ExcelSheetTemplate"));
    }

    @Test
    void errorOfATemplateReferencedByTwoSheetClassesIsReportedOnce() {
        assertThat(startupErrors(SCAN + ".sharedtemplate"))
                .extracting(ConfigurationError::code, ConfigurationError::type, ConfigurationError::element)
                .containsExactly(tuple("V-13", SharedInvalidTemplate.class, "@ExcelSheetTemplate"));
    }

    private List<ConfigurationError> startupErrors(String scannedPackage) {
        SheetsmithConfigurationException[] exception = new SheetsmithConfigurationException[1];
        runner.withPropertyValues("sheetsmith.validation.packages=" + scannedPackage).run(context -> {
            assertThat(context).hasFailed();
            for (Throwable t = context.getStartupFailure(); t != null; t = t.getCause()) {
                if (t instanceof SheetsmithConfigurationException configuration) {
                    exception[0] = configuration;
                }
            }
        });
        assertThat(exception[0]).as("startup failure caused by a SheetsmithConfigurationException").isNotNull();
        return exception[0].errors();
    }
}
