package core.framework.impl.template;

import core.framework.api.util.Lists;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.Expression;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionParser;
import core.framework.impl.template.expression.Token;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class ForHandler implements FragmentHandler, CompositeHandler {
    Expression expression;
    final List<FragmentHandler> handlers = Lists.newArrayList();
    String variable;
    Class<?> valueClass;

    public ForHandler(String statement, CallTypeStack stack, String reference) {
        Pattern pattern = Pattern.compile("for (\\w+) in (.+)");
        Matcher matcher = pattern.matcher(statement);
        if (!matcher.matches()) throw new Error("not match, exp=" + statement);
        variable = matcher.group(1);
        valueClass = Object.class; //todo: impl this
        String list = matcher.group(2);
        Token expression = new ExpressionParser().parse(list);
        this.expression = new ExpressionBuilder().build(expression, stack, List.class);
    }

    @Override
    public void add(FragmentHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        List<?> list = (List<?>) expression.eval(stack);
        for (Object item : list) {
            stack.contextObjects.put(variable, item);
            for (FragmentHandler handler : handlers) {
                handler.process(builder, stack);
            }
        }
        stack.contextObjects.remove(variable);
    }
}
