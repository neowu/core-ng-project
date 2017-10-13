package core.framework.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author neo
 */
public final class Maps {
    public static <T, V> Map<T, V> newHashMap() {
        return new HashMap<>();
    }

    public static <T, V> Map<T, V> newHashMap(T key, V value) {
        HashMap<T, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    public static <T, V> ConcurrentMap<T, V> newConcurrentHashMap() {
        return new ConcurrentHashMap<>();
    }

    public static <T, V> Map<T, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    // refer to guava maps impl, init capacity with load factor 0.75
    // https://github.com/google/guava/blob/master/guava/src/com/google/common/collect/Maps.java
    public static <T, V> Map<T, V> newHashMapWithExpectedSize(int size) {
        int capacity;
        if (size < 3) {
            capacity = size + 1;
        } else {
            capacity = (int) ((float) size / 0.75f + 1);
        }
        return new HashMap<>(capacity);
    }
}
