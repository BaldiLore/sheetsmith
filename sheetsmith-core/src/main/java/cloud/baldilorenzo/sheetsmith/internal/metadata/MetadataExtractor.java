package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.annotation.BodyStyles;
import cloud.baldilorenzo.sheetsmith.annotation.ColumnStyles;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyleSheet;
import cloud.baldilorenzo.sheetsmith.annotation.HeaderStyles;
import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.internal.metadata.MetadataValidator.Rules;
import cloud.baldilorenzo.sheetsmith.internal.style.StyleAttributes;
import cloud.baldilorenzo.sheetsmith.style.Border;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builds the validated {@link SheetMetadata} of an exported class from its annotations.
 * <p>
 * Every error found is collected and reported together. Stateless and thread-safe.
 */
public final class MetadataExtractor {

    private static final String SHEET = "@ExcelSheet";

    /**
     * Extracts the metadata of a class.
     *
     * @param type the exported class
     * @return the validated metadata
     * @throws SheetsmithConfigurationException listing every error of the class
     */
    public SheetMetadata extract(Class<?> type) {
        Inspection inspection = inspect(type);
        if (!inspection.errors().isEmpty()) {
            throw new SheetsmithConfigurationException(inspection.errors());
        }
        return inspection.metadata();
    }

    /**
     * Inspects a class without throwing: returns every error found and the columns that could be built, so that
     * further checks, such as converter binding, can run on them.
     *
     * @param type the exported class
     * @return the inspection result
     */
    public Inspection inspect(Class<?> type) {
        Objects.requireNonNull(type, "type");
        MetadataValidator validator = new MetadataValidator();

        ExcelSheet sheet = type.getAnnotation(ExcelSheet.class);
        if (sheet == null) {
            validator.error(Rules.MISSING_SHEET, type, "", "the class is not annotated with @ExcelSheet");
            return new Inspection(null, List.of(), validator.errors());
        }

        Map<String, StyleDefinition> styles = styleRegistry(type, sheet, validator);
        StyleResolver resolver = new StyleResolver(type, styles, validator);
        List<ColumnMetadata> columns = columns(type, resolver, validator);

        String title = sheet.title().isEmpty() ? null : sheet.title();
        StyleAttributes titleStyle = StyleAttributes.EMPTY;
        if (title == null && !sheet.titleStyle().isEmpty()) {
            validator.error(Rules.TITLE_STYLE_WITHOUT_TITLE, type, SHEET, "titleStyle is set but title is empty");
        } else {
            titleStyle = resolver.resolve(SHEET, "titleStyle", sheet.titleStyle());
        }
        validator.checkColor(type, SHEET, "accentColor", sheet.accentColor());
        validator.checkColor(type, SHEET, "outerBorderColor", sheet.outerBorderColor());
        SheetMetadata.HeaderSlots header = headerSlots(sheet.header(), resolver);
        SheetMetadata.BodySlots body = bodySlots(sheet.body(), resolver);

        List<ConfigurationError> errors = validator.errors();
        if (!errors.isEmpty()) {
            return new Inspection(null, columns, errors);
        }
        SheetMetadata metadata = new SheetMetadata(
                type,
                title,
                titleStyle,
                new SheetMetadata.Options(sheet.freezeHeader(), sheet.autoFilter(), sheet.autoSizeColumns()),
                sheet.preset(),
                emptyToNull(sheet.accentColor()),
                sheet.outerBorder() == Border.INHERIT ? null : sheet.outerBorder(),
                emptyToNull(sheet.outerBorderColor()),
                header,
                body,
                columns);
        return new Inspection(metadata, metadata.columns(), errors);
    }

    /**
     * Result of {@link #inspect(Class)}.
     *
     * @param metadata the metadata, or null when there are errors
     * @param columns  the columns that could be built, sorted by order, even when there are errors
     * @param errors   every error found
     */
    public record Inspection(SheetMetadata metadata, List<ColumnMetadata> columns, List<ConfigurationError> errors) {

        /**
         * Creates an inspection result.
         *
         * @param metadata the metadata, or null
         * @param columns  the columns, not null
         * @param errors   the errors, not null
         */
        public Inspection {
            columns = List.copyOf(columns);
            errors = List.copyOf(errors);
        }
    }

    // ---- styles ----

