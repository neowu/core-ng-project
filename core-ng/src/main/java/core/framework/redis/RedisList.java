package core.framework.redis;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author rexthk
 */
public interface RedisList {
    @Nullable
    default String pop(String key) {
        List<String> values = pop(key, 1);
        return values.isEmpty() ? null : values.get(0);
    }

    List<String> pop(String key, int size);

    // return the length of the list after the push operation
    long push(String key, String... values);

    default List<String> range(String key) {
        return range(key, 0, -1);
    }

    List<String> range(String key, long start, long stop);

    // trim the list, only keep most recent items
    void trim(String key, int maxSize);
}
