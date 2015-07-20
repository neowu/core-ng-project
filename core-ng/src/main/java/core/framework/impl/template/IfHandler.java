package core.framework.impl.template;

import core.framework.api.util.Lists;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.Expression;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionParser;
import core.framework.impl.template.expression.Token;

import java.util.List;

/**
 * @author neo
 */
public class IfHandler implements FragmentHandler, CompositeHandler {
    final Expression expression;
    final List<FragmentHandler> handlers = Lists.newArrayList();

    public IfHandler(String statement, CallTypeStack stack, String reference) {
        int index = statement.indexOf("if ");
        String condition = statement.substring(index + 3);

        Token expression = new ExpressionParser().parse(condition);
        this.expression = new ExpressionBuilder().build(expression, stack, Boolean.class);
    }

    @Override
    public void add(FragmentHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        Boolean result = (Boolean) expression.eval(stack);
        if (Boolean.TRUE.equals(result)) {
            for (FragmentHandler handler : handlers) {
                handler.process(builder, stack);
            }
        }
    }
}
