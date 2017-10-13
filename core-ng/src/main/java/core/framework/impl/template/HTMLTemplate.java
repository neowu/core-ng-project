package core.framework.impl.template;

import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.util.Exceptions;

/**
 * @author neo
 */
public class HTMLTemplate extends ContainerFragment {
    private final Class<?> modelClass;

    HTMLTemplate(Class<?> modelClass) {
        this.modelClass = modelClass;
    }

    public String process(TemplateContext context) {
        if (context.root == null)
            throw Exceptions.error("root must not be null");

        if (!modelClass.isInstance(context.root))
            throw Exceptions.error("model class does not match, expectedClass={}, actualClass={}", modelClass.getCanonicalName(), context.root.getClass().getCanonicalName());

        StringBuilder builder = new StringBuilder(2048);
        process(builder, context);
        return builder.toString();
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        processChildren(builder, context);
    }
}
