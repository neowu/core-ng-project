package core.framework.impl.template;

import core.framework.api.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class TemplateContext {
    public final Object root;
    public final Map<String, Object> contextObjects = Maps.newHashMap();
    public CDNFunction cdn;
    public MessageFunction message;

    public TemplateContext(Object root) {
        this.root = root;
    }

    // used by generated code
    public Object context(String name) {
        return contextObjects.get(name);
    }
}
