package core.framework.impl.template;

import core.framework.api.util.Lists;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.ExpressionImpl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class ForHandler implements FragmentHandler, CompositeHandler {
    ExpressionImpl expression;
    final List<FragmentHandler> handlers = Lists.newArrayList();
    String variable;
    Class<?> valueClass;

    public ForHandler(String expression, CallTypeStack stack, String reference) {
        Pattern pattern = Pattern.compile("for (\\w+) in (.+)");
        Matcher matcher = pattern.matcher(expression);
        if (!matcher.matches()) throw new Error("not match, exp=" + expression);
        variable = matcher.group(1);
        valueClass = Object.class; //todo: impl this
        String loopTarget = matcher.group(2);
        this.expression = new ExpressionImpl(loopTarget, stack, reference);
    }

    @Override
    public void add(FragmentHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        Object result = expression.eval(stack);
        if (!(result instanceof List))
            throw new Error("expression must be list, expression=" + expression.expression + ", ref=" + expression.reference);
        for (Object item : (List<?>) result) {
            stack.contextObjects.put(variable, item);
            for (FragmentHandler handler : handlers) {
                handler.process(builder, stack);
            }
        }
        stack.contextObjects.remove(variable);
    }
}
