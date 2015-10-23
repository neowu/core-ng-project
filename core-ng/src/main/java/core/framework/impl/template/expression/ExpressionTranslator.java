package core.framework.impl.template.expression;

/**
 * @author neo
 */
public class ExpressionTranslator {
    private final Token expression;
    private final CallTypeStack stack;

    public ExpressionTranslator(Token expression, CallTypeStack stack) {
        this.expression = expression;
        this.stack = stack;
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
            if (root && !stack.paramClasses.containsKey(field.name)) {
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
        if (root && !stack.paramClasses.containsKey(method.name)) {
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