    private static Map<String, StyleDefinition> styleRegistry(Class<?> type, ExcelSheet sheet,
                                                              MetadataValidator validator) {
        Map<String, StyleDefinition> registry = new HashMap<>();
        Map<String, Class<?>> origins = new HashMap<>();
        for (Class<?> styleSheet : new LinkedHashSet<>(Arrays.asList(sheet.styleSheets()))) {
            if (!styleSheet.isAnnotationPresent(ExcelStyleSheet.class)) {
                validator.error(Rules.NOT_A_STYLE_SHEET, type, SHEET, "styleSheets: " + styleSheet.getName()
                        + " is not annotated with @ExcelStyleSheet");
                continue;
            }
            for (StyleDefinition definition : declaredStyles(styleSheet, validator)) {
                Class<?> origin = origins.putIfAbsent(definition.name(), styleSheet);
                if (origin == null) {
                    registry.put(definition.name(), definition);
                } else {
                    validator.error(Rules.STYLE_SHEET_CONFLICT, type, SHEET, "styleSheets: style '"
                            + definition.name() + "' is defined by both " + origin.getName() + " and "
                            + styleSheet.getName());
                }
            }
        }
        for (StyleDefinition definition : declaredStyles(type, validator)) {
            registry.put(definition.name(), definition);
        }
        return registry;
    }

    private static List<StyleDefinition> declaredStyles(Class<?> declaring, MetadataValidator validator) {
        Map<String, StyleDefinition> styles = new LinkedHashMap<>();
        ExcelStyle[] declarations = declaring.getAnnotationsByType(ExcelStyle.class);
        for (int i = 0; i < declarations.length; i++) {
            ExcelStyle style = declarations[i];
            if (style.name().isBlank()) {
                validator.error(Rules.BLANK_STYLE_NAME, declaring, "@ExcelStyle(#" + (i + 1) + ")",
                        "style name is blank");
                continue;
            }
            String element = "@ExcelStyle(" + style.name() + ")";
            validator.checkStyle(declaring, element, style);
            if (styles.putIfAbsent(style.name(), StyleDefinition.from(style)) != null) {
                validator.error(Rules.DUPLICATE_STYLE, declaring, element,
                        "style '" + style.name() + "' is declared more than once");
            }
        }
        return List.copyOf(styles.values());
    }

    private static SheetMetadata.HeaderSlots headerSlots(HeaderStyles slots, StyleResolver resolver) {
        return new SheetMetadata.HeaderSlots(
                resolver.resolve(SHEET, "header.base", slots.base()),
                resolver.resolve(SHEET, "header.firstColumn", slots.firstColumn()),
                resolver.resolve(SHEET, "header.lastColumn", slots.lastColumn()));
    }

    private static SheetMetadata.BodySlots bodySlots(BodyStyles slots, StyleResolver resolver) {
        return new SheetMetadata.BodySlots(
                resolver.resolve(SHEET, "body.base", slots.base()),
                resolver.resolve(SHEET, "body.even", slots.even()),
                resolver.resolve(SHEET, "body.odd", slots.odd()),
                resolver.resolve(SHEET, "body.firstRow", slots.firstRow()),
                resolver.resolve(SHEET, "body.lastRow", slots.lastRow()),
                resolver.resolve(SHEET, "body.firstColumn", slots.firstColumn()),
                resolver.resolve(SHEET, "body.lastColumn", slots.lastColumn()));
    }

    private static ColumnMetadata.Slots columnSlots(String field, ColumnStyles slots, StyleResolver resolver) {
        return new ColumnMetadata.Slots(
                resolver.resolve(field, "styles.base", slots.base()),
                resolver.resolve(field, "styles.even", slots.even()),
                resolver.resolve(field, "styles.odd", slots.odd()),
                resolver.resolve(field, "styles.firstRow", slots.firstRow()),
                resolver.resolve(field, "styles.lastRow", slots.lastRow()));
    }

    /** Resolves style names against the registry of one class, reporting unknown names ({@code V-06}). */
    private static final class StyleResolver {

        private final Class<?> type;
        private final Map<String, StyleDefinition> styles;
        private final MetadataValidator validator;

        StyleResolver(Class<?> type, Map<String, StyleDefinition> styles, MetadataValidator validator) {
            this.type = type;
            this.styles = styles;
            this.validator = validator;
        }

        StyleAttributes resolve(String element, String slot, String name) {
            if (name.isEmpty()) {
                return StyleAttributes.EMPTY;
            }
            StyleDefinition definition = styles.get(name);
            if (definition == null) {
                validator.error(Rules.UNKNOWN_STYLE, type, element,
                        "style '" + name + "' not found (referenced by " + slot + ")");
                return StyleAttributes.EMPTY;
            }
            return definition.attributes();
        }
    }

