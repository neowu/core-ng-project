package core.framework.impl.template;

import core.framework.api.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class TemplateMetaContext {
    public final Class<?> rootClass;
    public final Map<String, Class<?>> paramClasses = Maps.newHashMap();
    public CDNFunction cdn;
    public MessageFunction message;

    public TemplateMetaContext(Class<?> rootClass) {
        this.rootClass = rootClass;
    }
}
