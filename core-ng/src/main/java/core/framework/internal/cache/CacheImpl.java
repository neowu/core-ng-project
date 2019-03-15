package core.framework.internal.cache;

import core.framework.cache.Cache;
import core.framework.internal.json.JSONMapper;
import core.framework.internal.validate.ValidationException;
import core.framework.internal.validate.Validator;
import core.framework.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class CacheImpl<T> implements Cache<T> {
    public final String name;
    public final Class<T> cacheClass;
    public final Duration duration;
    private final Logger logger = LoggerFactory.getLogger(CacheImpl.class);
    private final CacheStore cacheStore;
    private final JSONMapper<T> mapper;
    private final Validator validator;  // only validate when retrieve cache from store, in case data in cache store is stale, e.g. the class structure is changed but still got old data from cache
    // it's opposite as DB, which only validate on save

    CacheImpl(String name, Class<T> cacheClass, Duration duration, CacheStore cacheStore) {
        this.name = name;
        this.cacheClass = cacheClass;
        this.duration = duration;
        this.cacheStore = cacheStore;
        mapper = new JSONMapper<>(cacheClass);
        validator = Validator.of(cacheClass);
    }

    @Override
    public T get(String key, Function<String, T> loader) {
        String cacheKey = cacheKey(key);
        byte[] cacheValue = cacheStore.get(cacheKey);
        if (cacheValue != null) {
            T result = mapper.fromJSON(cacheValue);
            if (validate(result)) return result;
        }
        logger.debug("load value, key={}", key);
        T value = loader.apply(key);
        cacheStore.put(cacheKey, mapper.toJSON(value), duration);
        return value;
    }

    public Optional<String> get(String key) {
        byte[] result = cacheStore.get(cacheKey(key));
        if (result == null) return Optional.empty();
        return Optional.of(new String(result, UTF_8));
    }

    @Override
    public Map<String, T> getAll(Collection<String> keys, Function<String, T> loader) {
        int size = keys.size();
        int index;
        String[] cacheKeys = cacheKeys(keys);
        Map<String, T> values = Maps.newHashMapWithExpectedSize(size);
        Map<String, byte[]> newValues = Maps.newHashMapWithExpectedSize(size);
        Map<String, byte[]> cacheValues = cacheStore.getAll(cacheKeys);
        index = 0;
        for (String key : keys) {
            String cacheKey = cacheKeys[index];
            byte[] cacheValue = cacheValues.get(cacheKey);
            boolean load = true;
            if (cacheValue != null) {
                T result = mapper.fromJSON(cacheValue);
                if (validate(result)) {
                    values.put(key, result);
                    load = false;
                }
            }
            if (load) {
                logger.debug("load value, key={}", key);
                T value = loader.apply(key);
                newValues.put(cacheKey, mapper.toJSON(value));
                values.put(key, value);
            }
            index++;
        }
        if (!newValues.isEmpty()) cacheStore.putAll(newValues, duration);
        return values;
    }

    @Override
    public void put(String key, T value) {
        cacheStore.put(cacheKey(key), mapper.toJSON(value), duration);
    }

    @Override
    public void putAll(Map<String, T> values) {
        Map<String, byte[]> cacheValues = Maps.newHashMapWithExpectedSize(values.size());
        for (Map.Entry<String, T> entry : values.entrySet()) {
            cacheValues.put(cacheKey(entry.getKey()), mapper.toJSON(entry.getValue()));
        }
        cacheStore.putAll(cacheValues, duration);
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

    private boolean validate(T bean) {
        try {
            validator.validate(bean, false);
            return true;
        } catch (ValidationException e) {
            logger.warn("failed to validate value from cache, will load by loader", e);
            return false;
        }
    }
}
