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
        boolean changed = false;
        StringBuilder builder = new StringBuilder(text.length() * 2);
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '<':
                    builder.append("&lt;");
                    changed = true;
                    break;
                case '>':
                    builder.append("&gt;");
                    changed = true;
                    break;
                case '"':
                    builder.append("&quot;");
                    changed = true;
                    break;
                case '&':
                    builder.append("&amp;");
                    changed = true;
                    break;
                case '\'':
                    builder.append("&#39;");
                    changed = true;
                    break;
                case '/':
                    builder.append("&#47;");
                    changed = true;
                    break;
                default:
                    builder.append(ch);
                    break;
            }
        }
        return changed ? builder.toString() : text;
    }
    private final ExpressionHolder expression;

    public TextContentFragment(String expression, TemplateMetaContext context, String location) {
        this.expression = new ExpressionBuilder(expression, context, location).build();
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        Object result = expression.eval(context);
        builder.append(escapeHTML(String.valueOf(result)));
    }
}
