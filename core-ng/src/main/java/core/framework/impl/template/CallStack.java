package core.framework.impl.template;

import core.framework.api.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class CallStack {
    public final Object root;
    public final Map<String, Object> contextObjects = Maps.newHashMap();
    public CDNFunction cdnFunction;
    public MessageFunction messageFunction;

    public CallStack(Object root) {
        this.root = root;
    }

    // used by generated code
    public Object context(String name) {
        return contextObjects.get(name);
    }

    public String cdn(String url) {
        if (cdnFunction == null) throw new Error("cdn is not in context");
        return cdnFunction.url(url);
    }

    public String message(String key) {
        if (messageFunction == null) throw new Error("message is not in context");
        return messageFunction.message(key);
    }
}
