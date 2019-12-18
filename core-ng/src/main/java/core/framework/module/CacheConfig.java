package core.framework.module;

import core.framework.cache.Cache;
import core.framework.http.HTTPMethod;
import core.framework.internal.cache.CacheManager;
import core.framework.internal.cache.LocalCacheStore;
import core.framework.internal.cache.RedisCacheStore;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import core.framework.internal.redis.RedisImpl;
import core.framework.internal.resource.PoolMetrics;
import core.framework.internal.web.management.CacheController;
import core.framework.internal.web.management.ListCacheResponse;
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
    private LocalCacheStore localCacheStore;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;

        cacheManager = new CacheManager();

        var controller = new CacheController(cacheManager);
        context.route(HTTPMethod.GET, "/_sys/cache", (LambdaController) controller::list, true);
        context.bean(ListCacheResponse.class);
        context.route(HTTPMethod.GET, "/_sys/cache/:name/:key", (LambdaController) controller::get, true);
        context.route(HTTPMethod.DELETE, "/_sys/cache/:name/:key", (LambdaController) controller::delete, true);
    }

    @Override
    protected void validate() {
        if (cacheManager.caches().isEmpty()) {
            throw new Error("cache is configured but no cache added, please remove unnecessary config");
        }
    }

    public void redis(String host) {
        if (cacheManager.remoteCacheStore != null)
            throw new Error("cache is already configured, please configure cache store only once");

        configureRedis(host);
    }

    public void local() {
        if (cacheManager.remoteCacheStore != null)
            throw new Error("cache store is already configured, please configure only once");

        cacheManager.remoteCacheStore = localCacheStore();
    }

    public void maxLocalSize(long size) {
        localCacheStore().maxSize = size;
    }

    public <T> void local(Class<T> cacheClass, Duration duration) {
        if (cacheManager.localCacheStore == null) {
            cacheManager.localCacheStore = localCacheStore();
        }

        logger.info("add local cache, class={}", cacheClass.getCanonicalName());
        Cache<T> cache = cacheManager.add(cacheClass, duration, true);
        context.beanFactory.bind(Types.generic(Cache.class, cacheClass), null, cache);
    }

    public <T> void remote(Class<T> cacheClass, Duration duration) {
        if (cacheManager.remoteCacheStore == null) throw new Error("cache store is not configured, please configure first");

        logger.info("add remote cache, class={}", cacheClass.getCanonicalName());
        Cache<T> cache = cacheManager.add(cacheClass, duration, false);
        context.beanFactory.bind(Types.generic(Cache.class, cacheClass), null, cache);
    }

    void configureRedis(String host) {
        logger.info("create redis cache store, host={}", host);

        RedisImpl redis = new RedisImpl("redis-cache");
        redis.host = host;
        redis.timeout(Duration.ofSeconds(1));   // for cache, use shorter timeout than default redis config
        context.shutdownHook.add(ShutdownHook.STAGE_7, timeout -> redis.close());
        context.backgroundTask().scheduleWithFixedDelay(redis.pool::refresh, Duration.ofMinutes(5));
        context.stat.metrics.add(new PoolMetrics(redis.pool));
        cacheManager.remoteCacheStore = new RedisCacheStore(redis);
    }

    private LocalCacheStore localCacheStore() {
        if (localCacheStore == null) {
            logger.info("create local cache store");
            var cacheStore = new LocalCacheStore();
            context.backgroundTask().scheduleWithFixedDelay(cacheStore::cleanup, Duration.ofMinutes(5));
            localCacheStore = cacheStore;
        }
        return localCacheStore;
    }
}
