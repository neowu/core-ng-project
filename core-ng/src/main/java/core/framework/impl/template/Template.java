package core.framework.impl.template;

import java.util.List;

/**
 * @author neo
 */
public class Template {
    private final List<FragmentHandler> handlers;

    public Template(List<FragmentHandler> handlers) {
        this.handlers = handlers;
    }

    public String process(Object value) {
        TemplateContext context = new TemplateContext(value);
        StringBuilder builder = new StringBuilder();
        for (FragmentHandler handler : handlers) {
            handler.process(builder, context);
        }
        return builder.toString();
    }
}
