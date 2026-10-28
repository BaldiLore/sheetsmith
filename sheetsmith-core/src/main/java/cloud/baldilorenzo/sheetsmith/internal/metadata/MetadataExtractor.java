package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.annotation.BodyStyles;
import cloud.baldilorenzo.sheetsmith.annotation.ColumnStyles;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheetTemplate;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelStyleSheet;
import cloud.baldilorenzo.sheetsmith.annotation.HeaderStyles;
import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.internal.metadata.MetadataValidator.Rules;
import cloud.baldilorenzo.sheetsmith.internal.style.ColorUtils;
import cloud.baldilorenzo.sheetsmith.internal.style.StyleAttributes;
import cloud.baldilorenzo.sheetsmith.style.Border;
import cloud.baldilorenzo.sheetsmith.style.TablePreset;
import cloud.baldilorenzo.sheetsmith.style.Toggle;

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
 * The configuration of the sheet template referenced by the class, if any, is merged here: the metadata holds the
 * effective configuration, the class first, then the template, and the template is also checked on its own. Every
 * error found is collected and reported together. Stateless and thread-safe.
 */
public final class MetadataExtractor {

    private static final String SHEET = "@ExcelSheet";
    private static final String TEMPLATE = "@ExcelSheetTemplate";

    private static final ExcelSheetTemplate EMPTY_TEMPLATE =
            EmptyTemplate.class.getAnnotation(ExcelSheetTemplate.class);

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
            String message = type.isAnnotationPresent(ExcelSheetTemplate.class)
                    ? "the class is annotated with @ExcelSheetTemplate, not with @ExcelSheet: a template cannot be "
                    + "exported, reference it from a sheet class with @ExcelSheet(template = ...)"
                    : "the class is not annotated with @ExcelSheet";
            validator.error(Rules.MISSING_SHEET, type, "", message);
            return new Inspection(null, List.of(), validator.errors());
        }
        if (type.isAnnotationPresent(ExcelSheetTemplate.class)) {
            validator.error(Rules.TEMPLATE_ON_OTHER_CLASS, type, TEMPLATE, "a template cannot also be a sheet "
                    + "class: move the sheet configuration to a separate class with @ExcelSheet(template = ...)");
        }

        StyleSheets styleSheets = new StyleSheets(validator);
        Template template = template(type, sheet, styleSheets, validator);
        Map<String, StyleDefinition> styles = new HashMap<>(template.styles());
        styles.putAll(styleSheets.registry(type, SHEET, sheet.styleSheets()));
        for (StyleDefinition definition : declaredStyles(type, validator)) {
            styles.put(definition.name(), definition);
        }
        StyleResolver resolver = new StyleResolver(type, styles, validator);
        List<ColumnMetadata> columns = columns(type, resolver, validator);

        ExcelSheetTemplate defaults = template.annotation();
        String title = sheet.title().isEmpty() ? null : sheet.title();
        StyleAttributes titleStyle = StyleAttributes.EMPTY;
        if (title == null && !sheet.titleStyle().isEmpty()) {
            validator.error(Rules.TITLE_STYLE_WITHOUT_TITLE, type, SHEET, "titleStyle is set but title is empty");
        } else if (title != null) {
            titleStyle = resolver.resolve(SHEET, "titleStyle", sheet.titleStyle(), defaults.titleStyle());
        }
        validator.checkColor(type, SHEET, "accentColor", sheet.accentColor());
        validator.checkColor(type, SHEET, "outerBorderColor", sheet.outerBorderColor());
        SheetMetadata.HeaderSlots header = headerSlots(SHEET, sheet.header(), defaults.header(), resolver);
        SheetMetadata.BodySlots body = bodySlots(SHEET, sheet.body(), defaults.body(), resolver);

        List<ConfigurationError> errors = validator.errors();
        if (!errors.isEmpty()) {
            return new Inspection(null, columns, errors);
        }
        Border outerBorder = sheet.outerBorder() != Border.INHERIT ? sheet.outerBorder() : defaults.outerBorder();
        SheetMetadata metadata = new SheetMetadata(
                type,
                title,
                titleStyle,
                new SheetMetadata.Options(
                        toggle(sheet.freezeHeader(), defaults.freezeHeader(), true),
                        toggle(sheet.autoFilter(), defaults.autoFilter(), false),
                        toggle(sheet.autoSizeColumns(), defaults.autoSizeColumns(), true)),
                template.present() ? defaults.preset() : sheet.preset(),
                ColorUtils.normalize(emptyToNull(firstSet(sheet.accentColor(), defaults.accentColor()))),
                outerBorder == Border.INHERIT ? null : outerBorder,
                ColorUtils.normalize(emptyToNull(firstSet(sheet.outerBorderColor(), defaults.outerBorderColor()))),
                header,
                body,
                columns);
        return new Inspection(metadata, metadata.columns(), errors);
    }

    /**
     * Inspects a sheet template on its own, as it is checked for every sheet class that references it.
     * <p>
     * A class without {@link ExcelSheetTemplate} violates rule {@code V-01}.
     *
     * @param type the template class
     * @return the inspection result, with no metadata and no columns: a template is not exported
     */
    public Inspection inspectTemplate(Class<?> type) {
        Objects.requireNonNull(type, "type");
        MetadataValidator validator = new MetadataValidator();
        ExcelSheetTemplate template = type.getAnnotation(ExcelSheetTemplate.class);
        if (template == null) {
            validator.error(Rules.MISSING_SHEET, type, "",
                    "the class is not annotated with @ExcelSheet or @ExcelSheetTemplate");
        } else {
            checkTemplate(type, template, new StyleSheets(validator), validator);
        }
        return new Inspection(null, List.of(), validator.errors());
    }

    /**
     * Result of {@link #inspect(Class)} and {@link #inspectTemplate(Class)}.
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

    // ---- templates ----

    /**
     * The template referenced by a sheet class, checked on its own ({@code V-21} to {@code V-25} and the shared
     * rules), with the styles of its style sheets. Without a usable template, every attribute is unset.
     */
    private static Template template(Class<?> type, ExcelSheet sheet, StyleSheets styleSheets,
                                     MetadataValidator validator) {
        Class<?> templateType = sheet.template();
        if (templateType == ExcelSheetTemplate.None.class) {
            return Template.NONE;
        }
        if (sheet.preset() != TablePreset.INHERIT) {
            validator.error(Rules.PRESET_WITH_TEMPLATE, type, SHEET, "preset " + sheet.preset()
                    + " is set together with template " + templateType.getName()
                    + ": remove preset from the sheet class, or declare it in the template");
        }
        ExcelSheetTemplate annotation = templateType.getAnnotation(ExcelSheetTemplate.class);
        if (annotation == null) {
            validator.error(Rules.NOT_A_TEMPLATE, type, SHEET, "template: " + templateType.getName()
                    + " is not annotated with @ExcelSheetTemplate");
            return Template.NONE;
        }
        return new Template(annotation, checkTemplate(templateType, annotation, styleSheets, validator), true);
    }

    /** Checks a template on its own and returns the styles of its style sheets. */
    private static Map<String, StyleDefinition> checkTemplate(Class<?> type, ExcelSheetTemplate template,
                                                              StyleSheets styleSheets, MetadataValidator validator) {
        if (type.isAnnotationPresent(ExcelSheet.class) || type.isAnnotationPresent(ExcelStyleSheet.class)) {
            String other = type.isAnnotationPresent(ExcelSheet.class) ? "@ExcelSheet" : "@ExcelStyleSheet";
            validator.error(Rules.TEMPLATE_ON_OTHER_CLASS, type, TEMPLATE, "a template cannot also be annotated "
                    + "with " + other + ": declare the template on a separate class");
        }
        if (type.getAnnotationsByType(ExcelStyle.class).length > 0) {
            validator.error(Rules.STYLE_ON_TEMPLATE, type, TEMPLATE, "a template cannot declare @ExcelStyle: move "
                    + "the styles to a style sheet and reference it with styleSheets");
        }
        List<Field> fields = annotatedFields(type);
        if (!fields.isEmpty()) {
            validator.error(Rules.COLUMN_ON_TEMPLATE, type, TEMPLATE, "a template cannot have fields annotated "
                    + "with @ExcelColumn (found: " + fields.stream().map(Field::getName).toList()
                    + "): columns belong to the sheet classes");
        }
        validator.checkColor(type, TEMPLATE, "accentColor", template.accentColor());
        validator.checkColor(type, TEMPLATE, "outerBorderColor", template.outerBorderColor());
        Map<String, StyleDefinition> styles = styleSheets.registry(type, TEMPLATE, template.styleSheets());
        StyleResolver resolver = new StyleResolver(type, styles, validator);
        resolver.resolve(TEMPLATE, "titleStyle", template.titleStyle(), "");
        headerSlots(TEMPLATE, template.header(), EMPTY_TEMPLATE.header(), resolver);
        bodySlots(TEMPLATE, template.body(), EMPTY_TEMPLATE.body(), resolver);
        return styles;
    }

    /**
     * A template referenced by a sheet class.
     *
     * @param annotation the template attributes, all unset without a template
     * @param styles     the styles of the style sheets of the template
     * @param present    whether the sheet class references a template annotated with {@code @ExcelSheetTemplate}
     */
    private record Template(ExcelSheetTemplate annotation, Map<String, StyleDefinition> styles, boolean present) {

        static final Template NONE = new Template(EMPTY_TEMPLATE, Map.of(), false);
    }

    /** A template with every attribute unset. */
    @ExcelSheetTemplate
    private static final class EmptyTemplate {
    }

    private static boolean toggle(Toggle sheet, Toggle template, boolean fallback) {
        Toggle value = sheet != Toggle.INHERIT ? sheet : template;
        return value == Toggle.INHERIT ? fallback : value == Toggle.TRUE;
    }

    private static String firstSet(String sheet, String template) {
        return sheet.isEmpty() ? template : sheet;
    }

    // ---- styles ----

    /**
     * Builds the registry of the style sheets of one level, a sheet class or a template, reporting {@code V-08}
     * and {@code V-09}. The styles of each style sheet are read once per inspection, so that the errors of a style
     * sheet referenced by both a sheet class and its template are reported once.
     */
    private static final class StyleSheets {

        private final MetadataValidator validator;
        private final Map<Class<?>, List<StyleDefinition>> declared = new HashMap<>();

        StyleSheets(MetadataValidator validator) {
            this.validator = validator;
        }

        Map<String, StyleDefinition> registry(Class<?> type, String element, Class<?>[] styleSheets) {
            Map<String, StyleDefinition> registry = new HashMap<>();
            Map<String, Class<?>> origins = new HashMap<>();
            for (Class<?> styleSheet : new LinkedHashSet<>(Arrays.asList(styleSheets))) {
                if (!styleSheet.isAnnotationPresent(ExcelStyleSheet.class)) {
                    validator.error(Rules.NOT_A_STYLE_SHEET, type, element, "styleSheets: " + styleSheet.getName()
                            + " is not annotated with @ExcelStyleSheet");
                    continue;
                }
                for (StyleDefinition definition : declared.computeIfAbsent(styleSheet,
                        declaring -> declaredStyles(declaring, validator))) {
                    Class<?> origin = origins.putIfAbsent(definition.name(), styleSheet);
                    if (origin == null) {
                        registry.put(definition.name(), definition);
                    } else {
                        validator.error(Rules.STYLE_SHEET_CONFLICT, type, element, "styleSheets: style '"
                                + definition.name() + "' is defined by both " + origin.getName() + " and "
                                + styleSheet.getName());
                    }
                }
            }
            return registry;
        }
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

    private static SheetMetadata.HeaderSlots headerSlots(String element, HeaderStyles slots, HeaderStyles inherited,
                                                         StyleResolver resolver) {
        return new SheetMetadata.HeaderSlots(
                resolver.resolve(element, "header.base", slots.base(), inherited.base()),
                resolver.resolve(element, "header.firstColumn", slots.firstColumn(), inherited.firstColumn()),
                resolver.resolve(element, "header.lastColumn", slots.lastColumn(), inherited.lastColumn()));
    }

    private static SheetMetadata.BodySlots bodySlots(String element, BodyStyles slots, BodyStyles inherited,
                                                     StyleResolver resolver) {
        return new SheetMetadata.BodySlots(
                resolver.resolve(element, "body.base", slots.base(), inherited.base()),
                resolver.resolve(element, "body.even", slots.even(), inherited.even()),
                resolver.resolve(element, "body.odd", slots.odd(), inherited.odd()),
                resolver.resolve(element, "body.firstRow", slots.firstRow(), inherited.firstRow()),
                resolver.resolve(element, "body.lastRow", slots.lastRow(), inherited.lastRow()),
                resolver.resolve(element, "body.firstColumn", slots.firstColumn(), inherited.firstColumn()),
                resolver.resolve(element, "body.lastColumn", slots.lastColumn(), inherited.lastColumn()));
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
            return resolve(element, slot, name, "");
        }

        /**
         * Resolves the name set on the class or, when empty, the name inherited from its template. An unknown
         * inherited name is not reported: the registry of the class contains the one of the template, which
         * already reports it.
         */
        StyleAttributes resolve(String element, String slot, String name, String inherited) {
            boolean own = !name.isEmpty();
            String chosen = own ? name : inherited;
            if (chosen.isEmpty()) {
                return StyleAttributes.EMPTY;
            }
            StyleDefinition definition = styles.get(chosen);
            if (definition == null) {
                if (own) {
                    validator.error(Rules.UNKNOWN_STYLE, type, element,
                            "style '" + chosen + "' not found (referenced by " + slot + ")");
                }
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
