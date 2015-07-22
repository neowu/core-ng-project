package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Strings;
import core.framework.impl.template.expression.CallTypeStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author neo
 */
public class TemplateBuilder {
    private final String template;
    private final CallTypeStack stack;
    private final Deque<CompositeHandler> handlerStack = new ArrayDeque<>();
    private StringBuilder currentContent = new StringBuilder();
    private int currentLineNumber;

    public TemplateBuilder(String template, Class<?> modelClass) {
        //TODO: validate
        this.template = template;
        this.stack = new CallTypeStack(modelClass);
        handlerStack.add(new Template(modelClass));
    }

    public Template build() {
        try (BufferedReader reader = new BufferedReader(new StringReader(template))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;

                currentLineNumber++;
                if (isDirective(line)) {
                    processDirective(line);
                } else if (containsExpression(line)) {
                    processExpression(line);
                } else {
                    currentContent.append(line).append('\n');
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (handlerStack.size() != 1) {
            throw Exceptions.error("directive is not closed, handlers={}", handlerStack.peek());
        }

        addStaticContentFragmentHandler();

        return (Template) handlerStack.peek();
    }

    private void processDirective(String line) {
        addStaticContentFragmentHandler();
        int index = line.indexOf("<!--%");
        int endIndex = line.indexOf("%-->");
        String statement = line.substring(index + 5, endIndex).trim();
        if ("end".equals(statement)) {
            CompositeHandler handler = handlerStack.pop();
            handlerStack.peek().handlers.add(handler);
            if (handler instanceof ForHandler) {
                stack.paramClasses.remove(((ForHandler) handler).variable);
            }
        } else {
            String location = Strings.format("L{}:{}", currentLineNumber, statement);
            if (statement.startsWith("if")) {
                handlerStack.push(new IfHandler(statement, stack, location));
            } else if (statement.startsWith("for")) {
                ForHandler forHandler = new ForHandler(statement, stack, location);
                handlerStack.push(forHandler);
                stack.paramClasses.put(forHandler.variable, forHandler.valueClass);
            } else {
                throw Exceptions.error("unsupported directive, location={}", location);
            }
        }
    }

    private void processExpression(String line) {
        boolean inExpression = false;
        StringBuilder currentExpression = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '$' && i + 1 < line.length() && line.charAt(i + 1) == '{') {
                addStaticContentFragmentHandler();
                inExpression = true;
                i++;
            } else if (ch == '}') {
                String expression = currentExpression.toString();
                String location = Strings.format("L{}:{}", currentLineNumber, expression);
                handlerStack.peek().handlers.add(new ExpressionHandler(expression, stack, location));
                currentExpression = new StringBuilder();
                inExpression = false;
            } else if (inExpression) {
                currentExpression.append(ch);
            } else {
                currentContent.append(ch);
            }
        }
        if (currentExpression.length() > 0)
            throw Exceptions.error("expression is not closed, location=L{}:{}", currentLineNumber, currentExpression.toString());

        currentContent.append('\n');
    }

    private void addStaticContentFragmentHandler() {
        if (currentContent.length() > 0) {
            handlerStack.peek().handlers.add(new StaticHandler(currentContent.toString()));
            currentContent = new StringBuilder();
        }
    }

    private boolean isDirective(String line) {
        int length = line.length();
        for (int i = 0; i < length; i++) {
            char ch = line.charAt(i);
            if (ch != ' ') {
                boolean match = line.startsWith("<!--%", i);
                if (match && !line.endsWith("%-->"))
                    throw Exceptions.error("directive must ends with \"%-->\", location=L{}:{}", currentLineNumber, line);
                return match;
            }
        }
        return false;
    }

    private boolean containsExpression(String line) {
        return line.contains("${");
    }
}
