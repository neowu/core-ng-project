package core.framework.impl.template.expression;

import core.framework.util.Exceptions;

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
                throw Exceptions.error("\" or \' is not closed, expression={}", expression);
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
                    throw Exceptions.error("invalid field name, field={}", field);
                FieldToken token = new FieldToken(field);
                token.next = parse(expression.substring(i + 1));
                if (token.next instanceof ValueToken)
                    throw new Error("value token must not follow '.'");
                return token;
            }
        }

        if (NUMBER_PATTERN.matcher(expression).matches()) {
            return new ValueToken(expression, Number.class);
        } else {
            if (!FIELD_PATTERN.matcher(expression).matches())
                throw Exceptions.error("invalid field name, field={}", expression);
            return new FieldToken(expression);
        }
    }

    private Token parseMethod(String expression, int leftParenthesesIndex) {
        int length = expression.length();
        String method = expression.substring(0, leftParenthesesIndex);
        if (!METHOD_PATTERN.matcher(method).matches())
            throw Exceptions.error("invalid method name, method={}", method);

        MethodToken token = new MethodToken(method);
        int endIndex = findMethodEnd(expression, leftParenthesesIndex + 1);
        parseMethodParams(token, expression.substring(leftParenthesesIndex + 1, endIndex));
        if (endIndex < length - 1) {
            if (endIndex + 1 >= length)
                throw new Error("method can only be followed by field or method");
            if (expression.charAt(endIndex + 1) != '.')
                throw new Error("method can only be followed by '.'");

            token.next = parse(expression.substring(endIndex + 2));
            if (token.next instanceof ValueToken)
                throw new Error("value token must not be followed by '.'");
        }
        return token;
    }

    private void parseMethodParams(MethodToken token, String params) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < params.length(); i++) {
            char ch = params.charAt(i);
            if (ch == ',') {
                String param = builder.toString().trim();
                if (param.length() == 0)
                    throw Exceptions.error("expect param before ',', method={}", token.name);
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
        for (int i = index; i < expression.length(); i++) {
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
