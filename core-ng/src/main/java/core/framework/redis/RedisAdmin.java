package core.framework.redis;

import java.util.Map;

/**
 * @author neo
 */
public interface RedisAdmin {
    Map<String, String> info();
}
