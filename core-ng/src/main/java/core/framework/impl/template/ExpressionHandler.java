package core.framework.impl.template;

import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.Expression;
import core.framework.impl.template.expression.ExpressionImpl;

/**
 * @author neo
 */
public class ExpressionHandler implements FragmentHandler {
    final Expression expression;

    public ExpressionHandler(String expression, CallTypeStack stack, String reference) {
        this.expression = new ExpressionImpl(expression, stack, reference);
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        Object result = expression.eval(stack);
        builder.append(String.valueOf(result));
    }
}
