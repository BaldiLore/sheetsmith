package cloud.baldilorenzo.sheetsmith.internal;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.SheetData;
import cloud.baldilorenzo.sheetsmith.Sheetsmith;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.SheetsmithDefaults;
import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellConverterFactory;
import cloud.baldilorenzo.sheetsmith.internal.convert.ConverterBinder;
import cloud.baldilorenzo.sheetsmith.internal.convert.ConverterRegistry;
import cloud.baldilorenzo.sheetsmith.internal.convert.ReflectiveConverterFactory;
import cloud.baldilorenzo.sheetsmith.internal.convert.SheetBinding;
import cloud.baldilorenzo.sheetsmith.internal.metadata.MetadataCache;
import cloud.baldilorenzo.sheetsmith.internal.metadata.MetadataExtractor;
import cloud.baldilorenzo.sheetsmith.internal.write.WorkbookSupplier;
import cloud.baldilorenzo.sheetsmith.internal.write.WorkbookWriter;
import cloud.baldilorenzo.sheetsmith.internal.write.WritableSheet;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link Sheetsmith}. Immutable and thread-safe.
 */
public final class DefaultSheetsmith implements Sheetsmith {

    /** Metadata does not depend on configuration, so it is shared by every instance. */
    private static final MetadataCache METADATA = new MetadataCache();

    private static final int MAX_SHEET_NAME_LENGTH = 31;
    private static final Pattern FORBIDDEN_SHEET_NAME_CHARS = Pattern.compile("[\\\\/?*\\[\\]:]");

    private final ConverterBinder binder;
    private final WorkbookWriter writer;
    private final MetadataExtractor extractor = new MetadataExtractor();
    private final ClassValue<SheetBinding> bindings = new ClassValue<>() {
        @Override
        protected SheetBinding computeValue(Class<?> type) {
            return binder.bind(METADATA.get(type));
        }
    };

    private DefaultSheetsmith(Builder builder) {
        this.binder = new ConverterBinder(new ConverterRegistry(builder.converters), builder.factory);
        this.writer = new WorkbookWriter(WorkbookSupplier.XSSF, builder.defaults);
    }

