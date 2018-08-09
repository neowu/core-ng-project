package core.framework.redis;

import java.util.List;

/**
 * @author rexthk
 */
public interface RedisList {
    String pop(String key);

    void push(String key, String value);

    List<String> getAll(String key);
}
