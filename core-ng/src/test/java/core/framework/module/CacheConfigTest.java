package core.framework.module;

import core.framework.internal.cache.CacheImpl;
import core.framework.internal.cache.LocalCacheStore;
import core.framework.internal.cache.RedisCacheStore;
import core.framework.internal.cache.TestCache;
import core.framework.internal.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class CacheConfigTest {
    private CacheConfig config;

    @BeforeEach
    void createCacheConfig() {
        config = new CacheConfig();
        config.initialize(new ModuleContext(null), null);
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
            .hasMessageContaining("cache is configured but no cache added");
    }

    @Test
    void local() {
        config.local();

        assertThatThrownBy(() -> config.local())
            .hasMessageContaining("cache store is already configured");

        assertThatThrownBy(() -> config.redis("localhost"))
            .hasMessageContaining("cache store is already configured");
    }

    @Test
    void addWithLocal() {
        config.local();

        CacheStoreConfig cacheStoreConfig = config.add(TestCache.class, Duration.ofHours(1));
        CacheImpl<?> cache = config.caches.get("testcache");
        assertThat(cache.cacheStore).isInstanceOf(LocalCacheStore.class);

        cacheStoreConfig.local();
        assertThat(cache.cacheStore).isInstanceOf(LocalCacheStore.class);
    }

    @Test
    void addWithRedis() {
        config.redis("localhost");

        CacheStoreConfig cacheStoreConfig = config.add(TestCache.class, Duration.ofHours(1));
        CacheImpl<?> cache = config.caches.get("testcache");
        assertThat(cache.cacheStore).isInstanceOf(RedisCacheStore.class);

        cacheStoreConfig.local();
        assertThat(cache.cacheStore).isInstanceOf(LocalCacheStore.class);
    }

    @Test
    void cacheName() {
        assertThat(config.cacheName(TestCache.class))
            .isEqualTo("testcache");
    }

    @Test
    void addWithDuplicateCache() {
        config.local();

        config.add(TestCache.class, Duration.ofHours(1));
        assertThat(config.caches.get("testcache")).isNotNull();

        assertThatThrownBy(() -> config.add(TestCache.class, Duration.ofHours(1)))
            .isInstanceOf(Error.class)
            .hasMessageContaining("found duplicate cache name");
    }
}
