package core.framework.impl.cache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
public class CacheImplTest {
    CacheStore cacheStore;

    @Before
    public void prepare() {
        cacheStore = Mockito.mock(CacheStore.class);
    }

    @Test
    public void get() {
        when(cacheStore.get("name", "key")).thenReturn("1");

        CacheImpl<Integer> cache = new CacheImpl<>("name", Integer.class, Duration.ofHours(1), cacheStore);

        Integer value = cache.get("key", () -> null);
        Assert.assertEquals(1, (int) value);
    }

    @Test
    public void put() {
        CacheImpl<Integer> cache = new CacheImpl<>("name", Integer.class, Duration.ofHours(1), cacheStore);
        cache.put("key", 1);

        verify(cacheStore).put("name", "key", "1", Duration.ofHours(1));
    }
}