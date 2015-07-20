package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import core.framework.impl.codegen.CodeCompileException;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.Expression;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionParser;
import core.framework.impl.template.expression.HTMLText;
import core.framework.impl.template.expression.Token;

/**
 * @author neo
 */
public class ExpressionHandler implements FragmentHandler {
    final Expression expression;

    public ExpressionHandler(String expression, CallTypeStack stack, String reference) {
        try {
            Token token = new ExpressionParser().parse(expression);
            this.expression = new ExpressionBuilder().build(token, stack, Object.class);
        } catch (CodeCompileException e) {
            throw Exceptions.error("failed to compile expression, expression={}, ref={}", expression, reference);
        }
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        Object result = expression.eval(stack);
        if (result instanceof HTMLText) {
            builder.append(((HTMLText) result).html);
        } else if (result instanceof String) {
            builder.append(result);   //TODO: escape html
        } else {
            builder.append(String.valueOf(result));
        }
    }
}
