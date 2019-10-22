package core.framework.internal.template;

import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class TemplateContext {
    public final Object root;
    public final Map<String, Object> contextObjects = Maps.newHashMap();
    public final CDNManager cdnManager;

    public TemplateContext(Object root, CDNManager cdnManager) {
        this.root = root;
        this.cdnManager = cdnManager;
    }

    // used by generated code
    public Object context(String name) {
        return contextObjects.get(name);
    }
}
