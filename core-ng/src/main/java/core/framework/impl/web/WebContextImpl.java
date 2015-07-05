package core.framework.impl.web;

import core.framework.api.web.WebContext;

import java.util.Map;

/**
 * @author neo
 */
public class WebContextImpl implements WebContext {
    final ThreadLocal<Map<String, Object>> context = new ThreadLocal<>();

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) context.get().get(key);
    }
}
