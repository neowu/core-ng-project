package core.framework.internal.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class CacheManagerTest {
    private CacheManager cacheManager;

    @BeforeEach
    void createCacheManager() {
        cacheManager = new CacheManager(null);
    }

    @Test
    void cacheName() {
        assertThat(cacheManager.cacheName(TestCache.class))
                .isEqualTo("testcache");
    }
}
