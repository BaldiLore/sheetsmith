package cloud.baldilorenzo.sheetsmith.internal.style;

import cloud.baldilorenzo.sheetsmith.style.Fill;
import cloud.baldilorenzo.sheetsmith.style.Script;
import cloud.baldilorenzo.sheetsmith.style.Underline;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Turns effective styles into POI cell styles for one workbook, so that the number of cell styles depends on the
 * number of distinct styles, never on the number of cells.
 * <p>
 * Equal attributes give the same {@link CellStyle} instance; fonts and data formats are deduplicated the same way.
 * When a fill colour is set without a fill pattern, the pattern is {@link Fill#SOLID_FOREGROUND}. Unset attributes
 * keep the POI defaults.
 * <p>
 * Created per workbook; not thread-safe.
 */
public final class StyleCache {

    private static final IndexedColorMap COLOR_MAP = new DefaultIndexedColorMap();

    private final Workbook workbook;
    private final DataFormat dataFormat;
    private final Map<StyleAttributes, XSSFCellStyle> styles = new HashMap<>();
    private final Map<FontKey, XSSFFont> fonts = new HashMap<>();
    private final Map<String, Short> formats = new HashMap<>();

    /**
     * Creates a cache for a workbook.
     *
     * @param workbook an {@code XSSFWorkbook} or {@code SXSSFWorkbook}
     */
    public StyleCache(Workbook workbook) {
        this.workbook = Objects.requireNonNull(workbook, "workbook");
        this.dataFormat = workbook.createDataFormat();
    }

    /**
     * Returns the cell style for the given attributes, creating it on first use.
     *
     * @param attributes the effective attributes of a cell
     * @return the cell style
     */
    public CellStyle get(StyleAttributes attributes) {
        StyleAttributes key = applyFillRule(Objects.requireNonNull(attributes, "attributes"));
        XSSFCellStyle style = styles.get(key);
        if (style == null) {
            style = create(key);
            styles.put(key, style);
        }
        return style;
    }

    /**
     * Returns the number of distinct cell styles created by this cache.
     *
     * @return the number of cell styles
     */
    public int size() {
        return styles.size();
    }

    private static StyleAttributes applyFillRule(StyleAttributes attributes) {
        if (attributes.fillColor() != null && attributes.fillPattern() == null) {
            return attributes.toBuilder().fillPattern(Fill.SOLID_FOREGROUND).build();
        }
        return attributes;
    }

    private XSSFCellStyle create(StyleAttributes a) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        if (a.align() != null) {
            style.setAlignment(PoiMapping.horizontalAlignment(a.align()));
        }
        if (a.verticalAlign() != null) {
            style.setVerticalAlignment(PoiMapping.verticalAlignment(a.verticalAlign()));
        }
        if (a.wrapText() != null) {
            style.setWrapText(a.wrapText());
        }
        if (a.shrinkToFit() != null) {
            style.setShrinkToFit(a.shrinkToFit());
        }
        if (a.rotation() != null) {
            style.setRotation(a.rotation().shortValue());
        }
        if (a.indent() != null) {
            style.setIndention(a.indent().shortValue());
        }
        if (a.borderTop() != null) {
            style.setBorderTop(PoiMapping.borderStyle(a.borderTop()));
        }
        if (a.borderBottom() != null) {
            style.setBorderBottom(PoiMapping.borderStyle(a.borderBottom()));
        }
        if (a.borderLeft() != null) {
            style.setBorderLeft(PoiMapping.borderStyle(a.borderLeft()));
        }
        if (a.borderRight() != null) {
            style.setBorderRight(PoiMapping.borderStyle(a.borderRight()));
        }
        color(a.borderTopColor(), style::setTopBorderColor, style::setTopBorderColor);
        color(a.borderBottomColor(), style::setBottomBorderColor, style::setBottomBorderColor);
        color(a.borderLeftColor(), style::setLeftBorderColor, style::setLeftBorderColor);
        color(a.borderRightColor(), style::setRightBorderColor, style::setRightBorderColor);
        if (a.fillPattern() != null) {
            style.setFillPattern(PoiMapping.fillPattern(a.fillPattern()));
        }
        color(a.fillColor(), style::setFillForegroundColor, style::setFillForegroundColor);
        color(a.fillBackgroundColor(), style::setFillBackgroundColor, style::setFillBackgroundColor);
        FontKey fontKey = FontKey.of(a);
        if (!fontKey.isEmpty()) {
            style.setFont(font(fontKey));
        }
        if (a.dataFormat() != null) {
            style.setDataFormat(format(a.dataFormat()));
        }
        if (a.locked() != null) {
            style.setLocked(a.locked());
        }
        if (a.hidden() != null) {
            style.setHidden(a.hidden());
        }
        if (a.quotePrefix() != null) {
            style.setQuotePrefixed(a.quotePrefix());
        }
        return style;
    }

    private XSSFFont font(FontKey key) {
        XSSFFont font = fonts.get(key);
        if (font == null) {
            font = (XSSFFont) workbook.createFont();
            if (key.fontName() != null) {
                font.setFontName(key.fontName());
            }
            if (key.fontSize() != null) {
                font.setFontHeightInPoints(key.fontSize().shortValue());
            }
            if (key.bold() != null) {
                font.setBold(key.bold());
            }
            if (key.italic() != null) {
                font.setItalic(key.italic());
            }
            if (key.strikeout() != null) {
                font.setStrikeout(key.strikeout());
            }
            if (key.underline() != null) {
                font.setUnderline(PoiMapping.fontUnderline(key.underline()));
            }
            if (key.script() != null) {
                font.setTypeOffset(PoiMapping.typeOffset(key.script()));
            }
            color(key.fontColor(), font::setColor, font::setColor);
            fonts.put(key, font);
        }
        return font;
    }

    private short format(String format) {
        return formats.computeIfAbsent(format, dataFormat::getFormat);
    }

    /** Applies a hex colour as an {@link XSSFColor} and an indexed colour through its index. */
    private static void color(String value, Consumer<XSSFColor> rgb, Consumer<Short> indexed) {
        if (value == null) {
            return;
        }
        if (value.startsWith("#")) {
            int hex = Integer.parseInt(value.substring(1), 16);
            byte[] bytes = {(byte) (hex >> 16), (byte) (hex >> 8), (byte) hex};
            rgb.accept(new XSSFColor(bytes, COLOR_MAP));
        } else {
            indexed.accept(IndexedColors.valueOf(value).getIndex());
        }
    }

    /** The font subset of the style attributes. */
    private record FontKey(String fontName, Integer fontSize, Boolean bold, Boolean italic, Boolean strikeout,
                           Underline underline, String fontColor, Script script) {

        static FontKey of(StyleAttributes a) {
            return new FontKey(a.fontName(), a.fontSize(), a.bold(), a.italic(), a.strikeout(), a.underline(),
                    a.fontColor(), a.script());
        }

        boolean isEmpty() {
            return fontName == null && fontSize == null && bold == null && italic == null && strikeout == null
                    && underline == null && fontColor == null && script == null;
        }
    }
}
