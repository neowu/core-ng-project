package core.framework.impl.web.controller;

import core.framework.util.Maps;
import core.framework.web.Request;
import core.framework.web.WebContext;

import java.util.Map;

/**
 * @author neo
 */
public class WebContextImpl implements WebContext {
    private final ThreadLocal<Map<String, Object>> context = new ThreadLocal<>();
    private final ThreadLocal<Request> request = new ThreadLocal<>();

    @SuppressWarnings("unchecked")
    @Override
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

    public void initialize(Request request) {
        context.set(Maps.newHashMap());
        this.request.set(request);
    }

    public void cleanup() {
        context.remove();
        request.remove();
    }
}
