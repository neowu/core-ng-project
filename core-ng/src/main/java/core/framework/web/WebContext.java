package core.framework.web;

import javax.annotation.Nullable;

/**
 * @author neo
 */
public interface WebContext {
    Object get(String key);     // context is used to pass value from interceptor to controller/ws, key will be static and deterministic, that's why here is designed to return Object, not Optional<T>

    void put(String key, Object value);

    Request request();

    // to let ws assign cookie to response
    void responseCookie(CookieSpec spec, @Nullable String value);
}
