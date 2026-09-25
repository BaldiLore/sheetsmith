package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Collects configuration errors during extraction and provides the checks shared by several annotations.
 * <p>
 * An instance is used for a single extraction and is not thread-safe.
 */
public final class MetadataValidator {

    private static final Pattern HEX_COLOR = Pattern.compile("#[0-9A-Fa-f]{6}");

    private static final Set<String> INDEXED_COLORS = Arrays.stream(IndexedColors.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    private final List<ConfigurationError> errors = new ArrayList<>();

    /**
     * Returns whether a colour follows the colour syntax: {@code #RRGGBB}, case-insensitive, or the name of an
     * {@link IndexedColors} constant.
     *
     * @param value the colour, not empty
     * @return true if the colour is valid
     */
    public static boolean isValidColor(String value) {
        return HEX_COLOR.matcher(value).matches() || INDEXED_COLORS.contains(value);
    }

    /**
     * Records an error.
     *
     * @param code    the rule id
     * @param type    the class the error refers to
     * @param element the field, style or attribute involved, or empty
     * @param message the description
     */
    public void error(String code, Class<?> type, String element, String message) {
        errors.add(new ConfigurationError(code, type, element, message));
    }

    /**
     * Checks a colour attribute ({@code V-13}). An empty value is unset and always valid.
     *
     * @param type      the class declaring the attribute
     * @param element   the owner of the attribute
     * @param attribute the attribute name
     * @param value     the colour
     */
    public void checkColor(Class<?> type, String element, String attribute, String value) {
        if (!value.isEmpty() && !isValidColor(value)) {
            error(Rules.COLOR, type, element, attribute + " '" + value
                    + "' is not a valid colour: expected #RRGGBB or the name of an IndexedColors constant");
        }
    }

    /**
     * Checks a numeric attribute range ({@code V-14}). {@link ExcelStyle#UNSET} is always valid.
     *
     * @param type      the class declaring the attribute
     * @param element   the owner of the attribute
     * @param attribute the attribute name
     * @param value     the value
     * @param min       the minimum, inclusive
     * @param max       the maximum, inclusive
     */
    public void checkRange(Class<?> type, String element, String attribute, int value, int min, int max) {
        if (value != ExcelStyle.UNSET && (value < min || value > max)) {
            error(Rules.RANGE, type, element,
                    attribute + " " + value + " is out of range " + min + " to " + max);
        }
    }

    /**
     * Checks the colours ({@code V-13}) and numeric ranges ({@code V-14}) of a style declaration.
     *
     * @param type    the class declaring the style
     * @param element the name of the style declaration
     * @param style   the style declaration
     */
    public void checkStyle(Class<?> type, String element, ExcelStyle style) {
        int rotation = style.rotation();
        if (rotation != ExcelStyle.UNSET && rotation != 255 && (rotation < -90 || rotation > 90)) {
            error(Rules.RANGE, type, element, "rotation " + rotation + " is out of range -90 to 90, or 255");
        }
        checkRange(type, element, "indent", style.indent(), 0, 250);
        checkRange(type, element, "fontSize", style.fontSize(), 1, 409);
        checkColor(type, element, "borderColor", style.borderColor());
        checkColor(type, element, "borderTopColor", style.borderTopColor());
        checkColor(type, element, "borderBottomColor", style.borderBottomColor());
        checkColor(type, element, "borderLeftColor", style.borderLeftColor());
        checkColor(type, element, "borderRightColor", style.borderRightColor());
        checkColor(type, element, "fillColor", style.fillColor());
        checkColor(type, element, "fillBackgroundColor", style.fillBackgroundColor());
        checkColor(type, element, "fontColor", style.fontColor());
    }

    /**
     * Throws if any error was recorded.
     *
     * @throws SheetsmithConfigurationException listing every recorded error
     */
    public void throwIfInvalid() {
        if (!errors.isEmpty()) {
            throw new SheetsmithConfigurationException(errors);
        }
    }

    /** Ids of the validation rules checked during extraction. */
    static final class Rules {
        static final String MISSING_SHEET = "V-01";
        static final String NO_COLUMNS = "V-02";
        static final String DUPLICATE_ORDER = "V-03";
        static final String BLANK_HEADER = "V-04";
        static final String STATIC_FIELD = "V-05";
        static final String UNKNOWN_STYLE = "V-06";
        static final String DUPLICATE_STYLE = "V-07";
        static final String STYLE_SHEET_CONFLICT = "V-08";
        static final String NOT_A_STYLE_SHEET = "V-09";
        static final String COLOR = "V-13";
        static final String RANGE = "V-14";
        static final String TITLE_STYLE_WITHOUT_TITLE = "V-15";
        static final String BLANK_STYLE_NAME = "V-16";
        static final String INACCESSIBLE_VALUE = "V-17";

        private Rules() {
        }
    }
}
