package core.framework.internal.cache;

import core.framework.cache.Cache;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author neo
 */
public class CacheImpl<T> implements Cache<T> {
    public final String name;
    public final Class<T> cacheClass;
    public final Duration duration;

    final CacheContext<T> context;
    private final Logger logger = LoggerFactory.getLogger(CacheImpl.class);

    public CacheStore cacheStore;

    public CacheImpl(String name, Class<T> cacheClass, Duration duration) {
        this.name = name;
        this.cacheClass = cacheClass;
        this.duration = duration;
        context = new CacheContext<>(cacheClass);
    }

    @Override
    public T get(String key, Function<String, T> loader) {
        String cacheKey = cacheKey(key);
        T cacheValue = cacheStore.get(cacheKey, context);
        if (cacheValue != null) {
            stat("cache_hits", 1);
            return cacheValue;
        }

        logger.debug("load value, key={}", key);
        T value = load(loader, key);
        cacheStore.put(cacheKey, value, duration, context);
        stat("cache_misses", 1);
        return value;
    }

    public Optional<T> get(String key) {
        T result = cacheStore.get(cacheKey(key), context);
        if (result == null) return Optional.empty();
        return Optional.of(result);
    }

    @Override
    public Map<String, T> getAll(Collection<String> keys, Function<String, T> loader) {
        int size = keys.size();
        int index = 0;
        String[] cacheKeys = cacheKeys(keys);
        Map<String, T> values = Maps.newHashMapWithExpectedSize(size);
        List<CacheStore.Entry<T>> newValues = new ArrayList<>(size);
        Map<String, T> cacheValues = cacheStore.getAll(cacheKeys, context);
        stat("cache_hits", cacheValues.size());
        for (String key : keys) {
            String cacheKey = cacheKeys[index];
            T result = cacheValues.get(cacheKey);
            if (result == null) {
                logger.debug("load value, key={}", key);
                result = load(loader, key);
                newValues.add(new CacheStore.Entry<>(cacheKey, result));
            }
            values.put(key, result);
            index++;
        }
        if (!newValues.isEmpty()) {
            cacheStore.putAll(newValues, duration, context);
            stat("cache_misses", newValues.size());
        }
        return values;
    }

    @Override
    public void put(String key, T value) {
        cacheStore.put(cacheKey(key), value, duration, context);
    }

    @Override
    public void putAll(Map<String, T> values) {
        List<CacheStore.Entry<T>> cacheValues = new ArrayList<>(values.size());
        for (Map.Entry<String, T> entry : values.entrySet()) {
            cacheValues.add(new CacheStore.Entry<>(cacheKey(entry.getKey()), entry.getValue()));
        }
        cacheStore.putAll(cacheValues, duration, context);
    }

    @Override
    public void evict(String key) {
        cacheStore.delete(cacheKey(key));
    }

    @Override
    public void evictAll(Collection<String> keys) {
        String[] cacheKeys = cacheKeys(keys);
        cacheStore.delete(cacheKeys);
    }

    private String[] cacheKeys(Collection<String> keys) {
        String[] cacheKeys = new String[keys.size()];
        int index = 0;
        for (String key : keys) {
            cacheKeys[index] = cacheKey(key);
            index++;
        }
        return cacheKeys;
    }

    private String cacheKey(String key) {
        return name + ":" + key;
    }

    private T load(Function<String, T> loader, String key) {
        T value = loader.apply(key);
        if (value == null) throw new Error("value must not be null, key=" + key);
        return value;
    }

    // set to actionLog directly to keep trace log concise
    private void stat(String key, double value) {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog != null) {
            actionLog.stats.compute(key, (k, oldValue) -> (oldValue == null) ? value : oldValue + value);
        }
    }
}
