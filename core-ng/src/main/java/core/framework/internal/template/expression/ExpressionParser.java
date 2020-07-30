package core.framework.internal.template.expression;

import java.util.regex.Pattern;

/**
 * @author neo
 */
class ExpressionParser {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[\\d\\.]+");
    private static final Pattern METHOD_PATTERN = Pattern.compile("[a-z][a-zA-Z\\d]*");
    private static final Pattern FIELD_PATTERN = Pattern.compile("[a-z][a-zA-Z\\d]*");

    public Token parse(String expression) {
        int length = expression.length();
        char firstChar = expression.charAt(0);
        if (firstChar == '"' || firstChar == '\'') {
            if (length <= 1 || expression.charAt(length - 1) != firstChar)
                throw new Error("\" or \' is not closed, expression=" + expression);
            return new ValueToken("\"" + expression.substring(1, length - 1) + "\"", String.class);
        }

        for (int i = 0; i < length; i++) {
            char ch = expression.charAt(i);
            if (ch == '(') {
                return parseMethod(expression, i);
            } else if (ch == '.') {
                if (i == length - 1) {
                    throw new Error("expression must not end with '.'");
                }
                String field = expression.substring(0, i);
                if (NUMBER_PATTERN.matcher(field).matches()) continue;

                if (!FIELD_PATTERN.matcher(field).matches())
                    throw new Error("invalid field name, field=" + field);
                var token = new FieldToken(field);
                token.next = parse(expression.substring(i + 1));
                if (token.next instanceof ValueToken)
                    throw new Error("value token must not be followed by '.', expression=" + expression);
                return token;
            }
        }

        if (NUMBER_PATTERN.matcher(expression).matches()) {
            return new ValueToken(expression, Number.class);
        } else {
            if (!FIELD_PATTERN.matcher(expression).matches())
                throw new Error("invalid field name, field=" + expression);
            return new FieldToken(expression);
        }
    }

    private Token parseMethod(String expression, int leftParenthesesIndex) {
        int length = expression.length();
        String method = expression.substring(0, leftParenthesesIndex);
        if (!METHOD_PATTERN.matcher(method).matches()) throw new Error("invalid method name, method=" + method);

        MethodToken token = new MethodToken(method);
        int endIndex = findMethodEnd(expression, leftParenthesesIndex + 1);
        parseMethodParams(token, expression.substring(leftParenthesesIndex + 1, endIndex));
        if (endIndex + 1 < length) {
            if (expression.charAt(endIndex + 1) != '.')
                throw new Error("method can only be followed by '.', expression=" + expression);
            if (endIndex + 2 == length)
                throw new Error("method can only be followed by field or method, expression=" + expression);
            token.next = parse(expression.substring(endIndex + 2));
            if (token.next instanceof ValueToken)
                throw new Error("value token must not be followed by '.', expression=" + expression);
        }
        return token;
    }

    private void parseMethodParams(MethodToken token, String params) {
        var builder = new StringBuilder();
        int length = params.length();
        for (int i = 0; i < length; i++) {
            char ch = params.charAt(i);
            if (ch == ',') {
                String param = builder.toString().trim();
                if (param.isEmpty())
                    throw new Error("expect param before ',', method=" + token.name);
                token.params.add(parse(param));
                builder = new StringBuilder();
            } else {
                builder.append(ch);
            }
        }
        String param = builder.toString().trim();
        if (param.length() > 0)
            token.params.add(parse(param));
    }

    private int findMethodEnd(String expression, int index) {
        int leftParentheses = 0;
        int length = expression.length();
        for (int i = index; i < length; i++) {
            char ch = expression.charAt(i);
            if (ch == ')') {
                if (leftParentheses == 0) return i;
                else leftParentheses--;
            } else if (ch == '(') {
                leftParentheses++;
            }
        }
        throw new Error("parentheses is not closed properly");
    }
}
