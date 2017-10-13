package core.framework.web.site;

import java.util.Optional;

/**
 * @author neo
 */
public interface Message {
    Optional<String> get(String key, String language);

    default Optional<String> get(String key) {
        return get(key, null);
    }
}
