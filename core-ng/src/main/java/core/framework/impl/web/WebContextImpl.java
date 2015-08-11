package core.framework.impl.web;

import core.framework.api.util.Maps;
import core.framework.api.web.Request;
import core.framework.api.web.WebContext;

import java.util.Map;

/**
 * @author neo
 */
public class WebContextImpl implements WebContext {
    private final ThreadLocal<Map<String, Object>> context = new ThreadLocal<>();
    private final ThreadLocal<Request> request = new ThreadLocal<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) context.get().get(key);
    }

    @Override
    public Request request() {
        return request.get();
    }

    @Override
    public <T> void put(String key, T value) {
        context.get().put(key, value);
    }

    void initialize(Request request) {
        context.set(Maps.newHashMap());
        this.request.set(request);
    }

    void cleanup() {
        context.remove();
        request.remove();
    }
}
