package core.framework.impl.template.expression;

import core.framework.impl.template.TemplateContext;
import core.framework.util.Exceptions;

import java.lang.reflect.Type;

/**
 * @author neo
 */
public class ExpressionHolder {
    public final Type returnType;
    private final Expression expression;
    private final String expressionSource;
    private final String location;

    public ExpressionHolder(Expression expression, Type returnType, String expressionSource, String location) {
        this.expression = expression;
        this.returnType = returnType;
        this.expressionSource = expressionSource;
        this.location = location;
    }

    public Object eval(TemplateContext context) {
        try {
            return expression.eval(context);
        } catch (Throwable e) {
            throw Exceptions.error("failed to eval expression, location={}, expression={}, error={}", location, expressionSource, e.getMessage(), e);
        }
    }
}
