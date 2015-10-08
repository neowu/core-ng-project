package core.framework.impl.template.fragment;

import core.framework.api.util.Exceptions;
import core.framework.impl.reflect.GenericTypes;
import core.framework.impl.template.CallStack;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class ForFragment extends CompositeFragment {
    private static final Pattern STATEMENT_PATTERN = Pattern.compile("for ([a-zA-Z1-9]+) in ([#a-zA-Z1-9\\.\\(\\)]+)");

    private final ExpressionHolder expression;
    public final String variable;
    public final Class<?> valueClass;

    public ForFragment(String statement, CallTypeStack stack, String location) {
        Matcher matcher = STATEMENT_PATTERN.matcher(statement);
        if (!matcher.matches())
            throw Exceptions.error("statement must match \"for var in list\", statement={}, location={}", statement, location);

        variable = matcher.group(1);
        String list = matcher.group(2);

        ExpressionBuilder builder = new ExpressionBuilder(list, stack, location);
        expression = builder.build();
        if (!GenericTypes.isGenericList(expression.returnType))
            throw Exceptions.error("for statement must return List<T>, list={}, returnType={}, location={}",
                list, expression.returnType.getTypeName(), location);
        valueClass = GenericTypes.listValueClass(expression.returnType);
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        List<?> list = (List<?>) expression.eval(stack);
        for (Object item : list) {
            stack.contextObjects.put(variable, item);
            for (Fragment handler : handlers) {
                handler.process(builder, stack);
            }
        }
        stack.contextObjects.remove(variable);
    }
}
