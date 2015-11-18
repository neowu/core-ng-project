package core.framework.impl.cache;

import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

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
        List<String> values = cacheStore.getAll("name", Lists.newArrayList("key1", "key2"));
        Assert.assertEquals(Lists.newArrayList(null, null), values);
    }

    @Test
    public void putAll() {
        Map<String, String> values = Maps.newHashMap();
        values.put("key1", "v1");
        values.put("key2", "v2");
        cacheStore.putAll("name", values, Duration.ofHours(1));

        Assert.assertEquals("v1", cacheStore.get("name", "key1"));
        Assert.assertEquals(Lists.newArrayList("v1", "v2"), cacheStore.getAll("name", Lists.newArrayList("key1", "key2")));
    }
}