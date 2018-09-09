package core.framework.web;

/**
 * @author neo
 */
public interface WebContext {
    Object get(String key);

    void put(String key, Object value);

    Request request();
}
