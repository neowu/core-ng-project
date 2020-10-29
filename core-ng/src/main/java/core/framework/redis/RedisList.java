package core.framework.redis;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author rexthk
 */
public interface RedisList {
    @Nullable
    String pop(String key);

    long push(String key, String... values);

    default List<String> range(String key) {
        return range(key, 0, -1);
    }

    List<String> range(String key, long start, long stop);
}
