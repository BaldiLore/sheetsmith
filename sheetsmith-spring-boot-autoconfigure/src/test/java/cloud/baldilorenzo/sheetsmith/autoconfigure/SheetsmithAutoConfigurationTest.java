package cloud.baldilorenzo.sheetsmith.autoconfigure;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.SheetData;
import cloud.baldilorenzo.sheetsmith.Sheetsmith;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.SheetsmithDefaults;
import cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.Fixtures;
import cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalid.BlankHeaderSheet;
import cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.invalid.UnconvertibleSheet;
import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.tuple;

class SheetsmithAutoConfigurationTest {

    private static final String SCAN = "cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SheetsmithAutoConfiguration.class));

    @Test
    void sheetsmithBeanIsAvailableWithoutConfiguration() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(Sheetsmith.class).hasSingleBean(SheetsmithProperties.class)
                    .doesNotHaveBean(SheetsmithStartupValidator.class);

            Row row = firstDataRow(context.getBean(Sheetsmith.class), "Dated", Fixtures.Dated.class,
                    new Fixtures.Dated(LocalDate.of(2026, 9, 25), 3));
            assertThat(row.getCell(0).getCellStyle().getDataFormatString()).isEqualTo("yyyy-mm-dd");
            assertThat(row.getCell(1).getCellStyle().getDataFormatString()).isEqualTo("General");
        });
    }

    @Test
    void autoConfigurationIsRegisteredForSpringBoot() throws IOException {
        ClassPathResource imports = new ClassPathResource(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");

        assertThat(imports.getContentAsString(StandardCharsets.UTF_8).lines())
                .contains(SheetsmithAutoConfiguration.class.getName());
    }

    @Test
    void userDefinedSheetsmithReplacesTheDefaultOne() {
        Sheetsmith custom = Sheetsmith.builder().build();

        runner.withBean(Sheetsmith.class, () -> custom).run(context ->
                assertThat(context).getBean(Sheetsmith.class).isSameAs(custom));
    }

    @Test
    void converterBeansAreRegisteredByTheirGenericType() {
        runner.withUserConfiguration(ConverterBeans.class).run(context -> {
            Row row = firstDataRow(context.getBean(Sheetsmith.class), "Payments", Fixtures.Payment.class,
                    new Fixtures.Payment(new Fixtures.Money(new BigDecimal("9.90"), "EUR"), new Date(1000)));

            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("EUR 9.90");
            assertThat(row.getCell(1).getStringCellValue()).isEqualTo("epoch 1000");
        });
    }

    @Test
    void twoConverterBeansForTheSameTypeFailAtStartup() {
        runner.withUserConfiguration(DuplicateConverterBeans.class).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).rootCause()
                    .hasMessageContaining("'firstDateConverter'")
                    .hasMessageContaining("'secondDateConverter'")
                    .hasMessageContaining("java.util.Date");
        });
    }

    @Test
    void converterBeanWithUnresolvableTypeFailsAtStartup() {
        runner.withUserConfiguration(RawConverterBean.class).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).rootCause()
                    .hasMessageContaining("'rawConverter'")
                    .hasMessageContaining("CellConverter<T>");
        });
    }

    @Test
    void fieldConverterThatIsABeanIsTakenFromTheContext() {
        runner.withUserConfiguration(FieldConverterBean.class).run(context -> {
            Row row = firstDataRow(context.getBean(Sheetsmith.class), "Orders", Fixtures.Order.class,
                    new Fixtures.Order(new Fixtures.Money(new BigDecimal("5"), "EUR")));

            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("bean:5");
        });
    }

    @Test
    void fieldConverterThatIsNotABeanIsCreatedWithItsDependenciesButNotRegisteredForItsType() {
        runner.withBean(Fixtures.CurrencyFormatter.class, () -> new Fixtures.CurrencyFormatter("injected:"))
                .run(context -> {
                    Sheetsmith sheetsmith = context.getBean(Sheetsmith.class);

                    Row row = firstDataRow(sheetsmith, "Orders", Fixtures.Order.class,
                            new Fixtures.Order(new Fixtures.Money(new BigDecimal("7"), "EUR")));

                    assertThat(row.getCell(0).getStringCellValue()).isEqualTo("injected:7");
                    assertThat(context).doesNotHaveBean(Fixtures.FieldMoneyConverter.class);
                    SheetsmithConfigurationException exception = catchThrowableOfType(
                            SheetsmithConfigurationException.class,
                            () -> sheetsmith.validate(Fixtures.PlainMoney.class));
                    assertThat(exception.errors()).extracting(ConfigurationError::code).containsExactly("V-10");
                });
    }

    @Test
    void propertiesAreBound() {
        runner.withPropertyValues(
                        "sheetsmith.formats.date=dd/mm/yyyy",
                        "sheetsmith.formats.date-time=dd/mm/yyyy hh:mm",
                        "sheetsmith.formats.number=#,##0",
                        "sheetsmith.preset=DARK",
                        "sheetsmith.accent-color=DARK_BLUE")
                .run(context -> {
                    SheetsmithProperties properties = context.getBean(SheetsmithProperties.class);
                    assertThat(properties.formats()).isEqualTo(
                            new SheetsmithProperties.Formats("dd/mm/yyyy", "dd/mm/yyyy hh:mm", "#,##0"));
                    assertThat(properties.preset()).isEqualTo(TablePreset.DARK);
                    assertThat(properties.accentColor()).isEqualTo("DARK_BLUE");
                    assertThat(properties.validation().packages()).isEmpty();

                    Row row = firstDataRow(context.getBean(Sheetsmith.class), "Dated", Fixtures.Dated.class,
                            new Fixtures.Dated(LocalDate.of(2026, 9, 25), 3));
                    assertThat(row.getCell(0).getCellStyle().getDataFormatString()).isEqualTo("dd/mm/yyyy");
                    assertThat(row.getCell(1).getCellStyle().getDataFormatString()).isEqualTo("#,##0");
                });
    }

    @Test
    void defaultPropertiesMatchTheStandardDefaults() {
        runner.run(context -> assertThat(context.getBean(SheetsmithProperties.class).toDefaults())
                .isEqualTo(SheetsmithDefaults.standard()));
    }

    @Test
    void invalidPropertiesFailAtStartup() {
        runner.withPropertyValues("sheetsmith.preset=INHERIT").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).rootCause().hasMessageContaining("INHERIT");
        });
        runner.withPropertyValues("sheetsmith.accent-color=blue").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).rootCause().hasMessageContaining("'blue'");
        });
    }

    @Test
    void configurationMetadataIsGenerated() throws IOException {
        String metadata;
        try (InputStream in = new ClassPathResource("META-INF/spring-configuration-metadata.json").getInputStream()) {
            metadata = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertThat(metadata).contains(
                "\"sheetsmith.formats.date\"", "\"sheetsmith.formats.date-time\"", "\"sheetsmith.formats.number\"",
                "\"sheetsmith.preset\"", "\"sheetsmith.accent-color\"", "\"sheetsmith.validation.packages\"",
                "\"defaultValue\": \"yyyy-mm-dd\"", "\"defaultValue\": \"#4472C4\"");
    }

    @Test
    void invalidAnnotatedClassesStopTheApplicationAtStartup() {
        runner.withPropertyValues("sheetsmith.validation.packages=" + SCAN + ".valid," + SCAN + ".invalid")
                .run(context -> {
                    assertThat(context).hasFailed();
                    SheetsmithConfigurationException exception =
                            cause(context.getStartupFailure(), SheetsmithConfigurationException.class);
                    assertThat(exception.errors())
                            .extracting(ConfigurationError::code, ConfigurationError::type)
                            .containsExactly(
                                    tuple("V-04", BlankHeaderSheet.class),
                                    tuple("V-10", UnconvertibleSheet.class));
                });
    }

    @Test
    void validAnnotatedClassesPassTheStartupValidation() {
        runner.withPropertyValues("sheetsmith.validation.packages[0]=" + SCAN + ".valid").run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(SheetsmithStartupValidator.class);
        });
    }

    @Test
    void startupValidationUsesTheConvertersOfTheContext() {
        runner.withUserConfiguration(ConverterBeans.class)
                .withPropertyValues("sheetsmith.validation.packages=" + SCAN + ".invalid")
                .run(context -> {
                    SheetsmithConfigurationException exception =
                            cause(context.getStartupFailure(), SheetsmithConfigurationException.class);
                    assertThat(exception.errors()).extracting(ConfigurationError::code).containsExactly("V-04");
                });
    }

    private static Row firstDataRow(Sheetsmith sheetsmith, String name, Class<?> type, Object row) {
        byte[] bytes = sheetsmith.generate(List.of(data(name, type, row)));
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            return workbook.getSheet(name).getRow(1);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> SheetData<T> data(String name, Class<T> type, Object row) {
        return SheetData.of(name, type, List.of((T) row));
    }

    private static <T extends Throwable> T cause(Throwable failure, Class<T> type) {
        for (Throwable t = failure; t != null; t = t.getCause()) {
            if (type.isInstance(t)) {
                return type.cast(t);
            }
        }
        throw new AssertionError("no " + type.getSimpleName() + " in " + failure);
    }

    @Configuration(proxyBeanMethods = false)
    static class ConverterBeans {

        @Bean
        Fixtures.MoneyConverter moneyConverter() {
            return new Fixtures.MoneyConverter();
        }

        @Bean
        CellConverter<Date> dateConverter() {
            return (value, context) -> CellValue.text("epoch " + value.getTime());
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class DuplicateConverterBeans {

        @Bean
        CellConverter<Date> firstDateConverter() {
            return (value, context) -> CellValue.blank();
        }

        @Bean
        CellConverter<Date> secondDateConverter() {
            return (value, context) -> CellValue.blank();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class RawConverterBean {

        @Bean
        @SuppressWarnings("rawtypes")
        CellConverter rawConverter() {
            return (value, context) -> CellValue.blank();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class FieldConverterBean {

        @Bean
        Fixtures.FieldMoneyConverter fieldMoneyConverter() {
            return new Fixtures.FieldMoneyConverter(new Fixtures.CurrencyFormatter("bean:"));
        }
    }
}
