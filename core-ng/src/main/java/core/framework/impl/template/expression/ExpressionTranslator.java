package core.framework.impl.template.expression;

import java.util.regex.Pattern;

/**
 * @author neo
 */
public class ExpressionTranslator {
    private static final Pattern NUMBER = Pattern.compile("\\d+");
    final String expression;
    final CallTypeStack stack;
    StringBuilder result = new StringBuilder();
    StringBuilder currentToken = new StringBuilder();

    public ExpressionTranslator(String expression, CallTypeStack stack) {
        this.expression = expression;
        this.stack = stack;
    }

    public String translate() {
        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if (ch == '(') {
                translateMethod();
                result.append(ch);
            } else if (ch == ',' || ch == ')') {
                translateExpression();
                result.append(ch);
            } else if (ch == '!') {
                result.append(ch);
            } else {
                currentToken.append(ch);
            }
        }

        if (currentToken.length() > 0) translateExpression();

        return result.toString();
    }

    private void translateExpression() {
        String expression = token();
        if (expression.length() == 0) return;

        if (expression.startsWith("\"") || NUMBER.matcher(expression).matches()) {
            result.append(expression);
        } else {
            appendRootVariable(expression);
        }
    }

    private void translateMethod() {
        String method = token();
        if (method.startsWith("#")) {
            result.append("stack.function(\"").append(method.substring(1)).append("\").apply");
        } else {
            appendRootVariable(method);
        }
    }

    private String token() {
        String method = currentToken.toString().trim();
        currentToken = new StringBuilder();
        return method;
    }

    private void appendRootVariable(String expression) {
        String variable = expression;
        int index = expression.indexOf('.');
        if (index > 0) {
            variable = expression.substring(0, index);
        }
        if (!stack.paramClasses.containsKey(variable)) {
            result.append("root.");
        }
        result.append(expression);
    }
}
