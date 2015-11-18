package core.framework.impl.cache;

import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.List;

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
        when(cacheStore.get("name", "key")).thenReturn("1");

        Integer value = cache.get("key", key -> null);
        Assert.assertEquals(1, (int) value);
    }

    @Test
    public void getIfMiss() {
        when(cacheStore.get("name", "key")).thenReturn(null);

        Integer value = cache.get("key", key -> 1);
        Assert.assertEquals(1, (int) value);

        verify(cacheStore).put("name", "key", "1", Duration.ofHours(1));
    }

    @Test
    public void put() {
        cache.put("key", 1);

        verify(cacheStore).put("name", "key", "1", Duration.ofHours(1));
    }

    @Test
    public void getAll() {
        List<String> keys = Lists.newArrayList("key1", "key2", "key3");
        when(cacheStore.getAll("name", keys)).thenReturn(Lists.newArrayList("1", null, "3"));

        List<Integer> results = cache.getAll(keys, key -> 2);
        Assert.assertEquals(Lists.newArrayList(1, 2, 3), results);

        verify(cacheStore).putAll("name", Maps.newHashMap("key2", "2"), Duration.ofHours(1));
    }
}