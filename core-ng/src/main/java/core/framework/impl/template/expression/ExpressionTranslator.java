package core.framework.impl.template.expression;

import core.framework.impl.template.TemplateMetaContext;

/**
 * @author neo
 */
public class ExpressionTranslator {
    private final Token expression;
    private final TemplateMetaContext context;

    public ExpressionTranslator(Token expression, TemplateMetaContext context) {
        this.expression = expression;
        this.context = context;
    }

    public String translate() {
        if (expression instanceof ValueToken) return ((ValueToken) expression).value;

        StringBuilder builder = new StringBuilder();
        append(builder, expression, true);
        return builder.toString();
    }

    private void append(StringBuilder builder, Token token, boolean root) {
        if (token instanceof FieldToken) {
            FieldToken field = (FieldToken) token;
            if (root && !context.paramClasses.containsKey(field.name)) {
                builder.append("$root.");
            }
            builder.append(field.name);
            if (field.next != null) {
                builder.append('.');
                append(builder, field.next, false);
            }
        } else if (token instanceof MethodToken) {
            appendMethod(builder, (MethodToken) token, root);
        } else if (token instanceof ValueToken) {
            builder.append(((ValueToken) token).value);
        }
    }

    private void appendMethod(StringBuilder builder, MethodToken method, boolean root) {
        if (root && !context.paramClasses.containsKey(method.name)) {
            builder.append("$root.");
        }
        builder.append(method.name).append('(');

        int index = 0;
        for (Token param : method.params) {
            if (index > 0) builder.append(',');
            append(builder, param, true);
            index++;
        }
        builder.append(')');
        if (method.next != null) {
            builder.append('.');
            append(builder, method.next, false);
        }
    }
}
