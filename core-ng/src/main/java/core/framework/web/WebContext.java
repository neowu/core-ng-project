package core.framework.web;

/**
 * @author neo
 */
public interface WebContext {
    Object get(String key);     // context is used to pass value from interceptor to controller/ws, key will be static and deterministic, that's why here is designed to return Object, not Optional<T>

    void put(String key, Object value);

    Request request();
}
