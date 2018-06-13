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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
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
        assertThat(value).isEqualTo(1);
    }

    @Test
    void miss() {
        when(cacheStore.get("name:key")).thenReturn(null);

        Integer value = cache.get("key", key -> 1);
        assertThat(value).isEqualTo(1);

        verify(cacheStore).put("name:key", Strings.bytes("1"), Duration.ofHours(1));
    }

    @Test
    void get() {
        when(cacheStore.get("name:key")).thenReturn(Strings.bytes("1"));

        Optional<String> value = cache.get("key");
        assertThat(value).get().isEqualTo("1");
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
        when(cacheStore.getAll("name:key1", "name:key2", "name:key3")).thenReturn(values);

        Map<String, Integer> results = cache.getAll(Arrays.asList("key1", "key2", "key3"), key -> 2);
        assertThat(results).containsExactly(entry("key1", 1), entry("key2", 2), entry("key3", 3));

        verify(cacheStore).putAll(argThat(argument -> argument.size() == 1 && Arrays.equals(argument.get("name:key2"), Strings.bytes("2"))), eq(Duration.ofHours(1)));
    }
}
