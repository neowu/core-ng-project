package core.framework.api.module;

import core.framework.api.cache.Cache;
import core.framework.api.redis.Redis;
import core.framework.api.redis.RedisBuilder;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Types;
import core.framework.impl.cache.CacheManager;
import core.framework.impl.cache.CacheStore;
import core.framework.impl.cache.LocalCacheStore;
import core.framework.impl.cache.RedisCacheStore;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.management.CacheController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;

/**
 * @author neo
 */
public class CacheConfig {
    private static final Duration REDIS_CACHE_TIMEOUT = Duration.ofMillis(500);  // cache redis use shorter 500ms as timeout
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
        if (!context.test) {
            context.startupHook.add(cacheStore::start);
            context.shutdownHook.add(cacheStore::shutdown);
        }

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
            Redis redis = new RedisBuilder().host(host)
                .timeout(REDIS_CACHE_TIMEOUT)
                .get();
            context.shutdownHook.add(redis::shutdown);

            configureCacheManager(new RedisCacheStore(redis));
        }
    }

    private void configureCacheManager(CacheStore cacheStore) {
        context.cacheManager = new CacheManager(cacheStore);

        if (!context.test) {
            CacheController controller = new CacheController(context.cacheManager);
            context.httpServer.get("/management/cache", controller::list);
            context.httpServer.get("/management/cache/:name/:key", controller::get);
            context.httpServer.delete("/management/cache/:name/:key", controller::delete);
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
