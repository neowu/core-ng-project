package core.framework.api.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author neo
 */
public final class Sets {
    public static <T> Set<T> newHashSet() {
        return new HashSet<>();
    }

    @SafeVarargs
    public static <T> Set<T> newHashSet(T... values) {
        HashSet<T> set = new HashSet<>();
        Collections.addAll(set, values);
        return set;
    }
}
