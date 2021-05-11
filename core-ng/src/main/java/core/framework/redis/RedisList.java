package core.framework.redis;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author rexthk
 */
public interface RedisList {
    @Nullable
    default String pop(String key) {
        List<String> list = pop(key, 1);
        return list.isEmpty() ? null : list.get(0);
    }

    List<String> pop(String key, int size);

    long push(String key, String... values);

    default List<String> range(String key) {
        return range(key, 0, -1);
    }

    List<String> range(String key, long start, long stop);
}
