package core.framework.redis;

import java.util.Map;

/**
 * @author neo
 */
public interface RedisHash {
    String get(String key, String field);

    Map<String, String> getAll(String key);

    void set(String key, String field, String value);

    void multiSet(String key, Map<String, String> values);

    long del(String key, String... fields);
}