    @Override
    public byte[] generate(List<SheetData<?>> sheets) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generate(sheets, out);
        return out.toByteArray();
    }

    @Override
    public void generate(List<SheetData<?>> sheets, OutputStream out) {
        Objects.requireNonNull(sheets, "sheets");
        Objects.requireNonNull(out, "out");
        List<ConfigurationError> errors = new ArrayList<>(inputErrors(sheets));

        Map<Class<?>, SheetBinding> bound = new HashMap<>();
        for (SheetData<?> sheet : sheets) {
            Class<?> type = sheet.type();
            if (!bound.containsKey(type)) {
                try {
                    bound.put(type, bindings.get(type));
                } catch (SheetsmithConfigurationException e) {
                    errors.addAll(errors(type));
                    bound.put(type, null);
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new SheetsmithConfigurationException(errors);
        }

        List<WritableSheet> writable = new ArrayList<>(sheets.size());
        for (SheetData<?> sheet : sheets) {
            writable.add(new WritableSheet(sheet.name(), bound.get(sheet.type()), sheet.rows()));
        }
        FailureRecordingStream target = new FailureRecordingStream(out);
        try {
            writer.write(writable, target);
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (RuntimeException e) {
            IOException failure = target.failure();
            if (failure == null) {
                throw e;
            }
            // POI can report a failure of the stream as an unchecked exception without cause
            UncheckedIOException exception = new UncheckedIOException(failure);
            exception.addSuppressed(e);
            throw exception;
        }
    }

    @Override
    public void validate(Class<?> type) {
        List<ConfigurationError> errors = errors(Objects.requireNonNull(type, "type"));
        if (!errors.isEmpty()) {
            throw new SheetsmithConfigurationException(errors);
        }
    }

    /** Extraction and binding errors of a class, together. */
    private List<ConfigurationError> errors(Class<?> type) {
        MetadataExtractor.Inspection inspection = extractor.inspect(type);
        List<ConfigurationError> errors = new ArrayList<>(inspection.errors());
        errors.addAll(binder.check(type, inspection.columns()));
        return errors;
    }

    /** Rules {@code V-18} to {@code V-20}. */
    private static List<ConfigurationError> inputErrors(List<SheetData<?>> sheets) {
        List<ConfigurationError> errors = new ArrayList<>();
        if (sheets.isEmpty()) {
            errors.add(new ConfigurationError("V-18", null, "", "the sheet list is empty"));
        }
        Set<String> names = new HashSet<>();
        for (int i = 0; i < sheets.size(); i++) {
            SheetData<?> sheet = Objects.requireNonNull(sheets.get(i), "sheets must not contain null elements");
            String name = sheet.name();
            String element = "sheets[" + i + "]";
            String problem = sheetNameProblem(name);
            if (problem != null) {
                errors.add(new ConfigurationError("V-19", null, element, "sheet name '" + name + "' " + problem));
            }
            if (!names.add(name.toLowerCase(Locale.ROOT))) {
                errors.add(new ConfigurationError("V-20", null, element,
                        "sheet name '" + name + "' is already used, ignoring case"));
            }
        }
        return errors;
    }

    private static String sheetNameProblem(String name) {
        if (name.isEmpty() || name.length() > MAX_SHEET_NAME_LENGTH) {
            return "must be 1 to " + MAX_SHEET_NAME_LENGTH + " characters long";
        }
        if (FORBIDDEN_SHEET_NAME_CHARS.matcher(name).find()) {
            return "must not contain any of \\ / ? * [ ] :";
        }
        if (name.startsWith("'") || name.endsWith("'")) {
            return "must not start or end with '";
        }
        return null;
    }

    /**
     * Passes the content to the caller's stream, which it never closes, and keeps the first {@link IOException}
     * the stream throws, so that it can be reported even when POI replaces it with an exception without cause.
     */
    private static final class FailureRecordingStream extends FilterOutputStream {

        private IOException failure;

        FailureRecordingStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            try {
                out.write(b);
            } catch (IOException e) {
                throw record(e);
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            try {
                out.write(b, off, len);
            } catch (IOException e) {
                throw record(e);
            }
        }

        @Override
        public void flush() throws IOException {
            try {
                out.flush();
            } catch (IOException e) {
                throw record(e);
            }
        }

        /** The stream belongs to the caller: closing is reduced to flushing. */
        @Override
        public void close() throws IOException {
            flush();
        }

        IOException failure() {
            return failure;
        }

        private IOException record(IOException e) {
            if (failure == null) {
                failure = e;
            }
            return e;
        }
    }

    /**
     * Default implementation of {@link Sheetsmith.Builder}. Not thread-safe.
     */
    public static final class Builder implements Sheetsmith.Builder {

        private final Map<Class<?>, CellConverter<?>> converters = new LinkedHashMap<>();
        private CellConverterFactory factory = new ReflectiveConverterFactory();
        private SheetsmithDefaults defaults = SheetsmithDefaults.standard();

        /**
         * Creates a builder with default settings.
         */
        public Builder() {
        }

        @Override
        public <T> Builder converter(Class<T> type, CellConverter<? super T> converter) {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(converter, "converter");
            Class<?> key = ConverterRegistry.box(type);
            if (converters.putIfAbsent(key, converter) != null) {
                throw new IllegalArgumentException("a converter is already registered for type " + key.getName());
            }
            return this;
        }

        @Override
        public Builder converterFactory(CellConverterFactory factory) {
            this.factory = Objects.requireNonNull(factory, "factory");
            return this;
        }

        @Override
        public Builder defaults(SheetsmithDefaults defaults) {
            this.defaults = Objects.requireNonNull(defaults, "defaults");
            return this;
        }

        @Override
        public Sheetsmith build() {
            return new DefaultSheetsmith(this);
        }
    }
}
