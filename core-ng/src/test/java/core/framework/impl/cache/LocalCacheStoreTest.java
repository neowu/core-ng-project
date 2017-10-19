package core.framework.impl.cache;

import core.framework.util.Maps;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        Map<String, byte[]> values = cacheStore.getAll(new String[]{"key1", "key2"});
        assertNull(values.get("key1"));
        assertNull(values.get("key2"));
    }

    @Test
    void get() {
        byte[] value = Strings.bytes("value");
        cacheStore.put("key1", value, Duration.ofMinutes(1));

        byte[] retrievedValue = cacheStore.get("key1");
        assertArrayEquals(value, retrievedValue);
    }

    @Test
    void getExpiredKey() {
        byte[] value = Strings.bytes("value");
        cacheStore.put("key1", value, Duration.ZERO);

        byte[] retrievedValue = cacheStore.get("key1");
        assertNull(retrievedValue);
    }

    @Test
    void cleanup() {
        cacheStore.put("key1", Strings.bytes("value"), Duration.ZERO);
        cacheStore.put("key2", Strings.bytes("value"), Duration.ofMinutes(1));
        cacheStore.cleanup();

        assertEquals(1, cacheStore.caches.size());
    }

    @Test
    void putAll() {
        Map<String, byte[]> values = Maps.newHashMap();
        values.put("key1", Strings.bytes("1"));
        values.put("key2", Strings.bytes("2"));
        cacheStore.putAll(values, Duration.ofMinutes(1));

        assertEquals(2, cacheStore.caches.size());
    }

    @Test
    void delete() {
        cacheStore.put("key1", Strings.bytes("value"), Duration.ofMinutes(1));
        cacheStore.delete("key1");

        assertEquals(0, cacheStore.caches.size());
    }
}
