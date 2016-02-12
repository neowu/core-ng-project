package core.framework.impl.cache;

import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
public class CacheImplTest {
    private CacheImpl<Integer> cache;
    private CacheStore cacheStore;

    @Before
    public void prepare() {
        cacheStore = Mockito.mock(CacheStore.class);
        cache = new CacheImpl<>("name", Integer.class, Duration.ofHours(1), cacheStore);
    }

    @Test
    public void get() {
        when(cacheStore.get("name:key")).thenReturn(Strings.bytes("1"));

        Integer value = cache.get("key", key -> null);
        assertEquals(1, (int) value);
    }

    @Test
    public void getIfMiss() {
        when(cacheStore.get("name:key")).thenReturn(null);

        Integer value = cache.get("key", key -> 1);
        assertEquals(1, (int) value);

        verify(cacheStore).put("name:key", Strings.bytes("1"), Duration.ofHours(1));
    }

    @Test
    public void put() {
        cache.put("key", 1);

        verify(cacheStore).put("name:key", Strings.bytes("1"), Duration.ofHours(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAll() {
        Map<String, byte[]> values = Maps.newHashMap();
        values.put("name:key1", Strings.bytes("1"));
        values.put("name:key3", Strings.bytes("3"));
        when(cacheStore.getAll(new String[]{"name:key1", "name:key2", "name:key3"})).thenReturn(values);

        Map<String, Integer> results = cache.getAll(Arrays.asList("key1", "key2", "key3"), key -> 2);
        assertEquals(3, results.size());
        assertEquals(1, results.get("key1").intValue());
        assertEquals(2, results.get("key2").intValue());
        assertEquals(3, results.get("key3").intValue());

        ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        verify(cacheStore).putAll(argument.capture(), eq(Duration.ofHours(1)));
        assertArrayEquals(Strings.bytes("2"), (byte[]) argument.getValue().get("name:key2"));
    }
}