package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import core.framework.impl.template.function.HTMLFunction;

/**
 * @author neo
 */
public class Template {
    private final Class<?> modelClass;
    private final CompositeHandler handler;

    public Template(Class<?> modelClass, CompositeHandler handler) {
        this.modelClass = modelClass;
        this.handler = handler;
    }

    public String process(Object value) {
        if (value == null)
            throw Exceptions.error("value must not be null");

        if (!modelClass.isInstance(value))
            throw Exceptions.error("model class does not match, modelClass={}, valueClass={}", modelClass.getCanonicalName(), value.getClass().getCanonicalName());

        CallStack stack = new CallStack(value);
        stack.functions.put("html", new HTMLFunction());

        StringBuilder builder = new StringBuilder();
        for (FragmentHandler handler : this.handler.handlers) {
            handler.process(builder, stack);
        }
        return builder.toString();
    }
}
