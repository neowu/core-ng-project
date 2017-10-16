package core.framework.impl.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author neo
 */
class LocalCacheStoreTest {
    LocalCacheStore cacheStore;

    @BeforeEach
    void createLocalCacheStore() {
        cacheStore = new LocalCacheStore();
    }

    @Test
    void getAll() {
        Map<String, byte[]> values = cacheStore.getAll(new String[]{"key1", "key2"});
        assertNull(values.get("key1"));
        assertNull(values.get("key2"));
    }
}
