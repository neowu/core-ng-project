package core.framework.util;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author neo
 */
public final class Sets {
    public static <T> Set<T> newHashSet() {
        return new HashSet<>();
    }

    public static <T> Set<T> newConcurrentHashSet() {
        return ConcurrentHashMap.newKeySet();
    }

    // refer to guava maps impl, init capacity with load factor 0.75
    // https://github.com/google/guava/blob/master/guava/src/com/google/common/collect/Maps.java
    public static <T> Set<T> newHashSetWithExpectedSize(int size) {
        int capacity;
        if (size < 3) {
            capacity = size + 1;
        } else {
            capacity = (int) (size / 0.75f + 1);
        }
        return new HashSet<>(capacity);
    }

    // EnumSet provides EnumSet.of(values...) methods, use it directly when apply
    public static <T extends Enum<T>> Set<T> newEnumSet(Class<T> enumClass) {
        return EnumSet.noneOf(enumClass);
    }
}
