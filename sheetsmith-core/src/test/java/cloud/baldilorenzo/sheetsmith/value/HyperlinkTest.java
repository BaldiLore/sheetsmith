package cloud.baldilorenzo.sheetsmith.value;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class HyperlinkTest {

    @ParameterizedTest
    @ValueSource(strings = {"http://example.com", "https://example.com/docs?page=2", "mailto:info@example.com",
            "HTTPS://example.com", "Http://example.com", "MAILTO:info@example.com"})
    void allowedSchemesAreAcceptedInAnyCase(String address) {
        URI uri = URI.create(address);

        assertThat(Hyperlink.of(uri).address()).isSameAs(uri);
        assertThat(Hyperlink.of(uri, "Open").address()).isSameAs(uri);
        assertThat(new Hyperlink(uri, "Open").address()).isSameAs(uri);
        assertThat(Hyperlink.of(address).address()).isEqualTo(uri);
        assertThat(Hyperlink.of(address, "Open").address()).isEqualTo(uri);
    }

    @Test
    void addressIsNotNormalised() {
        assertThat(Hyperlink.of("HTTPS://example.com/A/../b").address().toString())
                .isEqualTo("HTTPS://example.com/A/../b");
    }

    @Test
    void textIsTheDescriptionWhenPresent() {
        assertThat(Hyperlink.of("https://example.com", "Example").text()).isEqualTo("Example");
    }

    @Test
    void textIsTheAddressWithoutDescription() {
        assertThat(Hyperlink.of("https://example.com/docs").text()).isEqualTo("https://example.com/docs");
        assertThat(Hyperlink.of("mailto:info@example.com", null).text()).isEqualTo("mailto:info@example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t\n"})
    void blankDescriptionIsNormalisedToNull(String description) {
        Hyperlink link = Hyperlink.of("https://example.com", description);

        assertThat(link.description()).isNull();
        assertThat(link.text()).isEqualTo("https://example.com");
    }

    @Test
    void nullDescriptionStaysNull() {
        assertThat(new Hyperlink(URI.create("https://example.com"), null).description()).isNull();
    }

    @Test
    void descriptionWithLeadingOrTrailingSpacesIsKeptAsItIs() {
        assertThat(Hyperlink.of("https://example.com", "  Example ").description()).isEqualTo("  Example ");
    }

    @Test
    void relativeAddressIsRejected() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Hyperlink.of(URI.create("/docs")))
                .withMessageContaining("/docs")
                .withMessageContaining("not absolute");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ftp://example.com/file.txt", "file:///tmp/report.xlsx", "javascript:alert(1)"})
    void otherSchemesAreRejected(String address) {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Hyperlink.of(address))
                .withMessageContaining(address)
                .withMessageContaining("http, https or mailto");
    }

    @ParameterizedTest
    @ValueSource(strings = {"https:example.com", "http:///path"})
    void webAddressWithoutHostIsRejected(String address) {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Hyperlink.of(URI.create(address), "Example"))
                .withMessageContaining(address)
                .withMessageContaining("no host");
    }

    /** {@code mailto:} is not even a valid URI in Java: it is rejected while parsing the address. */
    @Test
    void mailtoWithoutRecipientIsRejected() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Hyperlink.of("mailto:"))
                .withMessageContaining("mailto:");
    }

    @Test
    void syntacticallyInvalidAddressIsRejectedWithTheCause() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Hyperlink.of("https://exa mple.com", "Example"))
                .withMessageContaining("https://exa mple.com")
                .withMessageStartingWith("invalid hyperlink address")
                .withCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullAddressIsRejected() {
        assertThatNullPointerException().isThrownBy(() -> new Hyperlink(null, "Example")).withMessage("address");
        assertThatNullPointerException().isThrownBy(() -> Hyperlink.of((URI) null)).withMessage("address");
        assertThatNullPointerException().isThrownBy(() -> Hyperlink.of((String) null)).withMessage("address");
        assertThatNullPointerException().isThrownBy(() -> Hyperlink.of((String) null, "Example"))
                .withMessage("address");
    }

    @Test
    void linksWithEmptyAndNullDescriptionAreEqual() {
        Hyperlink empty = Hyperlink.of("https://example.com", "");
        Hyperlink none = Hyperlink.of("https://example.com");

        assertThat(empty).isEqualTo(none).hasSameHashCodeAs(none);
        assertThat(Hyperlink.of("https://example.com", "A")).isNotEqualTo(none);
    }
}
