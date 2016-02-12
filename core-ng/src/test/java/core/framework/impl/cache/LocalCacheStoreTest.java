package core.framework.impl.cache;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

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
        Map<String, byte[]> values = cacheStore.getAll(new String[]{"key1", "key2"});
        assertNull(values.get("key1"));
        assertNull(values.get("key2"));
    }
}