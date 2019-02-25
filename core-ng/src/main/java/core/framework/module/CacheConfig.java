package core.framework.module;

import core.framework.cache.Cache;
import core.framework.http.HTTPMethod;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.module.ShutdownHook;
import core.framework.impl.redis.RedisImpl;
import core.framework.impl.resource.PoolMetrics;
import core.framework.impl.web.management.CacheController;
import core.framework.impl.web.management.ListCacheResponse;
import core.framework.internal.cache.CacheManager;
import core.framework.internal.cache.CacheStore;
import core.framework.internal.cache.LocalCacheStore;
import core.framework.internal.cache.RedisCacheStore;
import core.framework.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public class CacheConfig extends Config {
    private final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
    private ModuleContext context;
    private CacheManager cacheManager;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
    }

    @Override
    protected void validate() {
        if (cacheManager == null || cacheManager.caches().isEmpty()) {
            throw new Error("cache is configured but no cache added, please remove unnecessary config");
        }
    }

    public void local() {
        if (cacheManager != null)
            throw new Error("cache is already configured, please configure cache store only once");

        logger.info("create local cache store");
        LocalCacheStore cacheStore = new LocalCacheStore();
        context.backgroundTask().scheduleWithFixedDelay(cacheStore::cleanup, Duration.ofMinutes(30));

        configureCacheManager(cacheStore);
    }

    public void redis(String host) {
        if (cacheManager != null)
            throw new Error("cache is already configured, please configure cache store only once");

        configureRedis(host);
    }

    void configureRedis(String host) {
        logger.info("create redis cache manager, host={}", host);

        RedisImpl redis = new RedisImpl("redis-cache");
        redis.host = host;
        redis.timeout(Duration.ofSeconds(1));   // for cache, use shorter timeout than default redis config
        context.shutdownHook.add(ShutdownHook.STAGE_7, timeout -> redis.close());
        context.backgroundTask().scheduleWithFixedDelay(redis.pool::refresh, Duration.ofMinutes(5));
        context.stat.metrics.add(new PoolMetrics(redis.pool));
        configureCacheManager(new RedisCacheStore(redis));
    }

    private void configureCacheManager(CacheStore cacheStore) {
        cacheManager = new CacheManager(cacheStore);

        var controller = new CacheController(cacheManager);
        context.route(HTTPMethod.GET, "/_sys/cache", controller::list, true);
        context.beanBody(ListCacheResponse.class);
        context.route(HTTPMethod.GET, "/_sys/cache/:name/:key", controller::get, true);
        context.route(HTTPMethod.DELETE, "/_sys/cache/:name/:key", controller::delete, true);
    }

    public <T> void add(Class<T> cacheClass, Duration duration) {
        if (cacheManager == null) throw new Error("cache is not configured, please configure cache store first");

        logger.info("add cache, class={}", cacheClass.getCanonicalName());
        Cache<T> cache = cacheManager.add(cacheClass, duration);
        context.beanFactory.bind(Types.generic(Cache.class, cacheClass), null, cache);
    }
}