    // ---- columns ----

    private static List<ColumnMetadata> columns(Class<?> type, StyleResolver resolver, MetadataValidator validator) {
        List<Field> fields = annotatedFields(type);
        if (fields.isEmpty()) {
            validator.error(Rules.NO_COLUMNS, type, "", "no field is annotated with @ExcelColumn");
        }
        Map<Integer, String> orders = new HashMap<>();
        List<ColumnMetadata> columns = new ArrayList<>();
        for (Field field : fields) {
            String name = field.getName();
            ExcelColumn column = field.getAnnotation(ExcelColumn.class);
            if (Modifier.isStatic(field.getModifiers())) {
                validator.error(Rules.STATIC_FIELD, type, name, "@ExcelColumn is not allowed on a static field");
                continue;
            }
            if (column.header().isBlank()) {
                validator.error(Rules.BLANK_HEADER, type, name, "header is blank");
            }
            String previous = orders.putIfAbsent(column.order(), name);
            if (previous != null) {
                validator.error(Rules.DUPLICATE_ORDER, type, name,
                        "order " + column.order() + " is also used by field " + previous);
            }
            validator.checkRange(type, name, "width", column.width(), 1, 255);
            StyleAttributes headerStyle = resolver.resolve(name, "headerStyle", column.headerStyle());
            ColumnMetadata.Slots slots = columnSlots(name, column.styles(), resolver);
            ValueAccessor accessor = accessor(type, field, validator);
            if (accessor != null) {
                columns.add(new ColumnMetadata(
                        column.header(),
                        column.order(),
                        name,
                        field.getType(),
                        accessor,
                        column.width() == ExcelStyle.UNSET ? null : column.width(),
                        emptyToNull(column.format()),
                        column.converter() == CellConverter.None.class ? null : column.converter(),
                        headerStyle,
                        slots));
            }
        }
        columns.sort(Comparator.comparingInt(ColumnMetadata::order));
        return columns;
    }

    /** Fields annotated with {@code @ExcelColumn}, from the topmost superclass below {@code Object} down. */
    private static List<Field> annotatedFields(Class<?> type) {
        Deque<Class<?>> hierarchy = new ArrayDeque<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            hierarchy.push(c);
        }
        List<Field> fields = new ArrayList<>();
        for (Class<?> c : hierarchy) {
            for (Field field : c.getDeclaredFields()) {
                if (!field.isSynthetic() && field.isAnnotationPresent(ExcelColumn.class)) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    // ---- value access ----

    private static ValueAccessor accessor(Class<?> type, Field field, MetadataValidator validator) {
        try {
            Method method = type.isRecord() ? recordAccessor(type, field) : getter(type, field);
            if (method != null) {
                return new ValueAccessor(unreflect(method), "method " + method.getName() + "()");
            }
            MethodHandle getter = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup())
                    .unreflectGetter(field);
            return new ValueAccessor(getter, "field " + field.getName());
        } catch (IllegalAccessException e) {
            String packageName = field.getDeclaringClass().getPackageName();
            validator.error(Rules.INACCESSIBLE_VALUE, type, field.getName(), "value is not accessible ("
                    + e.getMessage() + "); add a public getter or open package " + packageName
                    + " to sheetsmith, for example with 'opens " + packageName + ";' in module-info.java");
            return null;
        }
    }

    private static Method recordAccessor(Class<?> type, Field field) {
        for (RecordComponent component : type.getRecordComponents()) {
            if (component.getName().equals(field.getName())) {
                return component.getAccessor();
            }
        }
        return null;
    }

    private static Method getter(Class<?> type, Field field) {
        String suffix = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
        List<String> names = field.getType() == boolean.class || field.getType() == Boolean.class
                ? List.of("is" + suffix, "get" + suffix)
                : List.of("get" + suffix);
        for (String name : names) {
            try {
                Method method = type.getMethod(name);
                if (!Modifier.isStatic(method.getModifiers())
                        && field.getType().isAssignableFrom(method.getReturnType())) {
                    return method;
                }
            } catch (NoSuchMethodException e) {
                // no getter with this name: try the next one, then the field
            }
        }
        return null;
    }

    private static MethodHandle unreflect(Method method) throws IllegalAccessException {
        try {
            return MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException e) {
            return MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup())
                    .unreflect(method);
        }
    }

    private static String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }
}
