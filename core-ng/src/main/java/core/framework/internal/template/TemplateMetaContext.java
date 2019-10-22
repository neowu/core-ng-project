package core.framework.internal.template;

import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class TemplateMetaContext {
    public final Class<?> rootClass;
    public final Map<String, Class<?>> paramClasses = Maps.newHashMap();
    public CDNManager cdn;
    public MessageProvider message;

    public TemplateMetaContext(Class<?> rootClass) {
        this.rootClass = rootClass;
    }
}
