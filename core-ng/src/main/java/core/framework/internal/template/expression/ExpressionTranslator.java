package core.framework.internal.template.expression;

import core.framework.internal.template.TemplateMetaContext;

/**
 * @author neo
 */
class ExpressionTranslator {
    private final Token expression;
    private final TemplateMetaContext context;

    ExpressionTranslator(Token expression, TemplateMetaContext context) {
        this.expression = expression;
        this.context = context;
    }

    public String translate() {
        if (expression instanceof ValueToken token) return token.value;

        var builder = new StringBuilder();
        append(builder, expression, true);
        return builder.toString();
    }

    private void append(StringBuilder builder, Token token, boolean root) {
        if (token instanceof FieldToken fieldToken) {
            if (root && !context.paramClasses.containsKey(fieldToken.name)) {
                builder.append("$root.");
            }
            builder.append(fieldToken.name);
            if (fieldToken.next != null) {
                builder.append('.');
                append(builder, fieldToken.next, false);
            }
        } else if (token instanceof final MethodToken methodToken) {
            appendMethod(builder, methodToken, root);
        } else if (token instanceof final ValueToken valueToken) {
            builder.append(valueToken.value);
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
