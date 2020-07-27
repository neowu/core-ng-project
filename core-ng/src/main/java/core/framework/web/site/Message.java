package core.framework.web.site;

import javax.annotation.Nullable;

/**
 * @author neo
 */
public interface Message {
    String get(String key, @Nullable String language);

    default String get(String key) {
        return get(key, null);
    }
}
