package core.framework.impl.template.fragment;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Strings;
import core.framework.impl.codegen.CodeCompileException;
import core.framework.impl.template.CallStack;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.Expression;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionParser;
import core.framework.impl.template.expression.Token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class IfFragment extends CompositeFragment {
    private static final Pattern STATEMENT_PATTERN = Pattern.compile("if ((not )?)([#a-zA-Z1-9\\.\\(\\)]+)");
    final Expression expression;
    final boolean reverse;

    public IfFragment(String statement, CallTypeStack stack, String location) {
        Matcher matcher = STATEMENT_PATTERN.matcher(statement);
        if (!matcher.matches())
            throw Exceptions.error("statement must match \"if (not) condition\", statement={}, location={}", statement, location);

        reverse = "not ".equals(matcher.group(2));
        String condition = matcher.group(3);

        try {
            Token expression = new ExpressionParser().parse(condition);
            this.expression = new ExpressionBuilder().build(expression, stack, Boolean.class);
        } catch (CodeCompileException e) {
            throw new Error(Strings.format("failed to compile expression, statement={}, location={}", statement, location), e);
        }
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        Boolean result = (Boolean) expression.eval(stack);
        Boolean expected = reverse ? Boolean.FALSE : Boolean.TRUE;
        if (expected.equals(result)) {
            for (Fragment handler : handlers) {
                handler.process(builder, stack);
            }
        }
    }
}
