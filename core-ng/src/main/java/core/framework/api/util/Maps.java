package core.framework.api.util;

import java.util.HashMap;
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
}
