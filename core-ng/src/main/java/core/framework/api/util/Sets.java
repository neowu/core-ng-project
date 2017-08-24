package core.framework.api.util;

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

    @SafeVarargs
    public static <T> Set<T> newHashSet(T... values) {
        Set<T> set = new HashSet<>();
        for (T value : values) {
            set.add(value);
        }
        return set;
    }
}
