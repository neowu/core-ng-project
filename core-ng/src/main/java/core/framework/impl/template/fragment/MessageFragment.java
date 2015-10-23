package core.framework.impl.template.fragment;

import core.framework.impl.template.CallStack;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;

/**
 * @author neo
 */
public class MessageFragment implements Fragment {
    private final ExpressionHolder expression;

    public MessageFragment(String expression, CallTypeStack stack, String location) {
        this.expression = new ExpressionBuilder(expression, stack, location).build();
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        Object result = expression.eval(stack);
        builder.append(stack.message(String.valueOf(result)));
    }
}
