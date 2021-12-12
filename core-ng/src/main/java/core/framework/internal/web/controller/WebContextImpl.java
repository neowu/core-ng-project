package core.framework.internal.web.controller;

import core.framework.web.CookieSpec;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.WebContext;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
public class WebContextImpl implements WebContext {
    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    @Override
    public Object get(String key) {
        Context context = CONTEXT.get();
        if (context.context == null) return null;
        return context.context.get(key);
    }

    @Override
    public void put(String key, Object value) {
        Context context = CONTEXT.get();
        if (context.context == null) context.context = new HashMap<>();
        context.context.put(key, value);
    }

    @Override
    public Request request() {
        return CONTEXT.get().request;
    }

    @Override
    public void responseCookie(CookieSpec spec, @Nullable String value) {
        Context context = CONTEXT.get();
        if (context.responseCookies == null) context.responseCookies = new HashMap<>();
        context.responseCookies.put(spec, value);
    }

    public void initialize(Request request) {
        CONTEXT.set(new Context(request));
    }

    public void handleResponse(Response response) {
        Context context = CONTEXT.get();
        if (context.responseCookies != null) {
            for (Map.Entry<CookieSpec, String> entry : context.responseCookies.entrySet()) {
                response.cookie(entry.getKey(), entry.getValue());
            }
        }
    }

    public void cleanup() {
        CONTEXT.remove();
    }

    static class Context {
        final Request request;
        Map<String, Object> context;
        Map<CookieSpec, String> responseCookies;

        Context(Request request) {
            this.request = request;
        }
    }
}
