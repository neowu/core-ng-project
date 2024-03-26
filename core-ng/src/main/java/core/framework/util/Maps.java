package core.framework.util;

import java.util.EnumMap;
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

    public static <T, V> ConcurrentMap<T, V> newConcurrentHashMap() {
        return new ConcurrentHashMap<>();
    }

    public static <T, V> Map<T, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    public static <T, V> Map<T, V> newLinkedHashMapWithExpectedSize(int size) {
        return new LinkedHashMap<>(capacity(size));
    }

    public static <T, V> Map<T, V> newHashMapWithExpectedSize(int size) {
        return new HashMap<>(capacity(size));
    }

    // refer to guava maps impl, init capacity with load factor 0.75
    // https://github.com/google/guava/blob/master/guava/src/com/google/common/collect/Maps.java
    private static int capacity(int size) {
        int capacity;
        if (size < 3) {
            capacity = size + 1;
        } else {
            capacity = (int) (size / 0.75f + 1);
        }
        return capacity;
    }

    public static <T extends Enum<T>, V> Map<T, V> newEnumMap(Class<T> enumClass) {
        return new EnumMap<>(enumClass);
    }
}
