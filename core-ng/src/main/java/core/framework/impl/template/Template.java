package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import core.framework.impl.template.fragment.CompositeFragment;
import core.framework.impl.template.fragment.Fragment;
import core.framework.impl.template.function.Function;
import core.framework.impl.template.function.HTMLFunction;
import core.framework.impl.template.function.IfFunction;

import java.util.Map;

/**
 * @author neo
 */
public class Template extends CompositeFragment {
    private final Class<?> modelClass;

    public Template(Class<?> modelClass) {
        this.modelClass = modelClass;
    }

    public String process(Object model, Map<String, Function> customFunctions) {
        if (model == null)
            throw Exceptions.error("model must not be null");

        if (!modelClass.isInstance(model))
            throw Exceptions.error("model class does not match, expectedClass={}, actualClass={}", modelClass.getCanonicalName(), model.getClass().getCanonicalName());

        CallStack stack = new CallStack(model);
        addBuiltInFunctions(stack);
        if (customFunctions != null)
            stack.functions.putAll(customFunctions);

        StringBuilder builder = new StringBuilder();
        process(builder, stack);
        return builder.toString();
    }

    private void addBuiltInFunctions(CallStack stack) {
        stack.functions.put("html", new HTMLFunction());
        stack.functions.put("if", new IfFunction());
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        for (Fragment handler : this.handlers) {
            handler.process(builder, stack);
        }
    }
}
