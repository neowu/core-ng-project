package core.framework.impl.template.expression;

/**
 * @author neo
 */
public class ExpressionTranslator {
    public String translate(Token expression, CallTypeStack stack) {
        if (expression instanceof ValueToken) return ((ValueToken) expression).value;

        StringBuilder builder = new StringBuilder();
        append(builder, expression, stack, true);
        return builder.toString();
    }

    private void append(StringBuilder builder, Token token, CallTypeStack stack, boolean root) {
        if (token instanceof FieldToken) {
            FieldToken field = (FieldToken) token;
            if (root && !stack.paramClasses.containsKey(field.name)) {
                builder.append("$root.");
            }
            builder.append(field.name);
            if (field.next != null) {
                builder.append('.');
                append(builder, field.next, stack, false);
            }
        } else if (token instanceof MethodToken) {
            MethodToken method = (MethodToken) token;
            if (method.builtinMethod) {
                builder.append("stack.function(\"").append(method.name.substring(1)).append("\").apply(");
            } else {
                if (root && !stack.paramClasses.containsKey(method.name)) {
                    builder.append("$root.");
                }
                builder.append(method.name).append('(');
            }
            if (!method.params.isEmpty() && method.builtinMethod) builder.append("new Object[]{");
            int index = 0;
            for (Token param : method.params) {
                if (index > 0) builder.append(",");
                append(builder, param, stack, true);
                index++;
            }
            if (!method.params.isEmpty() && method.builtinMethod) builder.append("}");
            builder.append(')');
            if (method.next != null) {
                builder.append('.');
                append(builder, method.next, stack, false);
            }
        } else if (token instanceof ValueToken) {
            builder.append(((ValueToken) token).value);
        }
    }
}
