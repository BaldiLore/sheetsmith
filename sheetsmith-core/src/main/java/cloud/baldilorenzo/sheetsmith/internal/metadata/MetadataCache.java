package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;

import java.util.Objects;
import java.util.function.Function;

/**
 * Caches the metadata of exported classes. Thread-safe.
 * <p>
 * Backed by a {@link ClassValue}, which does not keep class loaders alive. A class whose extraction fails is not
 * cached: the exception is thrown again at every call.
 */
public final class MetadataCache {

    private final ClassValue<SheetMetadata> cache;

    /**
     * Creates a cache backed by a {@link MetadataExtractor}.
     */
    public MetadataCache() {
        this(new MetadataExtractor()::extract);
    }

    /**
     * Creates a cache backed by the given extraction function.
     *
     * @param extractor computes the metadata of a class
     */
    public MetadataCache(Function<Class<?>, SheetMetadata> extractor) {
        Objects.requireNonNull(extractor, "extractor");
        this.cache = new ClassValue<>() {
            @Override
            protected SheetMetadata computeValue(Class<?> type) {
                return extractor.apply(type);
            }
        };
    }

    /**
     * Returns the metadata of a class, extracting it on first use.
     *
     * @param type the exported class
     * @return the metadata
     * @throws SheetsmithConfigurationException if the class is invalid
     */
    public SheetMetadata get(Class<?> type) {
        return cache.get(Objects.requireNonNull(type, "type"));
    }
}
