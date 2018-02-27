package core.framework.module;

import core.framework.cache.Cache;
import core.framework.http.HTTPMethod;
import core.framework.impl.cache.CacheManager;
import core.framework.impl.cache.CacheStore;
import core.framework.impl.cache.LocalCacheStore;
import core.framework.impl.cache.RedisCacheStore;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.redis.RedisImpl;
import core.framework.impl.resource.PoolMetrics;
import core.framework.impl.web.management.CacheController;
import core.framework.util.ASCII;
import core.framework.util.Exceptions;
import core.framework.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;

/**
 * @author neo
 */
public final class CacheConfig {
    static String cacheName(String name, Type valueType) {
        if (name != null) return name;
        if (valueType instanceof Class) {
            return ASCII.toLowerCase(((Class<?>) valueType).getSimpleName());
        } else if (valueType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) valueType;
            StringBuilder builder = new StringBuilder();
            builder.append(ASCII.toLowerCase(((Class<?>) parameterizedType.getRawType()).getSimpleName()));
            Type[] arguments = parameterizedType.getActualTypeArguments();
            for (Type argument : arguments) {
                builder.append('-').append(ASCII.toLowerCase(((Class<?>) argument).getSimpleName()));
            }
            return builder.toString();
        }
        return ASCII.toLowerCase(valueType.getTypeName());
    }

    private final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
    private final ModuleContext context;
    private final State state;

    CacheConfig(ModuleContext context) {
        this.context = context;
        state = context.config.state("cache", State::new);
    }

    public void local() {
        if (state.cacheManager != null)
            throw new Error("cache() is already configured, please configure cache store only once");

        logger.info("create local cache store");
        LocalCacheStore cacheStore = new LocalCacheStore();
        context.backgroundTask().scheduleWithFixedDelay(cacheStore::cleanup, Duration.ofMinutes(30));

        configureCacheManager(cacheStore);
    }

    public void redis(String host) {
        if (state.cacheManager != null)
            throw new Error("cache() is already configured, please configure cache store only once");

        if (context.isTest()) {
            logger.info("use local cache during test");
            local();
        } else {
            logger.info("create redis cache manager, host={}", host);

            RedisImpl redis = new RedisImpl("redis-cache");
            redis.host = host;
            redis.timeout(Duration.ofSeconds(1));   // for cache, use shorter timeout than default redis config
            context.shutdownHook.add(redis::close);
            context.backgroundTask().scheduleWithFixedDelay(redis.pool::refresh, Duration.ofMinutes(5));
            context.stat.metrics.add(new PoolMetrics(redis.pool));
            configureCacheManager(new RedisCacheStore(redis));
        }
    }

    private void configureCacheManager(CacheStore cacheStore) {
        state.cacheManager = new CacheManager(cacheStore);

        CacheController controller = new CacheController(state.cacheManager);
        context.route(HTTPMethod.GET, "/_sys/cache", controller::list, true);
        context.route(HTTPMethod.GET, "/_sys/cache/:name/:key", controller::get, true);
        context.route(HTTPMethod.DELETE, "/_sys/cache/:name/:key", controller::delete, true);
    }

    public void add(String name, Type valueType, Duration duration) {
        if (state.cacheManager == null) throw Exceptions.error("cache() is not configured");

        String cacheName = cacheName(name, valueType);
        logger.info("add cache, cacheName={}, valueType={}, beanName={}", cacheName, valueType.getTypeName(), name);
        Cache<?> cache = state.cacheManager.add(cacheName, valueType, duration);
        context.beanFactory.bind(Types.generic(Cache.class, valueType), name, cache);
    }

    public void add(Type valueType, Duration duration) {
        add(null, valueType, duration);
    }

    public static class State implements Config.State {
        CacheManager cacheManager;

        @Override
        public void validate() {
            if (cacheManager.caches().isEmpty()) {
                throw new Error("cache() is configured but no cache added, please remove unnecessary config");
            }
        }
    }
}
