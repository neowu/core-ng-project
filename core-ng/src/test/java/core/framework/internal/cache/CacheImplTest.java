package core.framework.internal.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class CacheImplTest {
    @Mock
    CacheStore cacheStore;
    private CacheImpl<TestCache> cache;

    @BeforeEach
    void createCache() {
        cache = new CacheImpl<>("name", TestCache.class, Duration.ofHours(1));
        cache.cacheStore = cacheStore;
    }

    @Test
    void getWhenHit() {
        var value = cacheItem("value");
        when(cacheStore.get("name:key", cache.context)).thenReturn(value);

        TestCache result = cache.get("key", key -> null);
        assertThat(result).isSameAs(value);
    }

    @Test
    void getWhenMiss() {
        when(cacheStore.get("name:key", cache.context)).thenReturn(null);

        TestCache value = cache.get("key", key -> cacheItem("value"));
        assertThat(value.stringField).isEqualTo("value");

        verify(cacheStore).put("name:key", value, Duration.ofHours(1), cache.context);
    }

    @Test
    void get() {
        TestCache item = cacheItem("value");
        when(cacheStore.get("name:key", cache.context)).thenReturn(item);

        Optional<TestCache> value = cache.get("key");
        assertThat(value).get().isSameAs(item);

        assertThat(cache.get("notExistedKey")).isEmpty();
    }

    @Test
    void loaderReturnsNull() {
        assertThatThrownBy(() -> cache.get("key", key -> null))
            .isInstanceOf(Error.class)
            .hasMessageContaining("value must not be null");
    }

    @Test
    void getAllWhenMiss() {
        var values = Map.of("name:key1", cacheItem("v1"),
            "name:key3", cacheItem("v3"));
        when(cacheStore.getAll(new String[]{"name:key1", "name:key2", "name:key3"}, cache.context)).thenReturn(values);

        TestCache item2 = cacheItem("v2");
        Map<String, TestCache> results = cache.getAll(Arrays.asList("key1", "key2", "key3"), key -> item2);
        assertThat(results).containsKeys("key1", "key2", "key3");
        assertThat(results.get("key1").stringField).isEqualTo("v1");
        assertThat(results.get("key2").stringField).isEqualTo("v2");
        assertThat(results.get("key3").stringField).isEqualTo("v3");

        verify(cacheStore).putAll(argThat(argument -> argument.size() == 1 && "v2".equals(argument.get(0).value().stringField)), eq(Duration.ofHours(1)), eq(cache.context));
    }

    @Test
    void getAllWhenHit() {
        var values = Map.of("name:key1", cacheItem("v1"),
            "name:key2", cacheItem("v2"));
        when(cacheStore.getAll(new String[]{"name:key1", "name:key2"}, cache.context)).thenReturn(values);

        Map<String, TestCache> results = cache.getAll(Arrays.asList("key1", "key2"), key -> null);
        assertThat(results).containsKeys("key1", "key2");

        verify(cacheStore, never()).putAll(any(), any(), any());
    }

    @Test
    void put() {
        TestCache item = cacheItem("v1");
        cache.put("key", item);

        verify(cacheStore).put("name:key", item, Duration.ofHours(1), cache.context);
    }

    @Test
    void putAll() {
        cache.putAll(Map.of("key1", cacheItem("v1"),
            "key2", cacheItem("v2")));

        verify(cacheStore).putAll(argThat(argument -> {
            Set<String> keys = argument.stream().map(CacheStore.Entry::key).collect(Collectors.toSet());
            return argument.size() == 2
                   && keys.contains("name:key1")
                   && keys.contains("name:key2");
        }), eq(Duration.ofHours(1)), eq(cache.context));
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
