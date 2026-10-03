package cloud.baldilorenzo.sheetsmith.internal.metadata;

import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.fixtures.InvalidSheets;
import cloud.baldilorenzo.sheetsmith.fixtures.ValidSheets;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MetadataCacheTest {

    @Test
    void metadataIsBuiltOnceAndServedFromTheCache() {
        AtomicInteger extractions = new AtomicInteger();
        MetadataExtractor extractor = new MetadataExtractor();
        MetadataCache cache = new MetadataCache(type -> {
            extractions.incrementAndGet();
            return extractor.extract(type);
        });

        SheetMetadata first = cache.get(ValidSheets.Person.class);
        SheetMetadata second = cache.get(ValidSheets.Person.class);

        assertThat(second).isSameAs(first);
        assertThat(extractions).hasValue(1);
    }

    @Test
    void invalidClassIsNotCachedAndFailsAtEveryCall() {
        AtomicInteger extractions = new AtomicInteger();
        MetadataExtractor extractor = new MetadataExtractor();
        MetadataCache cache = new MetadataCache(type -> {
            extractions.incrementAndGet();
            return extractor.extract(type);
        });

        for (int i = 0; i < 2; i++) {
            assertThatExceptionOfType(SheetsmithConfigurationException.class)
                    .isThrownBy(() -> cache.get(InvalidSheets.V02NoColumns.class));
        }
        assertThat(extractions).hasValue(2);
    }

    @Test
    void concurrentCallersGetTheSameInstance() throws Exception {
        MetadataCache cache = new MetadataCache();
        int threads = 16;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<SheetMetadata>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                results.add(executor.submit(() -> {
                    start.await();
                    return cache.get(ValidSheets.Invoice.class);
                }));
            }
            start.countDown();

            SheetMetadata expected = results.get(0).get();
            for (Future<SheetMetadata> result : results) {
                assertThat(result.get()).isSameAs(expected);
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
