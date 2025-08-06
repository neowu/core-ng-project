package core.framework.web;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * @author neo
 */
public interface Session {
    Optional<String> get(String key);

    void set(String key, @Nullable String value); // set value to null to remove key

    void invalidate();
}
