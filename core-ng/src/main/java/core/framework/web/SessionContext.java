package core.framework.web;

/**
 * @author neo
 */
public interface SessionContext {
    // invalidate all session matches key=value
    void invalidate(String key, String value);
}
