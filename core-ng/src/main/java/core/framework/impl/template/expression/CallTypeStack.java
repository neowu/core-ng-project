package core.framework.impl.template.expression;

import core.framework.api.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class CallTypeStack {
    public final Class<?> rootClass;
    public final Map<String, Class<?>> paramClasses = Maps.newHashMap();

    public CallTypeStack(Class<?> rootClass) {
        this.rootClass = rootClass;
    }
}
