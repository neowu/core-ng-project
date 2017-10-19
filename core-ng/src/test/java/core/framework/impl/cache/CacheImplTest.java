package core.framework.impl.cache;

import core.framework.util.Maps;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class CacheImplTest {
    private CacheImpl<Integer> cache;
    private CacheStore cacheStore;

    @BeforeEach
    void createCache() {
        cacheStore = Mockito.mock(CacheStore.class);
        cache = new CacheImpl<>("name", Integer.class, Duration.ofHours(1), cacheStore);
    }

    @Test
    void hit() {
        when(cacheStore.get("name:key")).thenReturn(Strings.bytes("1"));

        Integer value = cache.get("key", key -> null);
        assertEquals(1, (int) value);
    }

    @Test
    void miss() {
        when(cacheStore.get("name:key")).thenReturn(null);

        Integer value = cache.get("key", key -> 1);
        assertEquals(1, (int) value);

        verify(cacheStore).put("name:key", Strings.bytes("1"), Duration.ofHours(1));
    }

    @Test
    void get() {
        when(cacheStore.get("name:key")).thenReturn(Strings.bytes("1"));

        Optional<String> value = cache.get("key");
        assertTrue(value.isPresent());
        assertEquals("1", value.get());
    }

    @Test
    void put() {
        cache.put("key", 1);

        verify(cacheStore).put("name:key", Strings.bytes("1"), Duration.ofHours(1));
    }

    @Test
    void evict() {
        cache.evict("key");

        verify(cacheStore).delete("name:key");
    }

    @Test
    void getAll() {
        Map<String, byte[]> values = Maps.newHashMap();
        values.put("name:key1", Strings.bytes("1"));
        values.put("name:key3", Strings.bytes("3"));
        when(cacheStore.getAll(new String[]{"name:key1", "name:key2", "name:key3"})).thenReturn(values);

        Map<String, Integer> results = cache.getAll(Arrays.asList("key1", "key2", "key3"), key -> 2);
        assertEquals(3, results.size());
        assertEquals(1, results.get("key1").intValue());
        assertEquals(2, results.get("key2").intValue());
        assertEquals(3, results.get("key3").intValue());

        verify(cacheStore).putAll(argThat(argument -> argument.size() == 1 && Arrays.equals(argument.get("name:key2"), Strings.bytes("2"))), eq(Duration.ofHours(1)));
    }
}
