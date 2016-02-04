package core.framework.impl.template.fragment;

import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;

/**
 * @author neo
 */
public class TextContentFragment implements Fragment {
    static String escapeHTML(String text) {
        int length = text.length();
        int index = findHTMLReservedChar(text);
        if (index == length) return text;
        StringBuilder builder = new StringBuilder(length * 2);
        for (int i = 0; i < index; i++) builder.append(text.charAt(i));
        for (; index < length; index++) {
            char ch = text.charAt(index);
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

    private static int findHTMLReservedChar(String text) {
        int length = text.length();
        int index = 0;
        for (; index < length; index++) {
            char ch = text.charAt(index);
            switch (ch) {
                case '<':
                    return index;
                case '>':
                    return index;
                case '"':
                    return index;
                case '&':
                    return index;
                case '\'':
                    return index;
                case '/':
                    return index;
                default:
                    break;
            }
        }
        return index;
    }

    private final ExpressionHolder expression;

    public TextContentFragment(String expression, TemplateMetaContext context, String location) {
        this.expression = new ExpressionBuilder(expression, context, location).build();
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        Object result = expression.eval(context);
        if (result != null) {
            builder.append(escapeHTML(String.valueOf(result)));
        }
    }
}
