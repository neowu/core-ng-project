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
    void cacheStore() {
        cacheManager.localCacheStore = mock(LocalCacheStore.class);

        assertThat(cacheManager.cacheStore(true)).isSameAs(cacheManager.localCacheStore);
        assertThat(cacheManager.cacheStore(false)).isSameAs(cacheManager.localCacheStore);

        cacheManager.localCacheStore = null;
        cacheManager.redisCacheStore = mock(RedisCacheStore.class);
        assertThat(cacheManager.cacheStore(false)).isSameAs(cacheManager.redisCacheStore);

        cacheManager.localCacheStore = mock(LocalCacheStore.class);
        cacheManager.redisCacheStore = mock(RedisCacheStore.class);
        cacheManager.redisLocalCacheStore = mock(RedisLocalCacheStore.class);
        assertThat(cacheManager.cacheStore(true)).isSameAs(cacheManager.redisLocalCacheStore);
        assertThat(cacheManager.cacheStore(false)).isSameAs(cacheManager.redisCacheStore);
    }

    @Test
    void add() {
        cacheManager.localCacheStore = mock(LocalCacheStore.class);
        Cache<TestCache> cache = cacheManager.add(TestCache.class, Duration.ofHours(1), true);
        assertThat(cache).isNotNull();

        assertThatThrownBy(() -> cacheManager.add(TestCache.class, Duration.ofHours(1), true))
                .isInstanceOf(Error.class)
                .hasMessageContaining("found duplicate cache name");
    }
}
