package core.framework.internal.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class LocalCacheStoreTest {
    private LocalCacheStore cacheStore;

    @BeforeEach
    void createLocalCacheStore() {
        cacheStore = new LocalCacheStore();
    }

    @Test
    void getAll() {
        Map<String, TestCache> values = cacheStore.getAll(new String[]{"key1", "key2"}, null);
        assertThat(values).isEmpty();

        var value = new TestCache();
        cacheStore.put("key1", value, Duration.ofMinutes(1), null);
        values = cacheStore.getAll(new String[]{"key1", "key2"}, null);
        assertThat(values).hasSize(1).containsEntry("key1", value);
    }

    @Test
    void get() {
        var value = new TestCache();
        cacheStore.put("key1", value, Duration.ofMinutes(1), null);

        TestCache retrievedValue = cacheStore.get("key1", null);
        assertThat(retrievedValue).isSameAs(value);

        LocalCacheStore.CacheItem<?> item = cacheStore.caches.get("key1");
        assertThat(item.hits).isEqualTo(1);

        cacheStore.get("key1", null);
        assertThat(item.hits).isEqualTo(2);
    }

    @Test
    void getWithExpiredKey() {
        var value = new TestCache();
        cacheStore.put("key1", value, Duration.ZERO, null);

        TestCache retrievedValue = cacheStore.get("key1", null);
        assertThat(retrievedValue).isNull();
    }

    @Test
    void cleanup() {
        cacheStore.put("key1", new TestCache(), Duration.ZERO, null);
        cacheStore.put("key2", new TestCache(), Duration.ofMinutes(1), null);
        cacheStore.cleanup();

        assertThat(cacheStore.caches).hasSize(1);
    }

    @Test
    void cleanupWithEviction() {
        cacheStore.maxSize = 1;
        cacheStore.put("k1", new TestCache(), Duration.ofHours(1), null);
        cacheStore.put("k2", new TestCache(), Duration.ofHours(1), null);
        cacheStore.put("k3", new TestCache(), Duration.ofHours(1), null);
        cacheStore.get("k1", null);
        cacheStore.get("k2", null);
        cacheStore.get("k2", null);

        cacheStore.cleanup();
        assertThat(cacheStore.caches).containsOnlyKeys("k2");
    }

    @Test
    void putAll() {
        var values = List.of(new CacheStore.Entry<>("key1", new TestCache()),
                new CacheStore.Entry<>("key2", new TestCache()));
        cacheStore.putAll(values, Duration.ofMinutes(1), null);

        assertThat(cacheStore.caches).hasSize(2);
    }

    @Test
    void delete() {
        cacheStore.put("key1", new TestCache(), Duration.ofMinutes(1), null);
        cacheStore.put("key2", new TestCache(), Duration.ofMinutes(1), null);

        assertThat(cacheStore.delete("key1", "key2")).isTrue();
        assertThat(cacheStore.caches).isEmpty();

        assertThat(cacheStore.delete("key1", "key2")).isFalse();
    }

    @Test
    void clear() {
        cacheStore.clear();

        assertThat(cacheStore.caches).isEmpty();
    }
}
