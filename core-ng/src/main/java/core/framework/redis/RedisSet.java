package core.framework.redis;

import java.util.Set;

/**
 * @author neo
 */
public interface RedisSet {
    boolean add(String key, String... values);

    Set<String> members(String key);

    boolean isMember(String key, String value);

    boolean remove(String key, String... values);
}
