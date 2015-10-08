package core.framework.impl.template.expression;

import core.framework.api.util.Strings;
import core.framework.impl.template.CallStack;

import java.lang.reflect.Type;

/**
 * @author neo
 */
public class ExpressionHolder {
    private final Expression expression;
    public final Type returnType;
    private final String expressionSource;
    private final String location;

    public ExpressionHolder(Expression expression, Type returnType, String expressionSource, String location) {
        this.expression = expression;
        this.returnType = returnType;
        this.expressionSource = expressionSource;
        this.location = location;
    }

    public Object eval(CallStack stack) {
        try {
            return expression.eval(stack);
        } catch (Throwable e) {
            throw new Error(Strings.format("failed to eval expression, location={}, expression={}, error={}",
                location, expressionSource, e.getMessage()), e);
        }
    }
}
