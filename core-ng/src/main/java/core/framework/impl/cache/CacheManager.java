package core.framework.impl.cache;

import core.framework.cache.Cache;
import core.framework.impl.reflect.GenericTypes;
import core.framework.util.ASCII;
import core.framework.util.Maps;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public class CacheManager {
    private final CacheStore cacheStore;
    private final Map<String, CacheImpl<?>> caches = Maps.newHashMap();

    public CacheManager(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    public <T> Cache<T> add(Type valueType, Duration duration) {
        new CacheTypeValidator(valueType).validate();

        String name = cacheName(valueType);
        CacheImpl<T> cache = new CacheImpl<>(name, valueType, duration, cacheStore);
        CacheImpl<?> previous = caches.putIfAbsent(name, cache);
        if (previous != null) throw new Error("found duplicate cache name, name=" + name);
        return cache;
    }

    public Optional<CacheImpl<?>> get(String name) {
        return Optional.ofNullable(caches.get(name));
    }

    public List<CacheImpl<?>> caches() {
        return new ArrayList<>(caches.values());
    }

    // cache only supports List<T> and bean, may simplify further
    String cacheName(Type valueType) {
        if (valueType instanceof Class) {
            return ASCII.toLowerCase(((Class<?>) valueType).getSimpleName());
        } else if (GenericTypes.isGenericList(valueType)) {
            return "list-" + ASCII.toLowerCase(GenericTypes.listValueClass(valueType).getSimpleName());
        }
        return ASCII.toLowerCase(valueType.getTypeName());
    }
}
