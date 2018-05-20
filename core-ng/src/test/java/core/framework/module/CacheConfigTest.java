package core.framework.module;

import core.framework.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class CacheConfigTest {
    private CacheConfig config;

    @BeforeEach
    void createCacheConfig() {
        config = new CacheConfig();
    }

    @Test
    void cacheName() {
        assertEquals("list-string", config.cacheName(null, Types.list(String.class)));

        assertEquals("string", config.cacheName(null, String.class));

        assertEquals("name", config.cacheName("name", String.class));
    }
}
