package core.framework.impl.template;

import core.framework.impl.template.function.HTMLFunction;

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
        CallStack stack = new CallStack(value);
        stack.functions.put("html", new HTMLFunction());

        StringBuilder builder = new StringBuilder();
        for (FragmentHandler handler : handlers) {
            handler.process(builder, stack);
        }
        return builder.toString();
    }
}
