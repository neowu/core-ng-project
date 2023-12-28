package core.framework.module;

import core.framework.cache.Cache;
import core.framework.http.HTTPMethod;
import core.framework.internal.cache.CacheClassValidator;
import core.framework.internal.cache.CacheImpl;
import core.framework.internal.cache.CacheStore;
import core.framework.internal.cache.LocalCacheMetrics;
import core.framework.internal.cache.LocalCacheStore;
import core.framework.internal.cache.RedisCacheStore;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import core.framework.internal.redis.RedisImpl;
import core.framework.internal.resource.PoolMetrics;
import core.framework.internal.web.sys.CacheController;
import core.framework.util.ASCII;
import core.framework.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
public class CacheConfig extends Config {
    private final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
    Map<String, CacheImpl<?>> caches;

    private ModuleContext context;
    private LocalCacheStore localCacheStore;
    private CacheStore redisCacheStore;
    private int maxLocalSize;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;

        caches = new HashMap<>();
        var controller = new CacheController(caches);
        context.route(HTTPMethod.GET, "/_sys/cache", (LambdaController) controller::list, true);
        context.route(HTTPMethod.GET, "/_sys/cache/:name/:key", (LambdaController) controller::get, true);
        context.route(HTTPMethod.DELETE, "/_sys/cache/:name/:key", (LambdaController) controller::delete, true);
    }

    @Override
    protected void validate() {
        if (caches.isEmpty()) {
            throw new Error("cache is configured but no cache added, please remove unnecessary config");
        }
        // maxLocalSize() can be configured before localCacheStore is created, so set max size at end
        if (maxLocalSize > 0 && localCacheStore != null) {
            localCacheStore.maxSize = maxLocalSize;
        }
    }

    public void local() {
        if (localCacheStore != null || redisCacheStore != null) throw new Error("cache store is already configured, please configure only once");
        localCacheStore();
    }

    public void redis(String host) {
        redis(host, null);
    }

    public void redis(String host, String password) {
        if (localCacheStore != null || redisCacheStore != null) throw new Error("cache store is already configured, please configure only once");
        configureRedis(host, password);
    }

    public <T> CacheStoreConfig add(Class<T> cacheClass, Duration duration) {
        if (localCacheStore == null && redisCacheStore == null) throw new Error("cache store is not configured, please configure first");
        logger.info("add cache, class={}, duration={}", cacheClass.getCanonicalName(), duration);
        new CacheClassValidator(cacheClass).validate();
        String name = cacheName(cacheClass);
        var cache = new CacheImpl<>(name, cacheClass, duration);
        cache.cacheStore = redisCacheStore != null ? redisCacheStore : localCacheStore;
        CacheImpl<?> previous = caches.putIfAbsent(name, cache);
        if (previous != null) throw new Error("found duplicate cache name, name=" + name);
        context.beanFactory.bind(Types.generic(Cache.class, cacheClass), null, cache);

        return new CacheStoreConfig(cache, this);
    }

    // number of objects to cache
    public void maxLocalSize(int size) {
        maxLocalSize = size;
    }

    String cacheName(Class<?> cacheClass) {
        return ASCII.toLowerCase(cacheClass.getSimpleName());
    }

    void configureRedis(String host, String password) {
        logger.info("create redis cache store, host={}", host);

        var redis = new RedisImpl("redis-cache");
        redis.host(host);
        redis.password(password);
        redis.timeout(Duration.ofSeconds(1));   // for cache, use shorter timeout than default redis config
        context.probe.hostURIs.add(host);
        context.shutdownHook.add(ShutdownHook.STAGE_6, timeout -> redis.close());
        context.backgroundTask().scheduleWithFixedDelay(redis.pool::refresh, Duration.ofMinutes(5));
        context.collector.metrics.add(new PoolMetrics(redis.pool));
        redisCacheStore = new RedisCacheStore(redis);
    }

    LocalCacheStore localCacheStore() {
        if (localCacheStore == null) {
            logger.info("create local cache store");
            var localCacheStore = new LocalCacheStore();
            context.backgroundTask().scheduleWithFixedDelay(localCacheStore::cleanup, Duration.ofMinutes(5));
            context.collector.metrics.add(new LocalCacheMetrics(localCacheStore));
            this.localCacheStore = localCacheStore;
        }
        return localCacheStore;
    }
}
