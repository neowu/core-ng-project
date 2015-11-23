package core.framework.impl.cache;

import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author neo
 */
public class LocalCacheStoreTest {
    LocalCacheStore cacheStore;

    @Before
    public void createLocalCacheStore() {
        cacheStore = new LocalCacheStore();
    }

    @Test
    public void getAll() {
        Map<String, String> values = cacheStore.getAll("name", Lists.newArrayList("key1", "key2"));
        assertNull(values.get("key1"));
        assertNull(values.get("key2"));
    }

    @Test
    public void putAll() {
        Map<String, String> values = Maps.newHashMap();
        values.put("key1", "v1");
        values.put("key2", "v2");
        cacheStore.putAll("name", values, Duration.ofHours(1));

        assertEquals("v1", cacheStore.get("name", "key1"));
        assertEquals(values, cacheStore.getAll("name", Lists.newArrayList("key1", "key2")));
    }
}