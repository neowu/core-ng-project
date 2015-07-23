package core.framework.impl.template.fragment;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Strings;
import core.framework.impl.codegen.CodeCompileException;
import core.framework.impl.template.CallStack;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.Expression;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionParser;
import core.framework.impl.template.expression.ExpressionTypeInspector;
import core.framework.impl.template.expression.Token;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class ForFragment extends CompositeFragment {
    private static final Pattern STATEMENT_PATTERN = Pattern.compile("for ([a-zA-Z1-9]+) in ([#a-zA-Z1-9\\.\\(\\)]+)");

    private final Expression expression;
    public final String variable;
    public final Class<?> valueClass;

    public ForFragment(String statement, CallTypeStack stack, String location) {
        Matcher matcher = STATEMENT_PATTERN.matcher(statement);
        if (!matcher.matches())
            throw Exceptions.error("statement must match \"for var in list\", statement={}, location={}", statement, location);

        variable = matcher.group(1);
        String list = matcher.group(2);

        try {
            Token expression = new ExpressionParser().parse(list);
            valueClass = new ExpressionTypeInspector().listValueClass(expression, stack.rootClass, list);
            this.expression = new ExpressionBuilder().build(expression, stack, List.class);
        } catch (CodeCompileException e) {
            throw new Error(Strings.format("failed to compile expression, statement={}, location={}", statement, location), e);
        }
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
