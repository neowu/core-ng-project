package core.framework.internal.cache;

import core.framework.json.JSON;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class CacheImplTest {
    private CacheImpl<TestCache> cache;
    private CacheStore cacheStore;

    @BeforeEach
    void createCache() {
        cacheStore = mock(CacheStore.class);
        cache = new CacheImpl<>("name", TestCache.class, Duration.ofHours(1), cacheStore);
    }

    @Test
    void hit() {
        when(cacheStore.get("name:key")).thenReturn(Strings.bytes("{\"stringField\":\"value\"}"));

        TestCache value = cache.get("key", key -> null);
        assertThat(value.stringField).isEqualTo("value");
    }

    @Test
    void hitWithStaleData() {
        when(cacheStore.get("name:key")).thenReturn(Strings.bytes("{}"));

        TestCache value = cache.get("key", key -> cacheItem("new"));
        assertThat(value.stringField).isEqualTo("new");
    }

    @Test
    void miss() {
        when(cacheStore.get("name:key")).thenReturn(null);

        TestCache value = cache.get("key", key -> cacheItem("value"));
        assertThat(value.stringField).isEqualTo("value");

        verify(cacheStore).put("name:key", Strings.bytes(JSON.toJSON(value)), Duration.ofHours(1));
    }

    @Test
    void get() {
        when(cacheStore.get("name:key")).thenReturn(Strings.bytes("{}"));

        Optional<String> value = cache.get("key");
        assertThat(value).get().isEqualTo("{}");
    }

    @Test
    void loaderReturnsNull() {
        assertThatThrownBy(() -> cache.get("key", key -> null))
                .isInstanceOf(Error.class)
                .hasMessageContaining("value must not be null");
    }

    @Test
    void getAll() {
        var values = Map.of("name:key1", Strings.bytes(JSON.toJSON(cacheItem("v1"))),
                "name:key3", Strings.bytes(JSON.toJSON(cacheItem("v3"))));
        when(cacheStore.getAll("name:key1", "name:key2", "name:key3")).thenReturn(values);

        TestCache item2 = cacheItem("v2");
        Map<String, TestCache> results = cache.getAll(Arrays.asList("key1", "key2", "key3"), key -> item2);
        assertThat(results).containsKeys("key1", "key2", "key3");
        assertThat(results.get("key1").stringField).isEqualTo("v1");
        assertThat(results.get("key2").stringField).isEqualTo("v2");
        assertThat(results.get("key3").stringField).isEqualTo("v3");

        verify(cacheStore).putAll(argThat(argument -> argument.size() == 1 && Arrays.equals(argument.get("name:key2"), Strings.bytes(JSON.toJSON(item2)))), eq(Duration.ofHours(1)));
    }

    @Test
    void getAllWithStaleData() {
        var values = Map.of("name:key1", Strings.bytes("{}"),
                "name:key2", Strings.bytes(JSON.toJSON(cacheItem("key2"))));
        when(cacheStore.getAll("name:key1", "name:key2")).thenReturn(values);

        Map<String, TestCache> results = cache.getAll(Arrays.asList("key1", "key2"), this::cacheItem);
        assertThat(results).containsKeys("key1", "key2");
        assertThat(results.get("key1").stringField).isEqualTo("key1");
        assertThat(results.get("key2").stringField).isEqualTo("key2");

        verify(cacheStore).putAll(argThat(argument -> argument.size() == 1 && Arrays.equals(argument.get("name:key1"), Strings.bytes(JSON.toJSON(cacheItem("key1"))))), eq(Duration.ofHours(1)));
    }

    @Test
    void put() {
        TestCache item = cacheItem("v1");
        cache.put("key", item);

        verify(cacheStore).put("name:key", Strings.bytes(JSON.toJSON(item)), Duration.ofHours(1));
    }

    @Test
    void putAll() {
        TestCache item1 = cacheItem("v1");
        TestCache item2 = cacheItem("v2");
        cache.putAll(Map.of("key1", item1, "key2", item2));

        verify(cacheStore).putAll(argThat(argument -> argument.size() == 2
                && Arrays.equals(argument.get("name:key1"), Strings.bytes(JSON.toJSON(item1)))
                && Arrays.equals(argument.get("name:key2"), Strings.bytes(JSON.toJSON(item2)))), eq(Duration.ofHours(1)));
    }

    @Test
    void evict() {
        cache.evict("key1");

        verify(cacheStore).delete("name:key1");
    }

    @Test
    void evictAll() {
        cache.evictAll(List.of("key1", "key2"));

        verify(cacheStore).delete("name:key1", "name:key2");
    }

    private TestCache cacheItem(String stringField) {
        var result = new TestCache();
        result.stringField = stringField;
        return result;
    }
}
