package core.framework.internal.cache;

import core.framework.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
class CacheManagerTest {
    private CacheManager cacheManager;

    @BeforeEach
    void createCacheManager() {
        cacheManager = new CacheManager();
    }

    @Test
    void cacheName() {
        assertThat(cacheManager.cacheName(TestCache.class))
                .isEqualTo("testcache");
    }

    @Test
    void add() {
        CacheStore store = mock(CacheStore.class);
        Cache<TestCache> cache = cacheManager.add(TestCache.class, Duration.ofHours(1), store);
        assertThat(cache).isNotNull();

        assertThatThrownBy(() -> cacheManager.add(TestCache.class, Duration.ofHours(1), store))
                .isInstanceOf(Error.class)
                .hasMessageContaining("found duplicate cache name");
    }

    @Test
    void get() {
        assertThat(cacheManager.get("testcache")).isEmpty();

        cacheManager.add(TestCache.class, Duration.ofHours(1), mock(CacheStore.class));
        assertThat(cacheManager.get("testcache")).isNotEmpty();
    }
}
