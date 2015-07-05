package core.framework.impl.cache;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
public class CacheImplTest {
    @Test
    public void get() {
        CacheStore store = Mockito.mock(CacheStore.class);
        when(store.get("name", "key")).thenReturn("1");

        CacheImpl<Integer> cache = new CacheImpl<>("name", Integer.class, Duration.ofHours(1), store);

        Integer value = cache.get("key", () -> null);
        Assert.assertEquals(1, (int) value);
    }

    @Test
    public void put() {
        CacheStore store = Mockito.mock(CacheStore.class);
        when(store.get("name", "key")).thenReturn("1");

        CacheImpl<Integer> cache = new CacheImpl<>("name", Integer.class, Duration.ofHours(1), store);
        cache.put("key", 1);

        verify(store).put("name", "key", "1", Duration.ofHours(1));
    }
}