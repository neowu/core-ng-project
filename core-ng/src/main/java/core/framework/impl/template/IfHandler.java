package core.framework.impl.template;

import core.framework.api.util.Lists;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.ExpressionImpl;

import java.util.List;

/**
 * @author neo
 */
public class IfHandler implements FragmentHandler, CompositeHandler {
    ExpressionImpl expression;
    final List<FragmentHandler> handlers = Lists.newArrayList();

    public IfHandler(String expression, CallTypeStack stack, String reference) {
        int index = expression.indexOf("if ");
        String condition = expression.substring(index + 3);
        this.expression = new ExpressionImpl(condition, stack, reference);
    }

    @Override
    public void add(FragmentHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        Object result = expression.eval(stack);
        if (!(result instanceof Boolean))
            throw new Error("condition must be boolean expression, expression=" + expression.expression + ", ref=" + expression.reference);
        if (Boolean.TRUE.equals(result)) {
            for (FragmentHandler handler : handlers) {
                handler.process(builder, stack);
            }
        }
    }
}
