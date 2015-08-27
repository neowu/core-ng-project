package core.framework.api.module;

import core.framework.api.cache.Cache;
import core.framework.api.http.HTTPMethod;
import core.framework.api.redis.Redis;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Types;
import core.framework.impl.cache.CacheManager;
import core.framework.impl.cache.CacheStore;
import core.framework.impl.cache.LocalCacheStore;
import core.framework.impl.cache.LocalCacheStoreCleanupJob;
import core.framework.impl.cache.RedisCacheStore;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.resource.PoolMaintenanceJob;
import core.framework.impl.scheduler.FixedRateTrigger;
import core.framework.impl.web.ControllerHolder;
import core.framework.impl.web.management.CacheController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;

/**
 * @author neo
 */
public final class CacheConfig {
    private final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
    private final ModuleContext context;

    public CacheConfig(ModuleContext context) {
        this.context = context;
    }

    public void local() {
        if (context.cacheManager != null) {
            throw new Error("cache store is configured, please only configure cache store once at beginning of application");
        }

        logger.info("create local cache store");
        LocalCacheStore cacheStore = new LocalCacheStore();
        context.scheduler().addTrigger(new FixedRateTrigger("local-cache-cleanup", new LocalCacheStoreCleanupJob(cacheStore), Duration.ofMinutes(30)));

        configureCacheManager(cacheStore);
    }

    public void redis(String host) {
        if (context.cacheManager != null) {
            throw new Error("cache store is configured, please only configure cache store once at beginning of application");
        }

        if (context.test) {
            logger.info("use local cache during test");
            local();
        } else {
            logger.info("create redis cache manager, host={}", host);

            Redis redis = new Redis(host);
            redis.pool.name("redis-cache");
            redis.pool.configure(5, 50, Duration.ofMinutes(30));
            redis.timeout = Duration.ofMillis(500);   // for cache, it should not be longer than 500ms to get value

            context.scheduler().addTrigger(new FixedRateTrigger("pool-maintenance-redis-cache", new PoolMaintenanceJob(redis.pool), Duration.ofMinutes(5)));
            context.shutdownHook.add(redis::close);

            configureCacheManager(new RedisCacheStore(redis));
        }
    }

    private void configureCacheManager(CacheStore cacheStore) {
        context.cacheManager = new CacheManager(cacheStore);

        if (!context.test) {
            CacheController controller = new CacheController(context.cacheManager);
            context.httpServer.handler.route.add(HTTPMethod.GET, "/management/cache", new ControllerHolder(controller::list, true));
            context.httpServer.handler.route.add(HTTPMethod.GET, "/management/cache/:name/:key", new ControllerHolder(controller::get, true));
            context.httpServer.handler.route.add(HTTPMethod.DELETE, "/management/cache/:name/:key", new ControllerHolder(controller::delete, true));
        }
    }

    public void add(String name, Type valueType, Duration duration) {
        if (context.cacheManager == null) {
            throw Exceptions.error("cache store is not configured, please configure cache store at beginning of application");
        }

        String cacheName = cacheName(name, valueType);
        logger.info("add cache, cacheName={}, valueType={}, beanName={}", cacheName, valueType.getTypeName(), name);
        Cache<?> cache = context.cacheManager.add(cacheName, valueType, duration);
        context.beanFactory.bind(Types.generic(Cache.class, valueType), name, cache);
    }

    public void add(Type valueType, Duration duration) {
        add(null, valueType, duration);
    }

    String cacheName(String name, Type valueType) {
        if (name != null) return name;
        if (valueType instanceof Class) return ((Class) valueType).getSimpleName().toLowerCase();
        else if (valueType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) valueType;
            StringBuilder builder = new StringBuilder();
            builder.append(((Class<?>) parameterizedType.getRawType()).getSimpleName().toLowerCase());
            Type[] arguments = parameterizedType.getActualTypeArguments();
            for (Type argument : arguments) {
                builder.append('-').append(((Class<?>) argument).getSimpleName().toLowerCase());
            }
            return builder.toString();
        }
        return valueType.getTypeName().toLowerCase();
    }
}
