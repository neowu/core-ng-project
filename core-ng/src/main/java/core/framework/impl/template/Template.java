package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import core.framework.impl.template.fragment.CompositeFragment;
import core.framework.impl.template.fragment.Fragment;

/**
 * @author neo
 */
public class Template extends CompositeFragment {
    private final Class<?> modelClass;

    public Template(Class<?> modelClass) {
        this.modelClass = modelClass;
    }

    public String process(CallStack stack) {
        if (stack.root == null)
            throw Exceptions.error("root must not be null");

        if (!modelClass.isInstance(stack.root))
            throw Exceptions.error("model class does not match, expectedClass={}, actualClass={}", modelClass.getCanonicalName(), stack.root.getClass().getCanonicalName());

        StringBuilder builder = new StringBuilder();
        process(builder, stack);
        return builder.toString();
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        for (Fragment handler : this.fragments) {
            handler.process(builder, stack);
        }
    }
}
