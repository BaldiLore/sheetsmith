package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.style.Align;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.Fill;
import cloud.baldilorenzo.sheetsmith.style.Script;
import cloud.baldilorenzo.sheetsmith.style.Underline;
import cloud.baldilorenzo.sheetsmith.style.VerticalAlign;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.util.Objects;

/**
 * Converts library style enums to their Apache POI counterparts.
 * <p>
 * Mirrored enums are converted by constant name. {@code INHERIT} has no POI counterpart: converting it throws
 * {@link IllegalArgumentException}.
 */
public final class PoiMapping {

    private PoiMapping() {
    }

    /**
     * Converts a horizontal alignment.
     *
     * @param align the alignment, not {@code INHERIT}
     * @return the POI alignment
     */
    public static HorizontalAlignment horizontalAlignment(Align align) {
        return byName(align, Align.INHERIT, HorizontalAlignment.class);
    }

    /**
     * Converts a vertical alignment.
     *
     * @param align the alignment, not {@code INHERIT}
     * @return the POI alignment
     */
    public static VerticalAlignment verticalAlignment(VerticalAlign align) {
        return byName(align, VerticalAlign.INHERIT, VerticalAlignment.class);
    }

    /**
     * Converts a border line.
     *
     * @param border the border line, not {@code INHERIT}
     * @return the POI border style
     */
    public static BorderStyle borderStyle(Border border) {
        return byName(border, Border.INHERIT, BorderStyle.class);
    }

    /**
     * Converts a fill pattern.
     *
     * @param fill the fill pattern, not {@code INHERIT}
     * @return the POI fill pattern
     */
    public static FillPatternType fillPattern(Fill fill) {
        return byName(fill, Fill.INHERIT, FillPatternType.class);
    }

    /**
     * Converts an underline style.
     *
     * @param underline the underline, not {@code INHERIT}
     * @return the POI underline
     */
    public static FontUnderline fontUnderline(Underline underline) {
        return byName(underline, Underline.INHERIT, FontUnderline.class);
    }

    /**
     * Converts a script to the POI type offset.
     *
     * @param script the script, not {@code INHERIT}
     * @return {@link Font#SS_NONE}, {@link Font#SS_SUPER} or {@link Font#SS_SUB}
     */
    public static short typeOffset(Script script) {
        Objects.requireNonNull(script, "script");
        switch (script) {
            case NONE:
                return Font.SS_NONE;
            case SUPER:
                return Font.SS_SUPER;
            case SUB:
                return Font.SS_SUB;
            default:
                throw inherit(script);
        }
    }

    private static <L extends Enum<L>, P extends Enum<P>> P byName(L value, L inherit, Class<P> poiType) {
        Objects.requireNonNull(value, "value");
        if (value == inherit) {
            throw inherit(value);
        }
        return Enum.valueOf(poiType, value.name());
    }

    private static IllegalArgumentException inherit(Enum<?> value) {
        return new IllegalArgumentException(
                value.getDeclaringClass().getSimpleName() + ".INHERIT has no Apache POI counterpart");
    }
}
