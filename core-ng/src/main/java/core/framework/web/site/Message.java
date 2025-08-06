package core.framework.web.site;

import org.jspecify.annotations.Nullable;

/**
 * @author neo
 */
public interface Message {
    String get(String key, @Nullable String language);

    default String get(String key) {
        return get(key, null);
    }
}
