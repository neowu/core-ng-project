package core.framework.impl.web.controller;

import core.framework.web.Request;
import core.framework.web.WebContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
public class WebContextImpl implements WebContext {
    private static final ThreadLocal<Map<String, Object>> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Request> REQUEST = new ThreadLocal<>();

    @Override
    public Object get(String key) {
        return CONTEXT.get().get(key);
    }

    @Override
    public Request request() {
        return REQUEST.get();
    }

    @Override
    public void put(String key, Object value) {
        CONTEXT.get().put(key, value);
    }

    public void initialize(Request request) {
        CONTEXT.set(new HashMap<>());
        REQUEST.set(request);
    }

    public void cleanup() {
        CONTEXT.remove();
        REQUEST.remove();
    }
}
