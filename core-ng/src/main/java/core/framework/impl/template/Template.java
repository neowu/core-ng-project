package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import core.framework.impl.template.function.Function;
import core.framework.impl.template.function.HTMLFunction;

import java.util.Map;

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

    public String process(Object model, Map<String, Function> customFunctions) {
        if (model == null)
            throw Exceptions.error("model must not be null");

        if (!modelClass.isInstance(model))
            throw Exceptions.error("model class does not match, expectedClass={}, actualClass={}", modelClass.getCanonicalName(), model.getClass().getCanonicalName());

        CallStack stack = new CallStack(model);
        stack.functions.put("html", new HTMLFunction());
        if (customFunctions != null)
            stack.functions.putAll(customFunctions);

        StringBuilder builder = new StringBuilder();
        for (FragmentHandler handler : this.handler.handlers) {
            handler.process(builder, stack);
        }
        return builder.toString();
    }
}
