package core.framework.impl.template;

import core.framework.api.util.Maps;
import core.framework.impl.template.function.Function;

import java.util.Map;

/**
 * @author neo
 */
public class CallStack {
    public final Object root;
    public final Map<String, Object> contextObjects = Maps.newHashMap();
    public final Map<String, Function> functions = Maps.newHashMap();

    public CallStack(Object root) {
        this.root = root;
    }

    // used by generated code
    public Object context(String name) {
        return contextObjects.get(name);
    }

    // used by generated code
    public Function function(String name) {
        return functions.get(name);
    }
}
