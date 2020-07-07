package core.framework.internal.cache;

import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
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
        Map<String, byte[]> values = cacheStore.getAll("key1", "key2");
        assertThat(values).isEmpty();
    }

    @Test
    void get() {
        byte[] value = Strings.bytes("value");
        cacheStore.put("key1", value, Duration.ofMinutes(1));

        byte[] retrievedValue = cacheStore.get("key1");
        assertThat(retrievedValue).isEqualTo(value);

        LocalCacheStore.CacheItem item = cacheStore.caches.get("key1");
        assertThat(item.hits).isEqualTo(1);

        cacheStore.get("key1");
        assertThat(item.hits).isEqualTo(2);
    }

    @Test
    void getWithExpiredKey() {
        byte[] value = Strings.bytes("value");
        cacheStore.put("key1", value, Duration.ZERO);

        byte[] retrievedValue = cacheStore.get("key1");
        assertThat(retrievedValue).isNull();
    }

    @Test
    void cleanup() {
        cacheStore.put("key1", Strings.bytes("value"), Duration.ZERO);
        cacheStore.put("key2", Strings.bytes("value"), Duration.ofMinutes(1));
        cacheStore.cleanup();

        assertThat(cacheStore.caches).hasSize(1);
    }

    @Test
    void cleanupWithEviction() {
        cacheStore.maxSize = 6;
        cacheStore.put("k1", Strings.bytes("12345"), Duration.ofHours(1));
        cacheStore.put("k2", Strings.bytes("12345"), Duration.ofHours(1));
        cacheStore.put("k3", Strings.bytes("12345"), Duration.ofHours(1));
        cacheStore.get("k1");
        cacheStore.get("k2");
        cacheStore.get("k2");

        cacheStore.cleanup();
        assertThat(cacheStore.caches).containsOnlyKeys("k2");
    }

    @Test
    void putAll() {
        var values = Map.of("key1", Strings.bytes("1"),
                "key2", Strings.bytes("2"));
        cacheStore.putAll(values, Duration.ofMinutes(1));

        assertThat(cacheStore.caches).hasSize(2);
    }

    @Test
    void delete() {
        cacheStore.put("key1", Strings.bytes("value"), Duration.ofMinutes(1));
        cacheStore.put("key2", Strings.bytes("value"), Duration.ofMinutes(1));
        cacheStore.delete("key1", "key2");

        assertThat(cacheStore.caches).isEmpty();
    }

    @Test
    void clear() {
        cacheStore.clear();

        assertThat(cacheStore.caches).isEmpty();
    }
}
