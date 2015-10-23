package core.framework.impl.template.fragment;

import core.framework.impl.template.CallStack;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;

/**
 * @author neo
 */
public class TextContentFragment implements Fragment {
    private final ExpressionHolder expression;

    public TextContentFragment(String expression, CallTypeStack stack, String location) {
        this.expression = new ExpressionBuilder(expression, stack, location).build();
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        Object result = expression.eval(stack);
        builder.append(escapeHTML(String.valueOf(result)));
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
                case '/':
                    builder.append("&#47;");
                    break;
                default:
                    builder.append(ch);
                    break;
            }
        }
        return builder.toString();
    }
}
