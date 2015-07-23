package core.framework.impl.template.fragment;

import core.framework.api.util.Strings;
import core.framework.impl.codegen.CodeCompileException;
import core.framework.impl.template.CallStack;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.Expression;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionParser;
import core.framework.impl.template.expression.Token;
import core.framework.impl.template.function.HTMLText;

/**
 * @author neo
 */
public class ExpressionFragment implements Fragment {
    final Expression expression;

    public ExpressionFragment(String expression, CallTypeStack stack, String location) {
        try {
            Token token = new ExpressionParser().parse(expression);
            this.expression = new ExpressionBuilder().build(token, stack, Object.class);
        } catch (CodeCompileException e) {
            throw new Error(Strings.format("failed to compile expression, expression={}, location={}", expression, location), e);
        }
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        Object result = expression.eval(stack);
        if (result instanceof HTMLText) {
            builder.append(((HTMLText) result).html);
        } else if (result instanceof String) {
            builder.append(escapeHTML((String) result));
        } else {
            builder.append(String.valueOf(result));
        }
    }

    private String escapeHTML(String text) {
        StringBuilder builder = new StringBuilder(text.length() * 2);
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '&':
                    builder.append("&amp;");
                    break;
                case '\'':
                    builder.append("&#39;");
                    break;
                default:
                    builder.append(ch);
                    break;
            }
        }
        return builder.toString();
    }
}
