package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.fixtures.ValidSheets;
import cloud.baldilorenzo.sheetsmith.internal.style.StyleAttributes;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataExtractorTest {

    private final MetadataExtractor extractor = new MetadataExtractor();

    @Test
    void onlyAnnotatedFieldsBecomeColumnsSortedByOrder() {
        SheetMetadata metadata = extractor.extract(ValidSheets.Person.class);

        assertThat(metadata.columns()).extracting(ColumnMetadata::fieldName).containsExactly("name", "age");
        assertThat(metadata.columns()).extracting(ColumnMetadata::header).containsExactly("Name", "Age");
        assertThat(metadata.columns()).extracting(ColumnMetadata::valueType)
                .containsExactly(String.class, int.class);
    }

    @Test
    void classValuesAreReadThroughPublicGetters() {
        SheetMetadata metadata = extractor.extract(ValidSheets.Person.class);
        ValidSheets.Person person = new ValidSheets.Person("ada", 36);

        assertThat(metadata.columns().get(0).accessor().get(person)).isEqualTo("ADA");
        assertThat(metadata.columns().get(0).accessor()).hasToString("method getName()");
        assertThat(metadata.columns().get(1).accessor().get(person)).isEqualTo(36);
    }

    @Test
    void inheritedFieldsComeFromTheTopmostSuperclassFirstAndAreReadFromTheField() {
        SheetMetadata metadata = extractor.extract(ValidSheets.Employee.class);
        ValidSheets.Employee employee = new ValidSheets.Employee(7L, "admin");

        assertThat(metadata.columns()).extracting(ColumnMetadata::fieldName).containsExactly("id", "role");
        assertThat(metadata.columns().get(0).accessor().get(employee)).isEqualTo(7L);
        assertThat(metadata.columns().get(0).accessor()).hasToString("field id");
        assertThat(metadata.columns().get(1).accessor().get(employee)).isEqualTo("admin");
    }

    @Test
    void recordComponentsAreReadThroughTheirAccessors() {
        SheetMetadata metadata = extractor.extract(ValidSheets.Product.class);
        ValidSheets.Product product = new ValidSheets.Product(new BigDecimal("9.90"), "P-1", "ignored");

        assertThat(metadata.columns()).extracting(ColumnMetadata::fieldName).containsExactly("code", "price");
        assertThat(metadata.columns().get(0).accessor()).hasToString("method code()");
        assertThat(metadata.columns().get(0).accessor().get(product)).isEqualTo("P-1");
        assertThat(metadata.columns().get(1).accessor().get(product)).isEqualTo(new BigDecimal("9.90"));
        assertThat(metadata.columns().get(0).width()).isEqualTo(12);
        assertThat(metadata.columns().get(1).format()).isEqualTo("#,##0.00");
    }

    @Test
    void booleanGettersUseIsPrefixAndGettersWithIncompatibleTypesAreIgnored() {
        SheetMetadata metadata = extractor.extract(ValidSheets.Flags.class);
        ValidSheets.Flags flags = new ValidSheets.Flags(true, Boolean.FALSE, 3);

        assertThat(metadata.columns()).extracting(column -> column.accessor().toString())
                .containsExactly("method isActive()", "method getVerified()", "field counter");
        assertThat(metadata.columns()).extracting(column -> column.accessor().get(flags))
                .containsExactly(true, false, 3);
    }

    @Test
    void publicGetterOfANonPublicClassIsAccessible() {
        SheetMetadata metadata = extractor.extract(ValidSheets.packagePrivateType());

        assertThat(metadata.columns().get(0).accessor().get(ValidSheets.packagePrivate("v"))).isEqualTo("v");
    }

    @Test
    void defaultsProduceNoTitleNoStylesAndDefaultOptions() {
        SheetMetadata metadata = extractor.extract(ValidSheets.Person.class);

        assertThat(metadata.type()).isEqualTo(ValidSheets.Person.class);
        assertThat(metadata.title()).isNull();
        assertThat(metadata.titleStyle()).isEqualTo(StyleAttributes.EMPTY);
        assertThat(metadata.options()).isEqualTo(new SheetMetadata.Options(true, false, true));
        assertThat(metadata.preset()).isEqualTo(TablePreset.INHERIT);
        assertThat(metadata.accentColor()).isNull();
        assertThat(metadata.outerBorder()).isNull();
        assertThat(metadata.outerBorderColor()).isNull();
        assertThat(metadata.header()).isEqualTo(SheetMetadata.HeaderSlots.EMPTY);
        assertThat(metadata.body()).isEqualTo(SheetMetadata.BodySlots.EMPTY);
        ColumnMetadata column = metadata.columns().get(0);
        assertThat(column.width()).isNull();
        assertThat(column.format()).isNull();
        assertThat(column.converterClass()).isNull();
        assertThat(column.headerStyle()).isEqualTo(StyleAttributes.EMPTY);
        assertThat(column.styles()).isEqualTo(ColumnMetadata.Slots.EMPTY);
    }

    @Test
    void sheetOptionsAndSlotsAreResolved() {
        SheetMetadata metadata = extractor.extract(ValidSheets.Invoice.class);

        assertThat(metadata.title()).isEqualTo("Invoices");
        assertThat(metadata.titleStyle()).isEqualTo(StyleAttributes.builder().fontSize(16).build());
        assertThat(metadata.options()).isEqualTo(new SheetMetadata.Options(false, true, false));
        assertThat(metadata.preset()).isEqualTo(TablePreset.MEDIUM);
        assertThat(metadata.accentColor()).isEqualTo("#1F4E79");
        assertThat(metadata.outerBorder()).isEqualTo(Border.MEDIUM);
        assertThat(metadata.outerBorderColor()).isEqualTo("DARK_BLUE");

        StyleAttributes left = StyleAttributes.builder().borderLeft(Border.THIN).build();
        StyleAttributes right = StyleAttributes.builder().borderRight(Border.THIN).build();
        StyleAttributes zebra = StyleAttributes.builder().fillColor("#F2F2F2").build();
        assertThat(metadata.header()).isEqualTo(new SheetMetadata.HeaderSlots(
                StyleAttributes.builder().bold(true).build(), left, right));
        assertThat(metadata.body()).isEqualTo(new SheetMetadata.BodySlots(
                StyleAttributes.builder().fontName("Arial").build(),
                zebra,
                StyleAttributes.builder().italic(true).build(),
                StyleAttributes.builder().bold(false).build(),
                StyleAttributes.builder().borderBottom(Border.DOUBLE).build(),
                left,
                right));

        ColumnMetadata customer = metadata.columns().get(0);
        assertThat(customer.converterClass()).isEqualTo(ValidSheets.UpperCaseConverter.class);
        assertThat(customer.headerStyle()).isEqualTo(left);
        assertThat(customer.styles().base()).isEqualTo(metadata.body().base());
        assertThat(customer.styles().even()).isEqualTo(zebra);
        assertThat(customer.styles().lastRow()).isEqualTo(metadata.body().lastRow());
    }

    @Test
    void styleDeclaredOnTheClassWinsOverStyleSheet() {
        SheetMetadata metadata = extractor.extract(ValidSheets.Invoice.class);

        assertThat(metadata.columns().get(1).styles().base())
                .isEqualTo(StyleAttributes.builder().dataFormat("0.00").fontColor("#00AA00").build());
    }
}
