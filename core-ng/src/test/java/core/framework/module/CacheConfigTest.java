package core.framework.module;

import core.framework.util.Types;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class CacheConfigTest {
    @Test
    void cacheName() {
        assertEquals("list-string", CacheConfig.cacheName(null, Types.list(String.class)));

        assertEquals("string", CacheConfig.cacheName(null, String.class));

        assertEquals("name", CacheConfig.cacheName("name", String.class));
    }
}
