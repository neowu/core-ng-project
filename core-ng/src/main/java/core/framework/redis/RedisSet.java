package core.framework.redis;

import java.util.Set;

/**
 * @author neo
 */
public interface RedisSet {
    long add(String key, String... values);

    Set<String> members(String key);

    boolean isMember(String key, String value);

    long remove(String key, String... values);
}
