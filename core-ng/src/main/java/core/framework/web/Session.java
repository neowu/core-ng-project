package core.framework.web;

import java.util.Optional;

/**
 * @author neo
 */
public interface Session {
    Optional<String> get(String key);

    void set(String key, String value);

    void remove(String key);

    void invalidate();
}
