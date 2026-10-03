package cloud.baldilorenzo.sheetsmith.internal.convert;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellConverterFactory;
import cloud.baldilorenzo.sheetsmith.internal.metadata.ColumnMetadata;
import cloud.baldilorenzo.sheetsmith.internal.metadata.SheetMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Binds a converter to each column of a sheet, checking rules {@code V-10} to {@code V-12}. Field-level converters
 * are created once per converter class. Thread-safe.
 */
public final class ConverterBinder {

    private static final String NO_CONVERTER = "V-10";
    private static final String INCOMPATIBLE_CONVERTER = "V-11";
    private static final String UNCREATABLE_CONVERTER = "V-12";

    private final ConverterRegistry registry;
    private final CellConverterFactory factory;
    private final Map<Class<?>, CellConverter<?>> fieldConverters = new ConcurrentHashMap<>();

    /**
     * Creates a binder.
     *
     * @param registry the registry of application and built-in converters
     * @param factory  creates field-level converters
     */
    public ConverterBinder(ConverterRegistry registry, CellConverterFactory factory) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    /**
     * Binds the columns of a sheet.
     *
     * @param metadata the sheet metadata
     * @return the binding
     * @throws SheetsmithConfigurationException listing every column that cannot be bound
     */
    public SheetBinding bind(SheetMetadata metadata) {
        Objects.requireNonNull(metadata, "metadata");
        List<ConfigurationError> errors = new ArrayList<>();
        List<SheetBinding.Column> columns = bindColumns(metadata.type(), metadata.columns(), errors);
        if (!errors.isEmpty()) {
            throw new SheetsmithConfigurationException(errors);
        }
        return new SheetBinding(metadata, columns);
    }

    /**
     * Checks that every given column can be bound, without throwing.
     *
     * @param type    the exported class
     * @param columns the columns to check
     * @return the binding errors, empty if every column can be bound
     */
    public List<ConfigurationError> check(Class<?> type, List<ColumnMetadata> columns) {
        List<ConfigurationError> errors = new ArrayList<>();
        bindColumns(Objects.requireNonNull(type, "type"), columns, errors);
        return List.copyOf(errors);
    }

    private List<SheetBinding.Column> bindColumns(Class<?> type, List<ColumnMetadata> columns,
                                                  List<ConfigurationError> errors) {
        List<SheetBinding.Column> bound = new ArrayList<>();
        for (ColumnMetadata column : columns) {
            CellConverter<?> converter = column.converterClass() != null
                    ? fieldConverter(type, column, errors)
                    : registeredConverter(type, column, errors);
            if (converter != null) {
                bound.add(new SheetBinding.Column(column, unchecked(converter)));
            }
        }
        return bound;
    }

    private CellConverter<?> registeredConverter(Class<?> type, ColumnMetadata column,
                                                 List<ConfigurationError> errors) {
        try {
            CellConverter<?> converter = registry.find(column.valueType());
            if (converter == null) {
                errors.add(new ConfigurationError(NO_CONVERTER, type, column.fieldName(), "no converter for type "
                        + column.valueType().getName() + ": declare one with @ExcelColumn(converter = ...) or "
                        + "register one for the type"));
            }
            return converter;
        } catch (ConverterRegistry.AmbiguousConverterException e) {
            errors.add(new ConfigurationError(NO_CONVERTER, type, column.fieldName(), e.getMessage()
                    + ": declare one with @ExcelColumn(converter = ...) or register one for the exact type"));
            return null;
        }
    }

    private CellConverter<?> fieldConverter(Class<?> type, ColumnMetadata column, List<ConfigurationError> errors) {
        Class<? extends CellConverter<?>> converterClass = column.converterClass();
        Class<?> handled = ConverterTypes.handledType(converterClass);
        Class<?> valueType = ConverterRegistry.box(column.valueType());
        if (handled != null && !handled.isAssignableFrom(valueType)) {
            errors.add(new ConfigurationError(INCOMPATIBLE_CONVERTER, type, column.fieldName(), "converter "
                    + converterClass.getName() + " handles " + handled.getName()
                    + ", which is not assignable from the field type " + column.valueType().getName()));
            return null;
        }
        try {
            return fieldConverters.computeIfAbsent(converterClass, this::create);
        } catch (RuntimeException e) {
            errors.add(new ConfigurationError(UNCREATABLE_CONVERTER, type, column.fieldName(), "converter "
                    + converterClass.getName() + " cannot be created: " + e.getMessage()));
            return null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private CellConverter<?> create(Class<?> converterClass) {
        CellConverter<?> converter = factory.create((Class) converterClass);
        if (converter == null) {
            throw new IllegalStateException("the converter factory returned null");
        }
        return converter;
    }

    @SuppressWarnings("unchecked")
    private static CellConverter<Object> unchecked(CellConverter<?> converter) {
        return (CellConverter<Object>) converter;
    }
}
