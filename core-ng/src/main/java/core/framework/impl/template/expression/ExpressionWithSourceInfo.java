package core.framework.impl.template.expression;

import core.framework.api.util.Strings;
import core.framework.impl.template.CallStack;

/**
 * @author neo
 */
public class ExpressionWithSourceInfo implements Expression {
    private final Expression expression;
    private final String expressionSource;
    private final String location;

    public ExpressionWithSourceInfo(Expression expression, String expressionSource, String location) {
        this.expression = expression;
        this.expressionSource = expressionSource;
        this.location = location;
    }

    @Override
    public Object eval(CallStack stack) {
        try {
            return expression.eval(stack);
        } catch (Throwable e) {
            throw new Error(Strings.format("failed to eval expression, location={}, expression={}, error={}", location, expressionSource, e.getMessage()), e);
        }
    }
}
