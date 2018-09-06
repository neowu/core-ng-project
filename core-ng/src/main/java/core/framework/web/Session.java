package core.framework.web;

import java.util.Optional;

/**
 * @author neo
 */
public interface Session {
    Optional<String> get(String key);

    void set(String key, String value); // set value to null to remove key

    void invalidate();
}
