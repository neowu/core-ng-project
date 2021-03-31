package core.framework.internal.template.fragment;

import core.framework.internal.template.TemplateContext;
import core.framework.internal.template.TemplateMetaContext;
import core.framework.internal.template.expression.ExpressionBuilder;
import core.framework.internal.template.expression.ExpressionHolder;

/**
 * @author neo
 */
public class TextContentFragment implements Fragment {
    static String escapeHTML(String text) {
        int length = text.length();
        int index = findHTMLReservedChar(text);
        if (index == length) return text;
        var builder = new StringBuilder(length * 2);
        for (int i = 0; i < index; i++) builder.append(text.charAt(i));
        for (; index < length; index++) {
            char ch = text.charAt(index);
            switch (ch) {
                case '<' -> builder.append("&lt;");
                case '>' -> builder.append("&gt;");
                case '"' -> builder.append("&quot;");
                case '&' -> builder.append("&amp;");
                case '\'' -> builder.append("&#39;");
                case '/' -> builder.append("&#47;");
                default -> builder.append(ch);
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
                case '>':
                case '"':
                case '&':
                case '\'':
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
