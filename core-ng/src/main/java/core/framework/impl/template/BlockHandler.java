package core.framework.impl.template;

import core.framework.api.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public class BlockHandler implements FragmentHandler {
    final String expression;
    final List<FragmentHandler> handlers = Lists.newArrayList();

    public BlockHandler(String expression) {
        this.expression = expression;
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        Object value = context.eval(expression);
        if (value instanceof Iterable) {
            for (Object v : (Iterable) value) {
                context.push(v);
                for (FragmentHandler handler : handlers) {
                    handler.process(builder, context);
                }
                context.pop();
            }
        } else if (value != null) {
            for (FragmentHandler handler : handlers) {
                handler.process(builder, context);
            }
        }
    }
}
